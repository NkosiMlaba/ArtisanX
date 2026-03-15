
# ArtisansX
Folder
C:\Users\nkosi\AndroidStudioProjects\ArtisanX

File
C:\Users\nkosi\AndroidStudioProjects\ArtisanX\app\src\main\java\com\example\artisanx\MainActivity.kt

# ArtisansX — PROJECT PLAN & PHASED IMPLEMENTATION

> Location-based artisan services marketplace for South Africa.  
> Stack: Kotlin · Jetpack Compose (M3) · Appwrite Cloud · Google Maps · Groq / OpenRouter AI  
> Architecture: MVVM + Repository · Hilt DI · Coroutines/Flow

---

## Table of Contents

1. [Pre-Development Setup Checklist](#1-pre-development-setup-checklist)
2. [Phase 1 — Mini MVP (Foundation)](#2-phase-1--mini-mvp-foundation)
3. [Phase 2 — Core Marketplace](#3-phase-2--core-marketplace)
4. [Phase 3 — Communication & Trust](#4-phase-3--communication--trust)
5. [Phase 4 — AI Integration & Polish](#5-phase-4--ai-integration--polish)
6. [Phase 5 — Post-MVP / Future](#6-phase-5--post-mvp--future)
7. [Database Schema Reference](#7-database-schema-reference)
8. [AI Feature Documentation](#8-ai-feature-documentation)
9. [Security Considerations](#9-security-considerations)
10. [Edge Cases & Error Handling Matrix](#10-edge-cases--error-handling-matrix)

---

## 1. Pre-Development Setup Checklist

These are things **you** (the developer) must do before writing any app code. The Claude Code prompt cannot do these for you.

### Accounts & Projects to Create

| Service | What to Do | You'll Get |
|---------|-----------|------------|
| **Appwrite Cloud** | Sign up at https://cloud.appwrite.io → Create project "ArtisansX" → Add Android platform (`com.example.artisanx`) | Project ID, Region endpoint URL |
| **Google Cloud Console** | https://console.cloud.google.com → New project → Enable: Maps SDK for Android, Places API, Geocoding API, Directions API → Create API key → Restrict to Android (SHA-1 + package name) | `MAPS_API_KEY` |
| **Groq & OpenRouter** | Get API keys from Groq Cloud and OpenRouter for AI features | `GROQ_API_KEY`, `OPENROUTER_API_KEY` |
| **Firebase** (Phase 3) | https://console.firebase.google.com → New project → Add Android app → Download `google-services.json` → Enable Cloud Messaging | FCM setup for push notifications |

### `local.properties` (root of project — NEVER commit)

```properties
sdk.dir=/path/to/android/sdk
MAPS_API_KEY=AIza...your_key
APPWRITE_PROJECT_ID=your_project_id
APPWRITE_ENDPOINT=https://fra.cloud.appwrite.io/v1
GROQ_API_KEY=gsk_your_key
OPENROUTER_API_KEY=sk-or-your_key
```

### Appwrite Console Setup

Create database `artisansx_db`, then create all collections and attributes listed in [Section 7](#7-database-schema-reference). Create storage bucket `artisansx_files`. Set permissions per collection as specified.

### Android Studio

- Install latest stable Android Studio
- Use "Empty Activity" (Jetpack Compose) template
- Package: `com.example.artisanx`, Min SDK 24
- After creation, add all dependencies from the Claude Code prompt

---

## 2. Phase 1 — Mini MVP (Foundation)

**Goal:** A user can register, log in, choose a role, and a customer can post a job that an artisan can see. This is the "hello world" of the marketplace — proving the vertical slice works.

### 2.1 Project Scaffolding

**Tasks:**
- Add all dependencies to `build.gradle.kts` (Appwrite, Hilt, Navigation, Maps, Coil, Retrofit, DataStore)
- Configure Hilt: `@HiltAndroidApp` on `ArtisansXApp.kt`, `@AndroidEntryPoint` on `MainActivity`
- Create `AppModule.kt` — provides Appwrite `Client`, `Account`, `Databases`, `Storage` as singletons
- Create `Constants.kt` — all Appwrite IDs (database, collections, bucket) as `const val`
- Create `Resource.kt` sealed class
- Read API keys from `local.properties` into `BuildConfig`
- Set up `AndroidManifest.xml` with Maps meta-data, permissions, Appwrite OAuth activity

**Deliverables:** App compiles, Hilt injects Appwrite client, `BuildConfig` has keys.

### 2.2 Authentication

**Tasks:**
- `AuthRepository` interface + `AuthRepositoryImpl` (Appwrite `Account` service)
  - `register(email, password, name)` → creates Appwrite account
  - `login(email, password)` → creates email session
  - `logout()` → deletes current session
  - `getCurrentUser()` → gets logged-in user or null
  - `isLoggedIn()` → boolean check
- `LoginScreen` — email + password fields, login button, "Don't have an account?" link
- `RegisterScreen` — name, email, password, confirm password. Validation: email format, password ≥8 chars, passwords match
- `LoginViewModel` / `RegisterViewModel` — manage form state, call repo, emit `Resource`
- On successful register → navigate to Role Selection
- On successful login → check if profile exists → navigate to appropriate dashboard

**Edge cases:**
- Network failure during auth → show retry-able error
- Email already registered → parse Appwrite error, show "Email already in use"
- Session expired → redirect to login with message
- Empty fields → disable button + inline validation messages

**Deliverables:** User can register and log in. Session persists across app restarts.

### 2.3 Role Selection & Profile Creation

**Tasks:**
- After registration, show a one-time screen: "I am a **Customer**" / "I am an **Artisan**"
- Customer path → create `user_profiles` document → navigate to Customer Dashboard
- Artisan path → navigate to Artisan Onboarding (Phase 1 keeps it simple: just name, trade category, skills, service area — no file uploads yet)
- Store role preference in DataStore so the app knows which dashboard to show on next launch

**Edge cases:**
- User kills app before completing profile → on next launch, detect missing profile, redirect to complete it
- Double-tap on submit → disable button on first tap

**Deliverables:** Role persists. Each role sees its own nav shell.

### 2.4 Navigation Shell

**Tasks:**
- `Screen.kt` sealed class with all routes
- `AppNavGraph.kt` — NavHost with conditional start destination (login if no session, dashboard if session exists)
- Customer bottom nav: Home (jobs), Post Job, Messages (placeholder), Profile
- Artisan bottom nav: Home (browse jobs), My Jobs (placeholder), Messages (placeholder), Profile
- `BottomNavBar.kt` — shared composable, items driven by role

**Deliverables:** Tabbed navigation working for both roles.

### 2.5 Customer: Post a Job (CRUD)

**Tasks:**
- `PostJobScreen` — form with: title (text), category (dropdown: Plumbing, Electrical, Painting, Carpentry, Tiling, Roofing, General, Other), description (multiline), budget (optional number), urgency (Standard/Urgent toggle), job type (Standard/Bidding)
- Location: for Phase 1, just a text field for address. Map pin comes in Phase 2.
- `JobRepository.createJob(job)` → creates document in `jobs` collection
- `CustomerDashboardScreen` → lists user's posted jobs (LazyColumn of `JobCard`)
- `JobDetailScreen` → view single job with all details
- Edit job (only if status is "open")
- Delete job (only if status is "open", with confirmation dialog)

**Validation:**
- Title: 5–100 chars
- Description: 20–2000 chars
- Budget: if provided, must be > 0
- Category: must be selected

**Deliverables:** Customer can create, read, update, delete jobs. Full CRUD.

### 2.6 Artisan: Browse Jobs

**Tasks:**
- `JobBrowseScreen` — shows all jobs with status "open" (LazyColumn)
- Filter by category (chip row at top)
- Sort by: newest, nearest (placeholder until maps), budget high→low
- Tap job → `JobDetailScreen` (read-only for artisan in Phase 1)
- Pull-to-refresh

**Edge cases:**
- No jobs available → empty state illustration + message
- Pagination: if >25 jobs, implement Appwrite cursor-based pagination

**Deliverables:** Artisan sees available jobs. Filters work.

### 2.7 Basic Profile Screens

**Tasks:**
- Customer profile: view/edit name, email, phone, addresses
- Artisan profile: view/edit name, trade category, skills, service area
- Logout button on both

**Deliverables:** Users can view and update their profiles.

---

## 3. Phase 2 — Core Marketplace

**Goal:** The marketplace actually works — artisans can bid, customers can accept, bookings are tracked, and maps show real locations.

### 3.1 Google Maps Integration

**Tasks:**
- `MapScreen` composable using `GoogleMap` from maps-compose library
- On `PostJobScreen`: replace address text field with a map + pin drop. Save lat/lng + geocoded address.
- On `JobBrowseScreen`: add a "Map View" toggle — shows jobs as markers on map
- On `ArtisanProfile`: artisan sets their service area center + radius on a map
- `LocationUtils.kt` — get current location via `FusedLocationProviderClient`, handle permissions
- Customer sees artisan's distance from job location
- "Navigate" button: opens Google Maps app with directions intent

**Permissions handling:**
- Use Accompanist `rememberPermissionState`
- Show rationale dialog explaining why location is needed
- Graceful fallback if denied: allow manual address entry, hide distance features

**Deliverables:** Map shows on job posting and browsing. Distance calculated. Navigation button works.

### 3.2 Artisan Onboarding (Full — Student vs Independent)

**Tasks:**
- After role selection as Artisan, show: "Are you currently a university or TVET student?"
- **Student path:** full name, phone, institution name, student number, upload student card (camera or gallery → Appwrite Storage), course/field, expected grad year, trade category, skills, service area + map pin, optional work photos
- **Independent path:** full name, phone, ID upload, trade category, skills, service area + map pin, work photos (required), optional certifications, years experience
- Badge assignment: "Student Artisan" or "Verified Artisan" (pending admin approval — for MVP, auto-set)
- Multi-step form (stepper UI) — don't dump everything on one screen
- File upload: compress images before upload (max 2MB), show upload progress, handle failure with retry

**Edge cases:**
- Large image → compress client-side using `Bitmap.compress()`
- Upload fails midway → show retry button, don't lose other form data
- Camera permission denied → fall back to gallery only
- Student card unreadable → store anyway, admin reviews

**Deliverables:** Both onboarding paths work end-to-end with file uploads.

### 3.3 Bidding System

**Tasks:**
- Jobs with `jobType = "bidding"` (or budget ≥ R1,000) open for bids
- `BidSubmitScreen` — artisan enters: price offer, message, estimated hours
- `BidsListScreen` — customer views all bids on their job, sorted by price. Shows artisan name, rating, distance, price, message.
- Customer taps "Accept" on a bid → bid status = "accepted", other bids = "rejected", job status = "assigned"
- Artisan sees bid status on their "My Jobs" tab

**Validation:**
- Price: must be > 0, reasonable range (warn if >10x budget)
- Message: 10–500 chars
- Estimated hours: > 0
- One bid per artisan per job (enforce in repo layer + Appwrite query check)

**Edge cases:**
- Artisan tries to bid on own job → block
- Customer accepts bid on already-assigned job → check status before updating
- Artisan edits bid → allow only if status = "pending"

**Deliverables:** Full bidding flow: submit → view → accept/reject.

### 3.4 Booking & Status Tracking

**Tasks:**
- When customer accepts artisan (direct or via bid) → create `bookings` document
- Status flow: `requested` → `accepted` → `in_progress` → `completed`
- Customer: sees booking status on dashboard, can view details
- Artisan: accepts booking (status → "accepted"), starts job (→ "in_progress"), marks complete (→ "completed")
- `BookingStatusScreen` — shows current status with a visual stepper/timeline
- Mark as paid (offline): customer taps "Mark Paid" → `isPaid = true`

**Edge cases:**
- Double booking same artisan at same time → warn but allow (artisan manages their schedule)
- Cancel booking → only if status is "requested" or "accepted", not in_progress
- Artisan marks complete but customer disagrees → for MVP, trust artisan; dispute resolution is post-MVP

**Deliverables:** End-to-end booking lifecycle works.

### 3.5 Credits System (Basic)

**Tasks:**
- New artisans get 5 free credits on registration
- Viewing full job details costs 1 credit (standard jobs)
- Submitting a bid costs 2 credits (bidding jobs)
- `credits` collection tracks balance
- Show credit balance on artisan dashboard
- "Insufficient credits" dialog when trying to unlock without enough
- "Buy Credits" screen — for MVP, just a placeholder with a note that in-app purchases are Phase 5. Can simulate adding credits for testing.

**Edge cases:**
- Race condition: artisan spends last credit simultaneously on two jobs → check balance before decrement atomically (use Appwrite function if possible, or optimistic check + re-verify)
- Negative balance → never allow, always check >= cost before proceeding

**Deliverables:** Credits deducted on job access. Balance visible. Cannot go negative.

---

## 4. Phase 3 — Communication & Trust

**Goal:** Users can chat within bookings, rate each other, and get notified of important events.

### 4.1 In-App Chat

**Tasks:**
- Chat available per booking (accessed from `BookingDetailScreen`)
- `ChatScreen` — message list (LazyColumn, reversed), text input, send button
- Send text messages, store in `chat_messages` collection
- Send photos (camera/gallery → upload to storage → store fileId in message)
- `ChatListScreen` — all active chats for the user, showing last message preview
- **Appwrite Realtime**: subscribe to `chat_messages` collection filtered by bookingId for live updates
- Show "typing" indicator (optional — can use a lightweight Appwrite document or skip for MVP)

**Edge cases:**
- Message fails to send → show retry icon next to message
- Very long message → truncate at 2000 chars
- Image upload fails → show error, allow retry, don't lose text message
- Chat with no messages → friendly empty state

**Deliverables:** Real-time chat within bookings. Photos supported.

### 4.2 Ratings & Reviews

**Tasks:**
- After booking status = "completed", customer can rate artisan
- `ReviewScreen` — 1-5 star selector (tappable stars composable), comment text field
- Submit → create `reviews` document, update `artisan_profiles.avgRating` and `.reviewCount`
  - New avg = ((oldAvg * oldCount) + newRating) / (oldCount + 1)
- Artisan profile shows: average rating (stars), review count, list of reviews
- One review per booking (enforce with query check)

**Edge cases:**
- Customer tries to review before completion → block, show message
- Customer tries to review twice → show "Already reviewed" message
- Very long comment → cap at 1000 chars
- Artisan with 0 reviews → show "No reviews yet" instead of 0 stars

**Deliverables:** Review flow works. Ratings aggregate correctly. Visible on artisan profile.

### 4.3 Push Notifications

**Tasks:**
- Integrate Firebase Cloud Messaging (FCM)
- Store FCM token in user's profile document
- Notification triggers (via Appwrite Functions or client-side logic for MVP):
  - New job posted nearby (artisan)
  - New bid received (customer)
  - Booking accepted (customer)
  - New chat message (both)
  - Booking completed (both)
  - Job reminder (artisan, 1hr before)
- Handle notification tap → deep link to relevant screen
- `POST_NOTIFICATIONS` runtime permission (Android 13+)

**Setup required:**
1. Firebase project → add `google-services.json` to `app/`
2. Add Firebase BOM + messaging dependencies
3. Create `ArtisansXFirebaseService : FirebaseMessagingService`

**Deliverables:** Notifications arrive for key events. Tapping opens correct screen.

---

## 5. Phase 4 — AI Integration & Polish

**Goal:** Embed Groq/OpenRouter AI in 3 meaningful features. Polish the entire UX.

### 5.1 AI: Smart Job Description Generator

**Where:** `PostJobScreen` — button labelled "✨ Help me describe this job"

**How it works:**
1. Customer enters a rough/short job description
2. Taps the AI button
3. App sends to Claude: system prompt explaining it's an artisan marketplace in SA, user's rough description, job category
4. Claude returns a clear, structured job description
5. Shown in a dialog/bottom sheet with "Use this" and "Edit" buttons
6. Customer can edit before posting

**Prompt template:**
```
You are a helpful assistant for ArtisansX, a South African artisan services marketplace.
The customer needs help writing a clear job description.

Category: {category}
Their rough description: "{userInput}"

Write a clear, professional job description in 3-5 sentences. Include:
- What needs to be done
- Any relevant details a skilled artisan would need
- Keep it in simple English accessible to South African users
Do not include pricing. Be specific but concise.
```

**AI documentation note (for submission):** This feature uses Claude's language generation to help customers who may not be confident writers create professional job descriptions, improving the quality of job postings and helping artisans understand requirements better.

### 5.2 AI: Artisan Bid Helper

**Where:** `BidSubmitScreen` — button labelled "✨ Get AI suggestion"

**How it works:**
1. Artisan views a job they want to bid on
2. Taps the AI helper button
3. App sends: job details (title, category, description, budget if any), artisan's trade/skills
4. Claude suggests: fair price range (in ZAR), a professional message template
5. Artisan reviews, edits, and submits their bid

**AI documentation note:** This helps entry-level artisans (especially students) who may not know how to price their services or write professional client-facing messages, reducing a key barrier to marketplace participation.

### 5.3 AI: Job-Artisan Matching

**Where:** `JobDetailScreen` (customer view) — section "Suggested Artisans"

**How it works:**
1. When customer views their posted job, app fetches nearby artisans in the same trade category
2. Sends to Groq/OpenRouter API: job details + list of artisan summaries (name, skills, rating, review count, distance, years experience)
3. AI ranks top 3-5 matches with a one-line explanation for each
4. Shown as a "Recommended for this job" card list
5. Customer can tap to view artisan profile or invite them

**AI documentation note:** This feature uses AI to analyse multiple factors (skills match, reputation, proximity) to suggest the best artisans for a specific job, providing value beyond simple filtering that the app's own logic couldn't easily replicate.

### 5.4 UI Polish & Hardening

**Tasks:**
- Loading skeletons (shimmer effect) instead of spinners where possible
- Empty state illustrations for: no jobs, no bids, no messages, no reviews
- Animated transitions between screens (shared element transitions where Compose supports)
- Pull-to-refresh on all list screens
- Snackbar for success actions ("Job posted!", "Bid submitted!")
- Confirm dialogs for destructive actions (delete job, cancel booking)
- Input field error messages (inline, red, below field)
- Consistent use of the app's color theme, typography scale
- Test on: small phone (API 26), medium phone (API 34), tablet (landscape)

---

## 6. Phase 5 — Post-MVP / Future

These are NOT implemented in the academic submission but documented for completeness:

- **In-app payments** (escrow via payment gateway — PayFast/Yoco for SA)
- **Pro subscriptions** for artisans (more credits, priority listing, verified badge)
- **Admin web panel** (Appwrite console serves as interim admin panel)
- **AI job matching automation** (auto-suggest on job creation, not just on-demand)
- **Insurance/guarantees** integration
- **Micro-learning modules** (education pillar from concept doc)
- **Credit-linked education incentives** (earn credits for completing modules)
- **iOS version** (Kotlin Multiplatform or separate Swift/SwiftUI app)
- **Backend proxy for AI calls** (move Anthropic API key server-side)
- **Advanced search** (full-text search via Appwrite, filters by rating, distance, price range)

---

## 7. Database Schema Reference

### Collections Overview

```
artisansx_db/
├── user_profiles        — Customer data
├── artisan_profiles     — Artisan data (student + independent)
├── jobs                 — Job postings
├── bids                 — Artisan bids on jobs
├── bookings             — Confirmed bookings
├── chat_messages        — Chat within bookings
├── reviews              — Post-job ratings
└── credits              — Artisan credit balances
```

### Attribute Details

#### `user_profiles`
| Attribute       | Type     | Required | Notes                    |
|----------------|----------|----------|--------------------------|
| userId         | string   | ✅       | Appwrite Auth user ID     |
| fullName       | string   | ✅       |                          |
| email          | string   | ✅       |                          |
| phone          | string   | ❌       |                          |
| addresses      | string[] | ❌       | Saved addresses          |
| profileImageId | string   | ❌       | Storage file ID          |
| role           | string   | ✅       | "customer"               |
| createdAt      | datetime | ✅       |                          |

#### `artisan_profiles`
| Attribute          | Type     | Required | Notes                            |
|-------------------|----------|----------|----------------------------------|
| userId            | string   | ✅       | Appwrite Auth user ID             |
| fullName          | string   | ✅       |                                  |
| email             | string   | ✅       |                                  |
| phone             | string   | ✅       |                                  |
| isStudent         | boolean  | ✅       |                                  |
| institutionName   | string   | ❌       | Student path only                |
| studentNumber     | string   | ❌       | Student path only                |
| studentCardFileId | string   | ❌       | Storage file ID                  |
| courseField       | string   | ❌       | Student path only                |
| gradYear          | integer  | ❌       | Student path only                |
| idFileId          | string   | ❌       | Independent path                 |
| tradeCategory     | string   | ✅       | Plumbing, Electrical, etc.       |
| skills            | string   | ✅       | Free text description            |
| serviceArea       | string   | ✅       | e.g. "Durban North"             |
| serviceRadiusKm   | float    | ✅       | Default 15                       |
| latitude          | float    | ✅       | Service area center              |
| longitude         | float    | ✅       | Service area center              |
| workPhotoIds      | string[] | ❌       | Storage file IDs                 |
| certifications    | string   | ❌       |                                  |
| yearsExperience   | integer  | ❌       |                                  |
| verified          | boolean  | ✅       | Default false, admin approves    |
| badge             | string   | ✅       | "Student Artisan" / "Verified Artisan" |
| avgRating         | float    | ✅       | Default 0.0                      |
| reviewCount       | integer  | ✅       | Default 0                        |
| createdAt         | datetime | ✅       |                                  |

#### `jobs`
| Attribute         | Type     | Required | Notes                                 |
|------------------|----------|----------|---------------------------------------|
| customerId       | string   | ✅       | User who posted                       |
| title            | string   | ✅       | 5-100 chars                           |
| category         | string   | ✅       | From predefined list                  |
| description      | string   | ✅       | 20-2000 chars                         |
| photoIds         | string[] | ❌       | Storage file IDs                      |
| latitude         | float    | ✅       |                                       |
| longitude        | float    | ✅       |                                       |
| address          | string   | ✅       |                                       |
| budget           | float    | ❌       | In ZAR                                |
| urgency          | string   | ✅       | "standard" \| "urgent"                |
| jobType          | string   | ✅       | "standard" \| "bidding"               |
| status           | string   | ✅       | "open" \| "assigned" \| "in_progress" \| "completed" \| "cancelled" |
| assignedArtisanId| string   | ❌       | Set when booking created              |
| createdAt        | datetime | ✅       |                                       |

#### `bids`
| Attribute      | Type     | Required | Notes                          |
|---------------|----------|----------|--------------------------------|
| jobId         | string   | ✅       |                                |
| artisanId     | string   | ✅       |                                |
| priceOffer    | float    | ✅       | In ZAR                         |
| message       | string   | ✅       | 10-500 chars                   |
| estimatedHours| float    | ✅       |                                |
| status        | string   | ✅       | "pending" \| "accepted" \| "rejected" |
| createdAt     | datetime | ✅       |                                |

#### `bookings`
| Attribute    | Type     | Required | Notes                                |
|-------------|----------|----------|--------------------------------------|
| jobId       | string   | ✅       |                                      |
| customerId  | string   | ✅       |                                      |
| artisanId   | string   | ✅       |                                      |
| status      | string   | ✅       | "requested" \| "accepted" \| "in_progress" \| "completed" |
| startedAt   | datetime | ❌       |                                      |
| completedAt | datetime | ❌       |                                      |
| isPaid      | boolean  | ✅       | Default false                        |
| createdAt   | datetime | ✅       |                                      |

#### `chat_messages`
| Attribute    | Type     | Required | Notes                |
|-------------|----------|----------|----------------------|
| bookingId   | string   | ✅       |                      |
| senderId    | string   | ✅       |                      |
| message     | string   | ✅       |                      |
| imageFileId | string   | ❌       | Storage file ID      |
| createdAt   | datetime | ✅       |                      |

#### `reviews`
| Attribute   | Type     | Required | Notes           |
|------------|----------|----------|-----------------|
| bookingId  | string   | ✅       |                 |
| customerId | string   | ✅       |                 |
| artisanId  | string   | ✅       |                 |
| rating     | integer  | ✅       | 1-5             |
| comment    | string   | ✅       | Max 1000 chars  |
| createdAt  | datetime | ✅       |                 |

#### `credits`
| Attribute   | Type     | Required | Notes            |
|------------|----------|----------|------------------|
| artisanId  | string   | ✅       |                  |
| balance    | integer  | ✅       | Default 5 (free) |
| lastUpdated| datetime | ✅       |                  |

### Required Indexes

| Collection         | Index Fields                | Type      |
|-------------------|-----------------------------|-----------|
| jobs              | status                      | key       |
| jobs              | category                    | key       |
| jobs              | customerId                  | key       |
| jobs              | status, category            | key       |
| bids              | jobId                       | key       |
| bids              | artisanId                   | key       |
| bids              | jobId, artisanId            | unique    |
| bookings          | customerId                  | key       |
| bookings          | artisanId                   | key       |
| bookings          | jobId                       | key       |
| chat_messages     | bookingId                   | key       |
| chat_messages     | bookingId, createdAt        | key       |
| reviews           | artisanId                   | key       |
| reviews           | bookingId                   | unique    |
| artisan_profiles  | tradeCategory               | key       |
| artisan_profiles  | userId                      | unique    |
| user_profiles     | userId                      | unique    |
| credits           | artisanId                   | unique    |

---

## 8. AI Feature Documentation

This section serves as the required documentation for AI integration in the app.

### Feature 1: Smart Job Description Generator

- **Location in app:** PostJobScreen → "✨ Help me describe this job" button
- **AI model:** Llama-based models (via Groq) or available models via OpenRouter
- **Purpose:** Helps customers (who may lack confidence in English writing) create clear, professional job descriptions from rough notes
- **How user interacts:** Customer types rough description → taps button → sees AI-improved version in bottom sheet → can "Use this", "Edit", or "Dismiss"
- **Prompt design:** System prompt establishes SA marketplace context; user message includes category + rough description; response constrained to 3-5 sentences
- **Fallback:** If API fails, user keeps their original text and can post manually
- **Why AI (not rule-based):** Natural language understanding of vague descriptions ("my geyser is broken and the ceiling is wet") requires contextual reasoning that rules can't handle

### Feature 2: Artisan Bid Helper

- **Location in app:** BidSubmitScreen → "✨ Get AI suggestion" button
- **AI model:** Claude Sonnet via Anthropic Messages API
- **Purpose:** Helps entry-level/student artisans price their services fairly and write professional messages to customers
- **How user interacts:** Artisan views job → taps helper → sees suggested price range (ZAR) + message template → edits and submits
- **Prompt design:** System prompt includes SA market context; user message includes job details + artisan skills/experience; response structured as JSON with priceRange and messageTemplate
- **Fallback:** If API fails, artisan fills in bid manually (fields remain empty, not broken)
- **Why AI:** Pricing involves contextual factors (job complexity, SA market rates, urgency) that vary too much for a lookup table

### Feature 3: Job-Artisan Matching

- **Location in app:** JobDetailScreen (customer view) → "Suggested Artisans" section
- **AI model:** Claude Sonnet via Anthropic Messages API
- **Purpose:** Ranks available artisans for a specific job considering multiple weighted factors
- **How user interacts:** Customer opens their job → sees "Suggested Artisans" card with ranked list + one-line explanations → can tap to view profile or invite
- **Prompt design:** System prompt defines ranking criteria (skills match, rating, distance, experience); user message includes job details + JSON array of artisan summaries; response is ranked JSON array with explanations
- **Fallback:** If API fails, show artisans sorted by rating (simple fallback sort, no AI explanations)
- **Why AI:** Multi-factor ranking with natural-language skill matching (e.g., "general plumbing" matching "geyser specialist") requires semantic understanding

---

## 9. Security Considerations

| Concern | Mitigation |
|---------|------------|
| API keys in source | Stored in `local.properties`, read via `BuildConfig`. `.gitignore` includes `local.properties`. |
| Anthropic key on device | **Known limitation for academic MVP.** Documented in code. Production would use a backend proxy. |
| Appwrite document access | Document-level security enabled on all collections. Permissions restrict read/write per role. |
| User input injection | All text inputs sanitised (trim, length cap, no HTML). Appwrite handles query parameterisation. |
| File uploads | Restricted to images (MIME check), max 10MB, compressed client-side before upload. |
| Session management | Appwrite sessions with automatic expiry. App checks session validity on launch. |
| Location data | Only stored when user explicitly posts a job or sets service area. Not tracked continuously. |
| POPIA compliance | Users can delete their account and all associated data. Privacy policy screen (text) included. |
| Password requirements | Min 8 characters enforced client-side. Appwrite enforces server-side. |

---

## 10. Edge Cases & Error Handling Matrix

| Scenario | Expected Behaviour |
|----------|-------------------|
| No internet connection | Show offline banner. Cached profile data (DataStore) shown. All write operations show "No connection" error with retry. |
| Appwrite service down | Same as no internet — generic "Service unavailable" error. |
| Session expired mid-use | Detect 401 response → clear local session → redirect to login with "Session expired" message. |
| Empty job list | Show illustration + "No jobs yet. Check back soon!" (artisan) or "Post your first job!" (customer). |
| Empty bid list | "No bids yet. Your job is visible to artisans in the area." |
| Empty chat | "Send your first message to get started." |
| Duplicate bid attempt | Query check before insert. Show "You've already bid on this job." |
| Duplicate review attempt | Query check before insert. Show "You've already reviewed this booking." |
| Large image upload | Compress to 80% JPEG, max 2MB. If still too large, show "Image too large" error. |
| Location permission denied | Hide map features, allow manual address text entry. Show explanation of what they're missing. |
| Camera permission denied | Fall back to gallery picker only. |
| Notification permission denied (Android 13+) | App works fully, just no push notifications. Remind user once in settings. |
| AI API rate limited/failed | Show "AI assistant unavailable right now" message. User completes task manually. App never blocks on AI. |
| Artisan has 0 credits | "Insufficient credits" dialog with link to buy/earn more. Cannot unlock job. |
| Double-tap submit | Disable button immediately on first tap. Re-enable only on error. |
| Very long text input | Enforce `maxLength` on TextField. Show char counter (e.g., "1847/2000"). |
| Back navigation during form | Show "Discard changes?" confirmation dialog if form has unsaved data. |
| App killed during file upload | Upload lost. On return, form data preserved (rememberSaveable), file field shows "Upload failed — tap to retry." |

