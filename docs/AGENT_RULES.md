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

## 6. Testing Protocol — MANDATORY After Every Feature

**This is not optional.** Every session that writes or modifies code MUST end with a successful build, a running app on device, and a log review. Do not report a feature as done until all four steps below pass.

### Step 1 — Build
```bash
./gradlew assembleDebug 2>&1 | grep -E "^e:|error:|BUILD|FAILED"
```
Fix **all** compile errors and warnings introduced by your changes before continuing. Do not skip past a failed build.

### Step 2 — Deploy and launch
`adb` is not on the system PATH. Always use the full path via PowerShell:
```powershell
$adb = "C:\Users\nkosi\AppData\Local\Android\Sdk\platform-tools\adb.exe"
& $adb install -r "app\build\outputs\apk\debug\app-debug.apk"
& $adb shell am start -n com.example.artisanx/.MainActivity
```
If no device is connected (`adb devices` shows empty), report this clearly and ask the user to connect one — do **not** skip this step silently.

### Step 3 — Monitor logs while the app runs
Clear the buffer, wait ~8 seconds, then dump:
```powershell
$adb = "C:\Users\nkosi\AppData\Local\Android\Sdk\platform-tools\adb.exe"
& $adb logcat -c
Start-Sleep -Seconds 8
& $adb logcat -d 2>&1 | Select-String -Pattern "FATAL|artisanx|Appwrite|Exception|Error|crash" | Select-Object -First 150
```
Specifically look for:
- Uncaught exceptions / crashes (`FATAL EXCEPTION`)
- Appwrite SDK errors (4xx/5xx responses, permission denials)
- Missing navigation routes (`IllegalArgumentException`)
- Null pointer / class cast issues at runtime

Report what you found — even "no errors in logs" is a useful signal that must be stated explicitly.

### Step 4 — Exercise the affected flows
Navigate to every screen touched by the feature and verify:
- **Happy path**: valid inputs, correct role, sufficient credits.
- **Other role's view**: what does the other party see after this action?
- **Empty state**: what shows when the list is empty?
- **Error state**: what shows if the backend call fails?
- **Edge cases** specific to the feature (duplicate bid, cancel completed booking, etc.).

If a flow requires specific data that isn't present (e.g. an accepted bid), ask the user to set it up using the test accounts (`customer_test@artisanx.dev` / `artisan_test@artisanx.dev`, password `TestPass123!`) rather than skipping the test.

### Step 5 — Report results before moving on
After testing, write a short summary:
```
✅ Build: passed
✅ Install: success
✅ Logs: no crashes or errors
✅ Flows tested: [list what was navigated]
⚠️  Known issue: [anything observed but not fixed in this session]
```
Do not proceed to the next feature until this summary is written.

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

## 10. Brand Color Palette — Always Use These, Never Default Material Colors

`dynamicColor` is **disabled**. Do not re-enable it. All screens use the tokens below.

### Light scheme (default)
| Token | Value | Usage |
|-------|-------|-------|
| `primary` | `#5840C3` (indigo) | Buttons, FABs, active nav indicators, progress bars |
| `onPrimary` | `#FFFFFF` | Text/icons on primary surfaces |
| `primaryContainer` | `#E6E0FF` | Chip backgrounds, highlighted cards |
| `onPrimaryContainer` | `#1A0065` | Text on primaryContainer |
| `secondary` | `#F8DA5D` (gold) | Accent chips, badges, star ratings, secondary buttons |
| `onSecondary` | `#1A1200` | Text on secondary surfaces |
| `secondaryContainer` | `#FFF8C5` | Light gold backgrounds |
| `tertiary` | `#007A77` (teal) | Success states, "active"/"verified" badges |
| `onTertiary` | `#FFFFFF` | Text on teal surfaces |
| `background` | `#FFFBFF` | Screen backgrounds |
| `surface` | `#FFFFFF` | Cards, sheets, dialogs |
| `surfaceVariant` | `#E6E0F0` | Input field fills, dividers |
| `outline` | `#7A757F` | Input borders, dividers |
| `error` | `#BA1A1A` | Error messages, destructive action indicators |

### Dark scheme
| Token | Value |
|-------|-------|
| `primary` | `#CBBEFF` |
| `onPrimary` | `#2C0095` |
| `primaryContainer` | `#4227AA` |
| `secondary` | `#E3C64B` |
| `background` | `#1C1B1F` |
| `surface` | `#1C1B1F` |

### Logo usage
- `R.drawable.artisanx_logo` — transparent background, use on any colored surface.
- `R.drawable.artisanx_logo_bg` — indigo background version, use on white/light surfaces.
- Display logo at `120.dp` on auth screens and the splash/loading screen.
- Do **not** use the old text-only "ArtisanX" heading by itself — always pair text with the logo image.

### Code reference
Colors are defined in `ui/theme/Color.kt`. Scheme wiring is in `ui/theme/Theme.kt`. Reference via `MaterialTheme.colorScheme.*` — never hardcode hex values in composables.

---

## 12. What "Done" Means

A feature is done when:

- [ ] It builds without errors or warnings introduced by your changes
- [ ] The happy path works end-to-end on the physical device
- [ ] The other role's perspective is handled (what they see, what they're notified of)
- [ ] Status/cascade side-effects are implemented in the repository layer
- [ ] Empty state, loading state, and error state are all present
- [ ] `GAP_ANALYSIS.md` is updated if the feature closes a gap or reveals a new one
- [ ] No half-implemented stubs left behind (no `TODO`, no disabled buttons that go nowhere, no routes registered but never navigated to)
