# ArtisanX

## What I Am

I am ArtisanX, a location-based marketplace application for the South African skilled-trades economy. I connect customers who need work done with artisans who can do the work. The trades I support include plumbing, electrical, painting, carpentry, tiling, roofing, and general handywork.

I run on Android. I am built with Kotlin and Jetpack Compose using Material 3 design. I use Appwrite Cloud for my backend services, including authentication, database storage, file storage, real-time updates, and serverless functions. I use Google Maps for location features. I use Groq and OpenRouter as my AI providers.

## Who I Serve

I serve two kinds of users.

The first is the **customer**. A customer is anyone who needs a job done at home or at a place of business. The customer can post a job, review bids from artisans, accept a bid, communicate with the chosen artisan, and rate the work after the job is complete.

The second is the **artisan**. An artisan can be a qualified independent tradesperson, or a student studying a trade at a university or TVET college. The artisan can browse jobs in their service area, submit bids on those jobs, manage active bookings, and build a public reputation through customer reviews.

I treat both kinds of users with equal importance. Each role has its own dashboard, its own bottom navigation, and its own workflow.

## Why I Exist

The South African skilled-trades sector is large but fragmented. Customers often struggle to find reliable artisans, and skilled workers often struggle to find consistent work. Word of mouth is the dominant channel, which limits opportunity for both sides.

I exist to make this market more efficient and more transparent. I give customers a structured way to describe their needs and compare options. I give artisans a way to reach customers beyond their immediate network. I record reviews and ratings so that good work builds a verifiable reputation over time.

I also create a path for trade students to earn while they learn. A student artisan receives a clear badge on their profile, and customers who want to support emerging professionals can choose them with confidence.

## How I Work

### Registration and Role Selection

A new user creates an account with their full name, email, and a password of at least eight characters. After registration, the user chooses to act as a customer or as an artisan. I save this choice both on the device and in my user profile collection so that the correct dashboard is shown on every future visit.

If a user chooses the artisan role, I take them through a short onboarding flow where they enter their trade category, their skills, their service area, and their hourly rate. This profile is what customers see when an artisan submits a bid.

### Posting a Job

A customer posts a job from their dashboard by tapping the floating action button. The job form asks for a title, a category, a description, a location, and an optional budget.

If the customer is unsure how to describe the problem, they can tap an AI assistant button. I send their rough description and the chosen category to my AI proxy. The proxy returns a clear, structured job description in three to five sentences, written in simple English suitable for South African users. The customer can accept the suggested text, edit it, or keep their own version.

The location is captured on a map. The customer drags the map until the pin sits on the correct address, and a live geocoded street address is shown at the bottom of the screen. When the customer confirms, the address and coordinates are saved with the job.

### Browsing and Bidding

An artisan sees jobs that match their service area on the browse screen. They can filter by category, sort by date or budget, and search by keyword across the title, description, category, and address.

When an artisan opens a job, they can submit a bid. The bid includes a price offer in South African Rand, a short message to the customer, and an estimated number of hours. If the artisan would like help with pricing, they can tap an AI suggestion button. I send the job details and the artisan's skills to my AI proxy, which returns a fair price range and a draft professional message. The artisan reviews and edits the suggestion before submitting.

I enforce one bid per artisan per job. If the artisan changes their mind, they can edit the bid as long as it has not yet been accepted or rejected.

### Hiring and Booking

The customer reviews all bids on their job, sorted by price. Each bid card shows the artisan's name, rating, distance, price, and message. When the customer accepts a bid, I create a booking record, mark the chosen bid as accepted, mark all other bids as rejected, and update the job status to assigned.

A booking moves through four states. It begins as requested. The artisan accepts it, which moves it to accepted. When the artisan starts the work, the state moves to in progress. When the work is finished, the artisan marks it complete. The customer can then mark the booking as paid once payment has been settled outside the app.

### Communication

I provide an in-app chat scoped to each booking. Both parties can send text messages and photos. I use Appwrite Realtime to deliver messages instantly without polling. This means messages appear on both screens within moments of being sent.

### Reviews

After a booking is complete, the customer can rate the artisan from one to five stars and leave a comment of up to one thousand characters. I update the artisan's average rating and review count automatically. Each booking can receive only one review, which protects the integrity of the rating system.

### Credits

Each new artisan receives five free credits when they register. Viewing a full job detail costs one credit, and submitting a bid on a bidding-type job costs two credits. I show the credit balance on the artisan dashboard and prevent any action that would take the balance below zero.

## My Architecture

I follow the MVVM pattern with a repository layer. My presentation layer is built entirely in Jetpack Compose. My view models expose state through StateFlow and a sealed Resource class with three states: Loading, Success, and Error. My screens collect this state and render the appropriate UI for each case.

I use Hilt for dependency injection. My Hilt modules provide the Appwrite client, the Account service, the Databases service, the Storage service, the Realtime service, and the Functions service as singletons.

My navigation uses a single Activity with Compose Navigation. The start destination is decided at launch time. If a session is active and a profile exists, the user lands on the relevant dashboard. If a session is active but no role has been selected, the user lands on the role selection screen. If no session is active, the user lands on the login screen.

## My AI Features

I include three AI-powered features. All three route through a single Appwrite serverless function called ai-proxy, which keeps my API keys on the server side rather than on the device.

The first feature is the **smart job description generator**, which helps customers turn a rough idea into a clear job post. It uses a Llama model through Groq.

The second feature is the **artisan bid helper**, which suggests a fair price range and a professional message template for an artisan preparing a bid. It uses Claude Sonnet through OpenRouter.

The third feature is the **job to artisan matching service**, which ranks the most suitable artisans for a given job and provides a one-line explanation for each match. It also uses Claude Sonnet through OpenRouter.

If any AI request fails, I fall back gracefully. The user can always complete the underlying task by hand. I never block a user on AI availability.

## My Configuration

I read all sensitive configuration from a local properties file at the project root. This file is excluded from version control. The required keys are the Android SDK directory, the Google Maps API key, the Appwrite project ID, the Appwrite endpoint, the Groq API key, and the OpenRouter API key. I expose these values to my code through generated BuildConfig fields.

## My Documentation

The full implementation plan, including phased delivery, the complete database schema, the security model, and the edge case matrix, is stored separately in [.docs/PROJECT_PLAN.md](.docs/PROJECT_PLAN.md).

The detailed user journeys for every screen are stored in [.docs/WORKFLOWS.md](.docs/WORKFLOWS.md).

The current state of feature delivery against the original specification is stored in [.docs/GAP_ANALYSIS.md](.docs/GAP_ANALYSIS.md).

The proposed future enhancements covering native AI, robotics, and emerging technologies are stored in [.docs/AI_AND_EMERGING_TECH.md](.docs/AI_AND_EMERGING_TECH.md).

## My Build Commands

I am built and tested with the standard Gradle wrapper.

```bash
./gradlew assembleDebug
./gradlew test
./gradlew connectedAndroidTest
./gradlew lint
./gradlew clean assembleDebug
```

## My Current State

Phase one of my implementation is complete. This includes authentication, role selection, job posting and browsing, basic profiles, navigation, bidding, bookings, in-app chat with real-time updates, reviews, credits, and all three AI features routed through the server-side proxy.

Future phases cover in-app payments, pro subscriptions for artisans, an admin web panel, micro-learning modules for skills development, and an iOS version. These items are documented in the project plan but are not yet built.
