# ArtisanX — Gap Analysis

Cross-reference of the README spec vs current implementation. Generated after Phase 5 (Maps, FCM, Credits) was completed.

---

## What Is Fully Implemented

- Auth: register, login, session persistence, logout
- Role selection → customer or artisan
- Artisan onboarding (simplified single-screen — see gap #10)
- Customer: post job with map pin, AI description generator, urgency/jobType/budget/category
- Artisan: browse jobs with category filter and geocoded location display
- Bidding: submit (with AI bid helper), list bids on a job, customer accept bid → booking created
- Bookings: status stepper, artisan accept/start/complete, Mark as Paid, Leave Review trigger
- Chat: send/receive messages, SA timezone timestamps, receive-only notifications, 5s polling
- Reviews: star rating, comment, avg rating + count aggregated on artisan profile
- Credits: balance display, 1-credit deduct on unlock, 2-credit deduct on bid, buy screen with test mode
- AI: job description generator (Groq), bid helper (Claude Sonnet), job-artisan matching (Claude Sonnet)
- FCM: token stored in profile, local notifications on new inbound chat message
- Location picker: map pin drop with debounced reverse geocoding, locale-safe coordinates

---

## Priority 1 — Broken / Incomplete Flows

These are gaps where a flow either doesn't work at all or is missing a critical step.

### P1-A: Booking Cancellation

**Spec (README §3.4):** "Cancel booking — only if status is `requested` or `accepted`, not `in_progress`."

**Current state:** `BookingsScreen` has no cancel button. `BookingsViewModel.updateStatus()` already accepts any status string, so the repository layer is ready.

**What to add:** Cancel button in `BookingCard` (customer side only, visible when status is `requested` or `accepted`), calls `onUpdateStatus("cancelled")`. Also add "cancelled" to `BookingStatusStepper` or hide the stepper for cancelled bookings.

---

### P1-B: `startedAt` / `completedAt` Timestamps

**Spec (README §7 schema):** `bookings.startedAt` and `bookings.completedAt` are optional datetime fields.

**Current state:** `BookingsViewModel.updateStatus()` patches only the `status` field. Neither timestamp is ever written.

**What to add:** When status transitions to `in_progress`, write `startedAt = now`. When status transitions to `completed`, write `completedAt = now`. Use the same ISO-8601 UTC format as `ChatRepositoryImpl.getCurrentIso8601Date()`.

---

### P1-C: ChatListScreen (Messages Tab)

**Spec (README §4.1):** "ChatListScreen — all active chats for the user, showing last message preview."

**Current state:** The Messages tab in both customer and artisan bottom nav leads nowhere (placeholder). Chat is only reachable from `BookingsScreen → Open Chat`.

**What to add:**
- `ChatListScreen`: queries all bookings for the current user, for each booking fetches the most recent chat message, renders as a list (other party name, last message preview, timestamp). Tap → navigate to `ChatScreen`.
- Route the Messages tab in `AppNavGraph` to this screen.

---

### P1-D: Sort Options on Job Browse

**Spec (README §2.6):** "Sort by: newest, nearest (placeholder until maps), budget high→low."

**Current state:** `JobBrowseScreen` has category filter chips but no sort selector. Jobs appear in Appwrite's default insertion order.

**What to add:** A sort dropdown or chip row below the category filter. Client-side sort on the already-loaded list: newest (by `createdAt` desc), budget (by `budget` desc, nulls last). "Nearest" sorts by stored lat/lng distance to artisan's location — can use haversine on the already-available coordinates.

---

## Priority 2 — Specified Features Not Yet Built

### P2-A: Navigate Button (Google Maps Intent)

**Spec (README §3.1):** "'Navigate' button opens Google Maps app with directions intent."

**Current state:** Job lat/lng is stored and shown on the map in `JobDetailScreen`, but no navigate button exists.

**What to add:** `OutlinedButton("Navigate")` on `JobDetailScreen` (visible to artisan only). Fires `Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lng?q=$lat,$lng"))`. Falls back gracefully if Google Maps is not installed.

---

### P2-B: Distance Calculation on Job Browse

**Spec (README §3.1):** "Customer sees artisan's distance from job location" / artisan sees distance to job.

**Current state:** Both artisan profile and job have lat/lng stored. Neither browse screen computes or displays distance.

**What to add:** `LocationUtils.haversineKm(lat1, lng1, lat2, lng2): Double` utility. On `JobBrowseScreen`, once the artisan's profile is loaded, compute distance from artisan's service area center to each job and display as "~3.2 km" in the job card.

---

### P2-C: Edit Bid

**Spec (README §3.3):** "Artisan edits bid — allow only if status = `pending`."

**Current state:** No edit UI. Once submitted, a bid cannot be changed.

**What to add:** Edit icon/button on the artisan's bid card (in `BidSubmitScreen` or a new view on `JobDetailScreen` for artisans who already bid). Pre-populates price, message, hours. Calls `BiddingRepository.updateBid()`.

---

### P2-D: Deep Link from Notification Tap

**Spec (README §4.3):** "Handle notification tap → deep link to relevant screen."

**Current state:** `ArtisansXFirebaseService` receives messages but notification tap just opens `MainActivity` with no routing.

**What to add:** In `AndroidManifest`, add intent filter or use `getIntent().extras` in `MainActivity` to read the `bookingId` from the notification data payload and navigate to `ChatScreen` or `BookingsScreen` accordingly.

---

### P2-E: Char Counters on Text Fields

**Spec (README §10):** "Show char counter (e.g., 1847/2000)."

**Current state:** No counters on any field.

**Fields that need counters:**
- Job description: 20–2000
- Bid message: 10–500
- Review comment: max 1000
- Chat input: max 2000

**What to add:** `supportingText` on each `OutlinedTextField` showing `"${text.length}/limit"`.

---

### P2-F: Full Artisan Onboarding (Student vs Independent)

**Spec (README §3.2):** Multi-step form with two distinct paths:
- **Student:** student card photo upload, institution, student number, course, grad year
- **Independent:** ID upload, work photos (required), certifications, years experience

**✅ RESOLVED (2026-04-26):** Replaced with a 3-step form:
- Step 1: Phone, trade category (dropdown), service area, student/independent toggle
- Step 2 (branches): Student → institution/number/course/grad year/student card upload | Independent → years experience/certifications/ID document upload
- Step 3: Skills description (char counter, 2000 max), up to 3 work photos uploaded to Appwrite Storage

All file uploads go to `BUCKET_ARTISANSX_FILES` via `Storage.createFile`. `createArtisanProfile` now stores all identity and portfolio fields in the Appwrite document at creation time.

---

### P2-G: Photo Upload in Chat

**Spec (README §4.1):** "Send photos (camera/gallery → upload to storage → store fileId in message)."

**Current state:** `chat_messages.imageFileId` exists in schema but `ChatScreen` has only text input.

**What to add:** Image attach button in chat input row. `ActivityResultContracts.GetContent` picker → upload to `artisansx_files` bucket → store `fileId` in message document. Render image messages with `AsyncImage` (Coil) in `MessageBubble`.

---

### P2-H: Job Photo Upload

**Spec (README §7):** `jobs.photoIds` string array in schema. README §5.4 implies this is part of job posting UX.

**Current state:** `PostJobScreen` has no image picker. `photoIds` always empty.

**What to add:** Optional image attach row in `PostJobScreen`. Upload to storage, store IDs in job document. Display in `JobDetailScreen`.

---

### P2-I: Artisan Service Area Map in Profile

**Spec (README §3.1):** "On `ArtisanProfile`: artisan sets their service area center + radius on a map."

**Current state:** Profile edit has a text field for `serviceArea` and numeric field for radius, but no map picker.

**What to add:** "Set on map" button on artisan profile edit → opens `LocationPickerScreen` → stores returned lat/lng + address.

---

### P2-J: Map View Toggle on Job Browse

**Spec (README §3.1):** "Add a 'Map View' toggle — shows jobs as markers on map."

**Current state:** List-only browse.

**What to add:** Toggle icon in `JobBrowseScreen` top bar. Map view renders a `GoogleMap` with a `Marker` per job, tapping a marker shows a bottom sheet with the job card.

---

## Priority 3 — UX Polish (Phase 4.4)

These match README §5.4 and §10.

| Item | Spec | Current State |
|------|------|---------------|
| Loading skeletons | Shimmer on list items while loading | `CircularProgressIndicator` everywhere |
| Empty state illustrations | Illustration + message | Text-only ("No jobs yet.") |
| "Discard changes?" dialogs | Confirm before back-navigating dirty forms | No confirmation anywhere |
| Offline error handling | Offline banner, cached data shown | Generic error text |
| Session expiry / 401 redirect | Detect 401 → clear session → login with "Session expired" | 401 surfaces as generic error |
| Privacy policy screen | POPIA §9 compliance | Not implemented |

---

## Out of Scope (Phase 5 / Post-MVP)

Explicitly deferred in README §6 — do not implement for academic submission:
- In-app payments (PayFast/Yoco)
- Pro subscriptions for artisans
- Admin web panel
- Appwrite Realtime for chat (5s polling is acceptable for MVP)
- iOS version, backend AI proxy, advanced search, micro-learning modules

---

## Implementation Order

| Priority | Item | Effort |
|----------|------|--------|
| P1-A | Booking cancellation button | XS |
| P1-B | startedAt/completedAt timestamps | XS |
| P1-C | ChatListScreen + wire Messages tab | M |
| P1-D | Sort options on job browse | S |
| P2-A | Navigate button (Maps intent) | XS |
| P2-B | Distance calculation on job browse | S |
| P2-C | Edit bid | S |
| P2-D | Deep link from notification tap | S |
| P2-E | Char counters on all text fields | S |
| P2-F | Full artisan onboarding (student/independent) | ~~XL~~ ✅ |
| P2-G | Photo upload in chat | M |
| P2-H | Job photo upload | M |
| P2-I | Artisan service area map in profile | S |
| P2-J | Map view toggle on job browse | M |
| P3 | UX polish (skeletons, dialogs, offline, 401) | L |
