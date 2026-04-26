# ArtisanX — Gap Analysis

Cross-reference of the README spec vs current implementation. Last updated 2026-04-26 (all P1/P2/P3 gaps closed).

---

## What Is Fully Implemented

- Auth: register, login, session persistence, logout
- Role selection → customer or artisan
- Artisan onboarding: 3-step form, student vs independent paths, file uploads
- Customer: post job with map pin, AI description generator, urgency/jobType/budget/category, up to 3 job photos
- Artisan: browse jobs with category filter, sort options, geocoded location display, map view toggle
- Bidding: submit (with AI bid helper), edit bid, list bids on a job, customer accept bid → booking created
- Bookings: status stepper, artisan accept/start/complete, Mark as Paid, Leave Review trigger, booking cancellation
- Chat: send/receive messages, photo attachments, SA timezone timestamps, receive-only notifications, 5s polling
- ChatListScreen: last-message preview per booking, wired to Messages tab for both roles
- Reviews: star rating, comment, avg rating + count aggregated on artisan profile
- Credits: balance display, 1-credit deduct on unlock, 2-credit deduct on bid, buy screen with test mode
- AI: job description generator (Groq), bid helper (Claude Sonnet), job-artisan matching (Claude Sonnet)
- FCM: token stored in profile, local notifications on new inbound chat message, notification tap → deep link to ChatScreen
- Location picker: map pin drop with debounced reverse geocoding, locale-safe coordinates
- Distance calculation: haversine displayed on job browse cards and map marker dialogs
- Navigate button: opens Google Maps directions from JobDetailScreen (artisan only)
- Char counters: job description (2000), bid message (500), review comment (1000), chat input (2000)
- Timestamps: `startedAt` written on `in_progress`, `completedAt` written on `completed`
- Session expiry / 401 detection: all 9 repositories emit `SessionEventBus`, MainActivity redirects to login
- Offline banner: `ConnectivityManager.NetworkCallback` in MainActivity, `OfflineBanner` composable
- Loading skeletons: shimmer `ShimmerBox`/`JobCardSkeleton`/`BookingCardSkeleton`/`ChatListSkeleton`
- Empty states with icons: all 4 list screens (CustomerDashboard, JobBrowse, Bookings, ChatList)
- Discard-changes dialogs: PostJobScreen, BidSubmitScreen
- Privacy policy screen: accessible from Login and Register screens (POPIA compliance)

---

## Priority 1 — All Resolved

### ✅ P1-A: Booking Cancellation

**Spec (README §3.4):** Cancel booking — only if status is `requested` or `accepted`.

**Resolution (2026-04-26):** Cancel button added to `BookingCard` (customer side), visible when status is `requested` or `accepted`. Fires `onUpdateStatus("cancelled")` after confirmation dialog. `BookingStatusStepper` returns early for cancelled bookings and shows a `Chip("Booking cancelled")` instead.

---

### ✅ P1-B: `startedAt` / `completedAt` Timestamps

**Spec (README §7 schema):** `bookings.startedAt` and `bookings.completedAt` are optional datetime fields.

**Resolution (2026-04-26):** `BookingRepositoryImpl.updateStatus()` writes `startedAt = ISO8601 UTC now` on `in_progress` transition, `completedAt = ISO8601 UTC now` on `completed` transition.

---

### ✅ P1-C: ChatListScreen (Messages Tab)

**Spec (README §4.1):** ChatListScreen — all active chats for the user, showing last message preview.

**Resolution (2026-04-26):** `ChatListScreen` + `ChatListViewModel` query all bookings for the current user, fetch the most recent message per booking, render as a list (other party name, last message preview, timestamp). Both `CustomerChatList` and `ArtisanChatList` routes in `AppNavGraph` point to this screen.

---

### ✅ P1-D: Sort Options on Job Browse

**Spec (README §2.6):** Sort by: newest, nearest (placeholder until maps), budget high→low.

**Resolution (2026-04-26):** Sort dropdown in `JobBrowseScreen` with three options: **Newest** (createdAt desc), **Budget: High→Low** (budget desc), **Urgent First** (urgent jobs first, then by budget). Note: "nearest" sort was replaced with "Urgent First" — haversine distance is computed and displayed on each card as `~X.X km` via `LocationUtils.haversineKm`, so proximity is visible without a separate sort.

---

## Priority 2 — All Resolved

### ✅ P2-A: Navigate Button (Google Maps Intent)

**Spec (README §3.1):** 'Navigate' button opens Google Maps app with directions intent.

**Resolution (2026-04-26):** `OutlinedButton("Navigate to Job Site")` on `JobDetailScreen`, visible to artisan only when job has non-zero coordinates. Fires `Intent(ACTION_VIEW, Uri.parse("geo:$lat,$lng?q=$lat,$lng($title)"))`. Falls back gracefully if Google Maps not installed.

---

### ✅ P2-B: Distance Calculation on Job Browse

**Spec (README §3.1):** Artisan sees distance to job.

**Resolution (2026-04-26):** `LocationUtils.haversineKm()` utility implemented. `JobBrowseViewModel.distanceKmFor(job)` computes distance from artisan's profile lat/lng to each job. Displayed on job browse cards and map marker dialog as `~X.X km` via `LocationUtils.formatDistanceKm()`.

---

### ✅ P2-C: Edit Bid

**Spec (README §3.3):** Artisan edits bid — allow only if status = `pending`.

**Resolution (2026-04-26):** `BidSubmitViewModel` detects existing bid in `init {}` block; if found and `status == "pending"`, sets `_isEditMode = true` and pre-populates fields. On submit, calls `biddingRepository.updateBid()` instead of create. Top bar title changes to "Edit Your Bid". Credit card hidden in edit mode (no credit cost for edits).

