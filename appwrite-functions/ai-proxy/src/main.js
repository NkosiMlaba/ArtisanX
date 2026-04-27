import { Groq } from "groq-sdk";

const GROQ_API_KEY = process.env.GROQ_API_KEY ?? "";
const OPENROUTER_API_KEY = process.env.OPENROUTER_API_KEY ?? "";

export default async ({ req, res, log, error }) => {
  let body = {};
  try {
    if (req.query?.d) {
      body = JSON.parse(decodeURIComponent(req.query.d));
    } else if (typeof req.body === "object" && req.body !== null) {
      body = req.body;
    } else if (typeof req.body === "string" && req.body.trim().startsWith("{")) {
      body = JSON.parse(req.body);
    }
  } catch {
    body = {};
  }

  const { action } = body;

  try {
    if (action === "generateJobDescription") {
      const { category, roughDescription } = body;

      const groq = new Groq({ apiKey: GROQ_API_KEY });
      const completion = await groq.chat.completions.create({
        model: "llama-3.3-70b-versatile",
        max_tokens: 300,
        messages: [
          {
            role: "system",
            content: `You are a helpful assistant for ArtisansX, a South African artisan services marketplace.
A customer needs help writing a clear job description. Reply with ONLY the improved description — no preamble, no labels.
Write 3-5 clear sentences. Include what needs to be done and relevant details a skilled artisan would need.
Keep it in simple English accessible to South African users. Do not include pricing.`,
          },
          {
            role: "user",
            content: `Category: ${category}\nRough description: "${roughDescription}"\n\nWrite the improved job description:`,
          },
        ],
      });

      const result = completion.choices[0]?.message?.content?.trim() ?? "";
      return res.json({ result });
    }

    if (action === "getBidSuggestion") {
      const { jobTitle, jobDescription, category, budget, artisanSkills } = body;
      const budgetText = budget > 0 ? `Customer budget: R${budget}` : "No budget specified";

      const response = await fetch("https://openrouter.ai/api/v1/chat/completions", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${OPENROUTER_API_KEY}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          model: "anthropic/claude-sonnet-4-5",
          max_tokens: 400,
          messages: [
            {
              role: "system",
              content: `You are a pricing assistant for ArtisansX, a South African artisan marketplace.
Help artisans price their services fairly and write professional messages.
Respond in this exact JSON format:
{"minPrice": 350, "maxPrice": 500, "messageTemplate": "Your professional message template here"}
Use South African Rand (ZAR). Keep the message template under 150 words, professional and friendly.`,
            },
            {
              role: "user",
              content: `Job: ${jobTitle}\nCategory: ${category}\nDescription: ${jobDescription}\n${budgetText}\nArtisan skills: ${artisanSkills}\n\nSuggest a fair price range and message template:`,
            },
          ],
        }),
      });

      const data = await response.json();
      const result = data.choices?.[0]?.message?.content?.trim() ?? "";
      return res.json({ result });
    }

    if (action === "matchArtisans") {
      const { jobTitle, jobDescription, category, artisanList } = body;

      const response = await fetch("https://openrouter.ai/api/v1/chat/completions", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${OPENROUTER_API_KEY}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          model: "anthropic/claude-sonnet-4-5",
          max_tokens: 400,
          messages: [
            {
              role: "system",
              content: `You are a matching assistant for ArtisansX, a South African artisan marketplace.
Rank artisans best suited for the given job. Consider: skills match, rating, experience.
Respond with ONLY a JSON array in this exact format:
[{"artisanId":"id","explanation":"One sentence why they're a good match"}]
Return up to 3 artisans ranked best first. If none match well, return an empty array [].`,
            },
            {
              role: "user",
              content: `Job: ${jobTitle}\nCategory: ${category}\nDescription: ${jobDescription}\n\nAvailable artisans:\n${artisanList}\n\nReturn the ranked JSON array:`,
            },
          ],
        }),
      });

      const data = await response.json();
      const result = data.choices?.[0]?.message?.content?.trim() ?? "";
      return res.json({ result });
    }

    return res.json({ error: `Unknown action: ${action}` }, 400);
  } catch (err) {
    error(err.message);
    return res.json({ error: "Internal error" }, 500);
  }
};
