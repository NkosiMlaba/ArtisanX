# ArtisanX — App Workflows

End-to-end user journeys for both roles. Every screen and action described in order.

---

## Contents

1. [Registration & Onboarding](#1-registration--onboarding)
2. [Customer: Post a Job → Receive Bids → Hire → Complete](#2-customer-post-a-job--receive-bids--hire--complete)
3. [Artisan: Browse Jobs → Bid → Get Hired → Complete Work](#3-artisan-browse-jobs--bid--get-hired--complete-work)
4. [In-App Chat (both roles)](#4-in-app-chat-both-roles)
5. [Reviews & Ratings](#5-reviews--ratings)
6. [Profile Management](#6-profile-management)
7. [Credits (Artisan)](#7-credits-artisan)
8. [AI Features](#8-ai-features)
9. [Test Account Quick-Login](#9-test-account-quick-login)

---

## 1. Registration & Onboarding

### New User Registration

1. Open app → **"Don't have an account? Register"**
2. Enter: Full Name, Email, Password (min 8 chars) → **Register**
3. App navigates to **Role Selection**:
   - Tap **"I'm a Customer"** — I need work done
   - Tap **"I'm an Artisan"** — I do the work
4. Role is saved locally (DataStore) and to `user_profiles` in Appwrite

### Artisan-Specific Onboarding (after role selection)

After choosing Artisan, you land on **Artisan Onboarding**:

1. Enter: Full Name, Trade Category (e.g. Plumbing), Skills, Service Area, Hourly Rate
2. Tap **Complete Profile**
3. Profile saved to `artisan_profiles` collection in Appwrite
4. Redirected to **Artisan Dashboard**

### Returning User Login

1. Open app → enter Email + Password → **Log In**
2. App checks Appwrite session:
   - Has artisan profile → **Artisan Dashboard**
   - Has customer profile with role="customer" → **Customer Dashboard**
   - Neither → **Role Selection** (completes onboarding)

### Switching Accounts (Testing)

- Go to **Profile** tab → **Logout**
- Login screen appears with empty fields
- Enter the other account's credentials

---

## 2. Customer: Post a Job → Receive Bids → Hire → Complete

This is the full customer lifecycle from needing work done to a completed job.

### Step 1 — Post a Job

1. **Customer Dashboard** → tap the **+** FAB (bottom right)
2. **Post a Job** screen:
   - **Job Title** — e.g. "Fix leaking kitchen tap"
   - **Category** — e.g. "Plumbing"
   - **Job Description** — describe the problem
   - **✨ Help me describe this job** (optional) — AI generates a professional description from your title + category; tap **Use This** or **Keep Mine**
   - **Location** — tap the location card → map opens:
     - Drag the map so the pin sits on your address
     - Bottom bar shows live geocoded street address (e.g. "Joe Slovo Street, Durban Central")
     - Tap **Confirm** — address fills back into the form
   - **Budget (ZAR)** — your expected spend
3. Tap **Post Job** → job created with status `open`
4. Returns to Customer Dashboard showing the new job card

### Step 2 — View Your Posted Job

From **Customer Dashboard**, tap any job card → **Job Detail** screen shows:

- Title, category, status chip, budget, address
- Embedded map showing exact job pin location
- Description
- **View Bids (N)** button — opens the bids list
- **✨ Suggested Artisans** section — AI ranks artisans in that category by skills, rating, and service area match

### Step 3 — Review Bids

1. Tap **View Bids (N)** → **Bids List** screen
2. Each bid card shows:
   - Artisan name, badge (Starter/Pro/etc), star rating + review count
   - Offered price (R), estimated hours
   - Personal message from the artisan
3. Tap **Accept** on the bid you want → confirmation dialog with artisan name → confirm
4. Bid status → `accepted`; job status → `assigned`; a **Booking** is created automatically
5. All other bids are automatically marked `rejected`

### Step 4 — Track the Booking

1. **Customer Dashboard** → **Bookings** tab or tap **View Booking** from Job Detail
2. **Bookings** screen shows your booking with:
   - Job title, artisan name, booking status
   - Status progression: `pending` → `confirmed` → `in_progress` → `completed`
3. Tap a booking → booking detail with **Chat** button and status

### Step 5 — Communicate via Chat

1. From booking detail → tap **Chat** (or from Bookings list)
2. **Chat** screen opens with the artisan's name in the title bar
3. Type a message → send (tap send button or press Enter)
4. Messages are colour-coded: your messages (right, primary blue) vs artisan (left, grey)
5. Timestamps shown in South African time (UTC+2): "14:23" for today, "Apr 26, 14:23" for older
6. Chat polls every 5 seconds — new messages from the artisan appear automatically

### Step 6 — Mark Job Complete

When the artisan marks the job as started/completed:

1. Booking status updates to `in_progress` / `completed`
2. Customer receives a local notification ("Booking Status Updated")
3. Once **completed**, the **Review** button appears on the booking

### Step 7 — Leave a Review

1. From the completed booking → tap **Leave a Review**
2. **Review** screen:
   - Star rating (1–5 stars)
   - Written comment (optional)
3. Tap **Submit** → review saved; artisan's rating updates immediately

---

## 3. Artisan: Browse Jobs → Bid → Get Hired → Complete Work

### Step 1 — Check Your Credit Balance

- Open **Artisan Dashboard** → credit balance shown at top
- Each bid costs **2 credits**
- Low on credits? → tap **Buy more credits →** → Buy Credits screen

### Step 2 — Browse Open Jobs

1. Tap **Jobs** tab (bottom nav) → **Browse Open Jobs**
2. Each job card shows:
   - Job title + budget chip
   - Category (blue label)
   - Location icon + geocoded address (e.g. "Westville")
   - ⚡ Urgent badge if marked urgent
3. Filter by category using the chip row (All / Plumbing / Electrical / Cleaning / Carpentry / Painting)
4. Pull down to refresh

### Step 3 — View Job Detail

Tap any job card → **Job Detail** shows:

- Full title, category, status, budget, address
- Embedded map with job pin
- Full description
- **Place a Bid** button (if you haven't bid yet)
- **Bid Submitted — Awaiting Response** (greyed, if you already bid)
- **View Your Booking** (if your bid was accepted)

### Step 4 — Submit a Bid

1. Tap **Place a Bid** → **Bid Submit** screen
2. Fill in:
   - **Price Offer (R)** — your quoted price
   - **Estimated Hours** — how long it will take
   - **Message** — introduce yourself, explain your approach
   - **✨ Suggest a Bid** (optional) — AI analyses the job and suggests a price range + message template based on your skills and the job description
3. Tap **Submit Bid**
4. 2 credits deducted from your balance
5. Returns to Job Detail showing "Bid Submitted — Awaiting Response"

### Step 5 — Bid Accepted

When the customer accepts your bid:

1. You receive a local notification: **"Bid Accepted!"**
2. Your **Artisan Dashboard** → My Recent Bids → bid shows `Accepted` chip
3. A Booking is created automatically

### Step 6 — Manage the Booking

1. **Artisan Dashboard** → tap **My Bookings** or **Bookings** tab
2. Booking shows: job title, customer name, status
3. Status actions you can take:
   - `pending` → **Confirm** → status becomes `confirmed`
   - `confirmed` → **Start Job** → status becomes `in_progress`
   - `in_progress` → **Mark Complete** → status becomes `completed`
4. Customer receives a push notification at each status change

### Step 7 — Communicate via Chat

Same as customer — tap **Chat** from the booking to message the customer. Your name shows in their chat title bar.

### Step 8 — Job Completed

Once you tap **Mark Complete**:

- Booking status → `completed`
- Job status → `completed` (cascades automatically)
- Customer can now leave you a review
- Your average rating updates on your profile

---

## 4. In-App Chat (both roles)

Chat is scoped to a **Booking** — you can only chat with the person on the other side of a booking.

| Element | Behaviour |
|---|---|
| Access | Booking detail screen → Chat button |
| Title bar | Shows the other party's name (not "Chat") |
| Bubbles | Right (blue) = you; Left (grey) = them |
| Timestamps | SA time (UTC+2): "14:23" today, "Apr 26, 14:23" older |
| Refresh | Polls every 5 seconds; new messages appear automatically |
| Notification | Local notification when the OTHER party sends a new message (not your own sends) |
| Max length | 2 000 characters per message |

---

## 5. Reviews & Ratings

- Only **customers** can leave reviews, and only after a booking is `completed`
- Each booking can have exactly **one** review
- The review button only appears once — once submitted it disappears
- Artisan ratings are visible on:
  - **Artisan Profile** (star bar + list of individual reviews)
  - **Bids List** (star + count next to artisan name)
  - **Job Detail → Suggested Artisans** section

---

## 6. Profile Management

### Customer Profile

1. **Bookings** tab → **Profile** tab
2. Shows: Full Name, Email
3. Tap **Edit Profile** → edit name → **Save**
4. Tap **Logout** → returns to Login screen

### Artisan Profile

1. **Profile** tab (bottom nav)
2. Shows:
   - Full Name, Trade, Service Area, Hourly Rate, Badge
   - Average star rating bar + total review count
   - Scrollable list of individual reviews (reviewer, stars, comment, date)
3. Tap **Edit Profile** → edit: Full Name, Trade Category, Skills, Service Area, Hourly Rate → **Save**
4. Badge is assigned automatically based on review count:
   - 0 reviews → **Starter**
   - 1–4 reviews → **Rising**
   - 5–9 reviews → **Established**
   - 10+ reviews → **Pro**
5. Tap **Logout** → returns to Login screen

---

## 7. Credits (Artisan)

Credits are the artisan's bidding currency. Each bid costs 2 credits.

### Checking Balance

- Always visible on **Artisan Dashboard** at the top of the page

### Adding Credits (Test Mode)

1. **Artisan Dashboard** → **Buy more credits →**
2. **Buy Credits** screen:
   - Current balance shown at top
   - 4 real packages shown (payments not yet live):
     - Starter: 10 credits — R 9.99
     - Popular: 25 credits — R 19.99
     - Value: 60 credits — R 39.99
     - Pro: 150 credits — R 79.99
   - Scroll down to **Developer Test Mode** section
   - Tap **+5**, **+10**, or **+25** → credits added immediately, balance updates live
3. "In-app payments coming soon via PayFast / Yoco" notice shown

### Credit Deduction

- Happens automatically on bid submission
- If balance < 2, bid is rejected with an error message

---

## 8. AI Features

### ✨ Job Description Generator (Customer — Post Job screen)

- Tap **Help me describe this job** after entering Title and Category
- Powered by **Groq** (llama-3.3-70b-versatile)
- Generates a professional, detailed job description
- Review in the dialog → **Use This** (fills description field) or **Keep Mine** (discards)

### ✨ Bid Suggestion (Artisan — Bid Submit screen)

- Tap **Suggest a Bid** after the job detail has loaded
- Powered by **OpenRouter → Claude** (claude-sonnet-4-5)
- Analyses: job title, description, category, customer budget, your registered skills
- Returns: suggested price range (min/max in ZAR) + a personalised message template
- Pre-fills the price and message fields; you can edit before submitting

### ✨ Suggested Artisans (Customer — Job Detail screen)

- Visible only to the job owner on `open` jobs
- Powered by **OpenRouter → Claude** (claude-sonnet-4-5)
- Loads automatically when the job detail opens
- Ranks artisans in the same category by: skills match, service area, rating, review count
- Shows name, trade, service area, star rating, badge, and AI explanation for the match

---

## 9. Test Account Quick-Login

Both passwords have been set to `TestPass123!`.

| Role | Email | Password | Appwrite ID |
|---|---|---|---|
| Customer | customer_test@artisanx.dev | TestPass123! | testcustomer001 |
| Artisan | artisan_test@artisanx.dev | TestPass123! | testartisan001 |

### Resetting a test password (if needed)

```bash
appwrite users update-password \
  --user-id testcustomer001 \
  --password "TestPass123!"

appwrite users update-password \
  --user-id testartisan001 \
  --password "TestPass123!"
```

### Existing test data in Appwrite

| Collection | Data |
|---|---|
| `jobs` | "Fix leaking kitchen tap" — Plumbing, R500, Durban North, KZN (status: assigned) |
| `jobs` | "Water Leak Fix" — Plumbing, R500, Westville (status: open) |
| `bids` | testartisan001 bid on the kitchen tap job (accepted) |
| `bookings` | Booking linking testcustomer001 ↔ testartisan001 for the kitchen tap job |
| `credits` | testartisan001 balance: 10 credits |
| `chat_messages` | Messages in the booking chat |

---

## Screen Map

```
Login ──────────────────────────────────────────────────┐
Register ── RoleSelection ── ArtisanOnboarding ─────────┤
                                                         │
Customer Flow:                                           │
  CustomerDashboard ── PostJob                           │
       │                └── LocationPickerScreen         │
       ├── JobDetail ── BidsList ── (accept bid)         │
       │       └── BuyCredits (not customer, artisan)    │
       ├── CustomerBookings ── Chat                      │
       │                   └── Review                    │
       └── CustomerProfile                               │
                                                         │
Artisan Flow:                                            │
  ArtisanDashboard ── BuyCredits                        │
       ├── JobBrowse ── JobDetail ── BidSubmit           │
       ├── ArtisanBookings ── Chat                       │
       └── ArtisanProfile                               ─┘
```

---

## Status Flows

### Job Status

```
open → assigned → in_progress → completed
         ↑
   (bid accepted)
```

### Booking Status

```
pending → confirmed → in_progress → completed
  ↑           ↑            ↑            ↑
(created)  (artisan     (artisan     (artisan
           confirms)     starts)    completes)
```

### Bid Status

```
pending → accepted  (triggers booking creation)
        → rejected  (all other bids when one is accepted)
```