---

### ✅ P2-D: Deep Link from Notification Tap

**Spec (README §4.3):** Handle notification tap → deep link to relevant screen.

**Resolution (2026-04-26):** `ArtisansXFirebaseService` puts `EXTRA_BOOKING_ID` in the pending intent. `MainActivity.handleDeepLinkIntent()` reads it and calls `viewModel.setDeepLink(bookingId)`. `AppNavGraph` has a `LaunchedEffect(pendingBookingId)` that navigates to `ChatScreen(bookingId)` and calls `onDeepLinkConsumed()`. Works for both cold-start and foreground taps.

---

### ✅ P2-E: Char Counters on Text Fields

**Spec (README §10):** Show char counter (e.g., 1847/2000).

**Resolution (2026-04-26):**

| Field | Counter |
|-------|---------|
| Job description (`PostJobScreen`) | `supportingText = { Text("${description.length}/2000 — describe what needs doing") }` |
| Bid message (`BidSubmitScreen`) | `supportingText = { Text("${message.length}/500") }` |
| Review comment (`ReviewScreen`) | `supportingText = { Text("${comment.length}/1000") }` |
| Chat input (`ChatScreen`) | Counter appears at >1800 chars, red at limit |

---

### ✅ P2-F: Full Artisan Onboarding (Student vs Independent)

**Resolution (2026-04-26):** 3-step form with student/independent branching. Step 1: phone, trade category, service area, toggle. Step 2 (branched): student card / ID upload paths. Step 3: skills description (2000 char counter), up to 3 work photos. All uploads via `AppwriteFileUtils`.

---

### ✅ P2-G: Photo Upload in Chat

**Resolution (2026-04-26):** Attach icon opens image picker → `AppwriteFileUtils.uploadFromUri()` → `chatRepository.sendImageMessage()` stores `imageFileId`. `MessageBubble` renders `AsyncImage` from file view URL.

---

### ✅ P2-H: Job Photo Upload

**Resolution (2026-04-26):** Photo picker grid on `PostJobScreen` (up to 3). Upload via `AppwriteFileUtils`, stored in `jobs.photoIds`. `JobDetailScreen` displays photo row. Root cause of earlier failures (bucket missing `create` permission + empty `cachedUserId`) fixed.

---

### ✅ P2-I: Artisan Service Area Map in Profile

**Resolution (2026-04-26):** `LocationOn` icon button on Service Area field in `ArtisanProfileScreen` edit mode opens `LocationPickerScreen` inline. On selection, `editServiceArea`, `editLatitude`, `editLongitude` updated in `ProfileViewModel` and saved.

---

### ✅ P2-J: Map View Toggle on Job Browse

**Resolution (2026-04-26):** Map/List toggle icon in `JobBrowseScreen` top bar. Map renders `GoogleMap` with filtered markers. Tapping a marker shows an `AlertDialog` with job title, category, budget, address, distance, and "View Details" navigation.

---

## Priority 3 — All Resolved

| Item | Spec | Status |
|------|------|--------|
| Loading skeletons | Shimmer on list items while loading | ✅ `ShimmerBox`, `JobCardSkeleton`, `BookingCardSkeleton`, `ChatListSkeleton` on all 4 list screens |
| Empty state illustrations | Illustration + message | ✅ Icon + contextual message on CustomerDashboard, JobBrowse, Bookings, ChatList |
| "Discard changes?" dialogs | Confirm before back-navigating dirty forms | ✅ `PostJobScreen` and `BidSubmitScreen` with `BackHandler` + `AlertDialog` |
| Offline error handling | Offline banner, cached data shown | ✅ `ConnectivityManager.NetworkCallback` + `OfflineBanner` in MainActivity |
| Session expiry / 401 redirect | Detect 401 → clear session → login | ✅ All 9 repos check `isSessionExpired()` → `SessionEventBus` → MainActivity navigates to login |
| Privacy policy screen | POPIA §9 compliance | ✅ `PrivacyPolicyScreen` linked from Login and Register screens |

---

## Out of Scope (Phase 5 / Post-MVP)

Explicitly deferred in README §6 — do not implement for academic submission:
- In-app payments (PayFast/Yoco)
- Pro subscriptions for artisans
- Admin web panel
- Appwrite Realtime for chat (5s polling is acceptable for MVP)
- iOS version, backend AI proxy, advanced search, micro-learning modules

---

## Implementation Order — Final Status

| Priority | Item | Effort | Status |
|----------|------|--------|--------|
| P1-A | Booking cancellation button | XS | ✅ |
| P1-B | startedAt/completedAt timestamps | XS | ✅ |
| P1-C | ChatListScreen + wire Messages tab | M | ✅ |
| P1-D | Sort options on job browse | S | ✅ |
| P2-A | Navigate button (Maps intent) | XS | ✅ |
| P2-B | Distance calculation on job browse | S | ✅ |
| P2-C | Edit bid | S | ✅ |
| P2-D | Deep link from notification tap | S | ✅ |
| P2-E | Char counters on all text fields | S | ✅ |
| P2-F | Full artisan onboarding (student/independent) | ~~XL~~ | ✅ |
| P2-G | Photo upload in chat | ~~M~~ | ✅ |
| P2-H | Job photo upload | ~~M~~ | ✅ |
| P2-I | Artisan service area map in profile | S | ✅ |
| P2-J | Map view toggle on job browse | M | ✅ |
| P3 | UX polish (skeletons, dialogs, offline, 401, privacy) | L | ✅ |
