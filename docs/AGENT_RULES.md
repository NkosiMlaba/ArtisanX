# ArtisanX — Agent Implementation Rules

Reference this file when implementing any feature. These rules apply to all agents and sub-agents.

---

## 1. Think Holistically Before Writing a Single Line

Before implementing anything, answer these questions:

- **Who creates this data?** Which role (customer/artisan), which screen, which ViewModel method?
- **Who reads it?** Every screen or list that displays it — not just the one you're building.
- **Who updates it?** Status transitions, edits, approvals.
- **Who deletes it?** Confirmation dialogs, cascading side-effects (e.g. cancelling a booking reopens the job).
- **What does the other role see?** ArtisanX is a two-sided marketplace. If you build a screen for one role, ask what the other role sees or needs as a result.
- **What happens in the notification layer?** Does this action need to notify the other party? Does tapping that notification need to deep-link somewhere?
- **Does this affect the credits system?** Unlocking jobs costs 1, bidding costs 2. Any new artisan action that accesses paid content must check and deduct credits.
- **Does this affect booking or job status?** Status transitions cascade: accepting a bid → creates booking + marks job assigned + rejects other bids. Completing a booking → marks job completed. Cancelling → reverts job to open.

---

## 2. Full CRUD Is the Baseline, Not the Goal

Every entity in the schema needs:

| Operation | What to check |
|-----------|---------------|
| **Create** | Input validation (length, format, required fields). Disable button on submit. Show snackbar on success/error. |
| **Read** | Loading state, error state, empty state (text + icon, never just blank). Pull-to-refresh on list screens. |
| **Update** | Only allowed at valid statuses (e.g. edit bid only if `pending`, edit job only if `open`). Pre-populate form with existing values. No credits deducted for edits. |
| **Delete** | Confirmation `AlertDialog` before destructive action. Cascade any dependent state (see §1). Navigate back on success. |
| **Status transitions** | Timestamp fields (`startedAt`, `completedAt`, `createdAt`) must be written when status changes. Cascading updates to related collections must happen in the same repository method. |

---

## 3. Every New Screen Must Be Wired End-to-End

A screen is **not done** until all of these are true:

1. `Screen.kt` — route object added with correct path and `createRoute()` helper if it takes args.
2. `AppNavGraph.kt` — `composable(route = ...)` block added.
3. **Entry point** — at least one button/nav item in an existing screen navigates to it.
4. **Back navigation** — `navController.popBackStack()` or `NavigateBack` UiEvent wired up.
5. **Bottom nav** — if this is a tab-level screen, add it to the relevant `BottomNavItem` list. Use role-prefixed routes (`customer_*`, `artisan_*`) so the existing `isCustomerFlow`/`isArtisanFlow` detection in `AppNavGraph` keeps working.
6. **SnackbarHostState** — passed in if the screen needs to show messages.

---

## 4. Architecture Patterns — Never Deviate

| Layer | Rule |
|-------|------|
| **Repository** | Every Appwrite call wrapped in `try-catch`, returns `Resource<T>`. Never return raw data or throw. |
| **ViewModel** | Exposes `State<T>` (not Flow) for UI state. `MutableSharedFlow<UiEvent>` for one-shot events (snackbar, navigate). `viewModelScope.launch` for all coroutines. |
| **Screen** | Collects UiEvents via `LaunchedEffect(key1 = true) { uiEvent.collectLatest { ... } }`. Never calls repository directly. |
| **Hilt** | New repository implementations must be bound in `RepositoryModule`. New ViewModels get `@HiltViewModel`. New screens use `hiltViewModel()`. |
| **Navigation args** | Read via `savedStateHandle.get<String>("argName")` in ViewModel. Never read from the composable. |
| **Icons** | `material-icons-extended` is now a dependency — any Material icon is available. Use `Icons.Default.*`, `Icons.Filled.*`, `Icons.Outlined.*`, or `Icons.AutoMirrored.Filled.*`. |

---

## 5. Cross-Cutting Concerns — Always Check

**Notifications**
- New inbound chat messages → notify receiver (done in `ChatViewModel.refreshMessages`, not in repository).
- Booking status changes → notify the other party (done in `BookingRepositoryImpl.updateBookingStatus`).
- Bid accepted → notify artisan (done in `BiddingRepositoryImpl.acceptBid`).
- All calls to `showLocalNotification` should pass `bookingId` where relevant so the notification tap deep-links to the correct `ChatScreen`.

**Navigation / Deep Links**
- Notification tap passes `bookingId` as `Intent` extra → `MainActivity` → `MainViewModel.setDeepLink()` → `AppNavGraph` LaunchedEffect navigates to `Screen.Chat`.
- When adding new notification types that should deep-link to a different screen, extend `ArtisansXFirebaseService.EXTRA_BOOKING_ID` pattern with a new extra and handle in `MainActivity.handleDeepLinkIntent`.

