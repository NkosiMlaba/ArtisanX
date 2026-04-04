# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ArtisanX (ArtisansX) is a location-based artisan services marketplace Android app for South Africa. Customers post jobs (plumbing, electrical, etc.) and artisans bid on them. Think Uber/Bolt for skilled trades.

**Current state:** Phase 1 (Mini MVP) is implemented — auth, role selection, job CRUD, artisan job browsing, basic profiles. Phases 2-4 (maps, bidding, bookings, chat, AI features) are not yet built.

## Tech Stack

- **Language:** Kotlin, Jetpack Compose (Material 3)
- **Architecture:** MVVM + Repository pattern, Hilt DI, Coroutines/Flow
- **Backend:** Appwrite Cloud (auth, database, storage) — no custom server
- **Maps:** Google Maps SDK for Compose (not yet integrated)
- **AI:** Groq/OpenRouter for job descriptions, Anthropic Claude API for bid helper and matching (Phase 4)
- **Min SDK:** 24, Target/Compile SDK: 36

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.example.artisanx.SomeTest"

# Run instrumented tests
./gradlew connectedAndroidTest

# Clean build
./gradlew clean assembleDebug

# Lint check
./gradlew lint
```

## Architecture

### Package Structure

Source root: `app/src/main/java/com/example/artisanx/`

- `di/` — Hilt modules. `AppModule` provides Appwrite `Client`, `Account`, `Databases`, `Storage` as singletons. `RepositoryModule` binds interfaces to implementations.
- `domain/model/` — Data classes mapping to Appwrite documents (Job, etc.)
- `domain/repository/` — Repository interfaces (`AuthRepository`, `JobRepository`, `ProfileRepository`)
- `data/repository/` — Implementations that call Appwrite `Databases`/`Account` services
- `data/local/` — `DataStoreManager` for local preferences (user role, session state)
- `presentation/` — UI layer organized by feature: `auth/`, `customer/`, `artisan/`, `onboarding/`, `profile/`, `common/`, `navigation/`
- `util/` — `Constants` (Appwrite IDs), `Resource<T>` sealed class (Loading/Success/Error)
- `ui/theme/` — Material 3 theme (Color, Type, Theme)

### Key Patterns

- **Resource sealed class** (`util/Resource.kt`): All async operations return `Resource<T>` — screens pattern-match on Loading/Success/Error
- **ViewModels** expose `StateFlow<Resource<T>>`, screens collect via `collectAsStateWithLifecycle()`
- **Navigation**: Single-Activity with `AppNavGraph` (Compose Navigation). Routes defined in `Screen` sealed class. Start destination determined by session state (login vs dashboard)
- **Role-based UI**: After registration, users pick Customer or Artisan. Role stored in DataStore. Each role has separate dashboard and bottom nav tabs.

### Appwrite Integration

- Database ID: defined in `Constants.DATABASE_ID`
- Collections: `user_profiles`, `artisan_profiles`, `jobs`, `bids`, `bookings`, `chat_messages`, `reviews`, `credits`
- Storage bucket: `artisansx_files`
- All Appwrite IDs/config read from `local.properties` via `BuildConfig`
- Document-level security enabled on all collections

## Configuration

API keys and Appwrite config live in `local.properties` (never committed):

```properties
MAPS_API_KEY=...
APPWRITE_PROJECT_ID=...
APPWRITE_ENDPOINT=https://fra.cloud.appwrite.io/v1
GROQ_API_KEY=...
OPENROUTER_API_KEY=...
```

These are read into `BuildConfig` fields in `app/build.gradle.kts`.

## Implementation Phases

See `README.md` for full details. Summary:

1. **Phase 1 (done):** Auth, role selection, job CRUD, artisan browse, profiles, navigation
2. **Phase 2:** Google Maps, full artisan onboarding (student vs independent), bidding, bookings, credits
3. **Phase 3:** In-app chat (Appwrite Realtime), ratings/reviews, push notifications (FCM)
4. **Phase 4:** AI features (job description generator, bid helper, job-artisan matching), UI polish

## Database Schema

Full schema with all attributes is in `README.md` Section 7. Key collections and their relationships:
- `jobs.customerId` -> user who posted
- `bids.jobId` + `bids.artisanId` -> artisan bid on a job (unique pair)
- `bookings` link a job, customer, and artisan with status tracking
- `chat_messages.bookingId` -> chat scoped to a booking
- `reviews.bookingId` -> one review per booking

## Code Standards

- Every Appwrite call wrapped in try-catch, returning `Resource<T>`
- Input validation on all user-facing forms before backend calls
- Use `rememberSaveable` for form state survival across config changes
- Accompanist `rememberPermissionState` for runtime permissions
- API keys never in source — always via `BuildConfig` from `local.properties`