**Cascades on booking/job state**
- Accept bid → update bid to `accepted`, reject all other pending bids, set job `assigned` + `assignedArtisanId`, create `bookings` document.
- Cancel booking → set booking `cancelled`, revert job to `open`, clear `assignedArtisanId`.
- Complete booking → set booking `completed` + write `completedAt`, set job `completed`.
- All cascades live in the **repository layer**, never in ViewModels.

**Credits**
- Unlock job detail: 1 credit deducted before fetching full details.
- Submit bid: 2 credits deducted before creating document. Edit bid: no cost.
- On insufficient credits, emit snackbar and return early — never proceed and fail.
- New artisan registration: initialise `credits` document with balance = 5.

**Char counters**
- Every `OutlinedTextField` with a length limit must show `"${text.length}/limit"` in `supportingText`.
- Job description: 20–2000. Bid message: 10–500. Review comment: 1000. Chat: 2000 (shown only after 1800 chars).

---

## 6. Testing Protocol — Feature Is Not Done Until Verified on Device

Follow this sequence for every feature:

### Step 1 — Build
```bash
./gradlew assembleDebug 2>&1 | grep -E "^e:|BUILD|FAILED"
```
Fix all compile errors before continuing.

### Step 2 — Deploy
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.example.artisanx/.MainActivity
```

### Step 3 — Exercise the full flow
- Test the **happy path** (valid inputs, correct role, sufficient credits).
- Test the **other role's view** — what does the other party see after this action?
- Test **empty states** — what shows when the list is empty?
- Test **error states** — what shows if the backend call fails?
- Test **edge cases** specific to the feature (duplicate bid, cancel already-completed booking, etc.).

### Step 4 — Read logs
```bash
adb logcat -s AndroidRuntime:E System.err:W com.example.artisanx:D | head -100
```
Look for uncaught exceptions, Appwrite errors, permission denials.

### Step 5 — Ask if blocked
If a flow can't be exercised without data (e.g. testing bid acceptance requires a posted job and a submitted bid), ask the user to:
- Use the test accounts (`customer_test@artisanx.dev` / `artisan_test@artisanx.dev`, password `TestPass123!`).
- Or describe what state needs to be set up.

---

## 7. Data Model Completeness

Every attribute in the Appwrite schema (see README §7) must be either:
- **Displayed** somewhere in the UI, or
- **Documented** in `GAP_ANALYSIS.md` as a known gap with a reason.

Fields that exist in the schema but are silently ignored are bugs, not gaps. Current known un-wired fields:
- `jobs.photoIds` — no photo upload in PostJobScreen yet (P2-H)
- `artisan_profiles.workPhotoIds`, `.studentCardFileId`, `.idFileId` — full onboarding not built yet (P2-F)
- `artisan_profiles.latitude`, `.longitude`, `.serviceRadiusKm` — set during onboarding but no map picker on profile edit yet (P2-I)
- `chat_messages.imageFileId` — no image send in ChatScreen yet (P2-G)

---

## 8. Scope Control

- **Do not** add features, UI states, or abstractions not asked for.
- **Do** consider whether the feature you're asked to build has implications for other screens, and implement those implications in the same session.
- If implications are large (e.g. a data model change that affects 5 screens), list them explicitly and ask before implementing all of them.
- **Do not** leave a feature half-wired. If a ViewModel method exists but no screen calls it, or a screen has a button that navigates nowhere, that is a broken feature, not a partial one.

---

## 9. File and Naming Conventions

| Concern | Convention |
|---------|-----------|
| New screen | `presentation/<feature>/<Name>Screen.kt` + `<Name>ViewModel.kt` |
| New repository method | Add to interface first, then implement |
| New domain model | `domain/model/<Name>.kt` with a `toX()` extension on `Map<String, Any>` |
| New constant (collection ID, etc.) | Add to `util/Constants.kt` |
| New utility | `util/<Name>Utils.kt` as an `object` |
| Routes | `Screen.kt` — use role prefix for tab screens (`customer_*`, `artisan_*`) |

---

## 10. What "Done" Means

A feature is done when:

- [ ] It builds without errors or warnings introduced by your changes
- [ ] The happy path works end-to-end on the physical device
- [ ] The other role's perspective is handled (what they see, what they're notified of)
- [ ] Status/cascade side-effects are implemented in the repository layer
- [ ] Empty state, loading state, and error state are all present
- [ ] `GAP_ANALYSIS.md` is updated if the feature closes a gap or reveals a new one
- [ ] No half-implemented stubs left behind (no `TODO`, no disabled buttons that go nowhere, no routes registered but never navigated to)
