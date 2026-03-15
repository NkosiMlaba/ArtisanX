# ArtisansX — Claude Code / Antigravity Implementation Prompt

You are building **ArtisansX**, a location-based artisan services marketplace Android app (think Uber/Bolt but for skilled trades). The project uses **Kotlin**, **Jetpack Compose (Material 3)**, **Appwrite Cloud** as the BaaS, **Google Maps SDK for Compose**, and embeds **AI** via the Anthropic Claude API.

## Starting Point

This project continues from a **default "Empty Activity"** Android Studio project created with:
- **Template:** Empty Activity (Jetpack Compose)
- **Language:** Kotlin
- **Min SDK:** API 24 (Android 7.0)
- **Build system:** Gradle (Kotlin DSL — `build.gradle.kts`)
- **Package name:** `com.example.artisanx`
- **App name:** ArtisansX
Folder: C:\Users\nkosi\AndroidStudioProjects\ArtisanX
File: C:\Users\nkosi\AndroidStudioProjects\ArtisanX\app\src\main\java\com\example\artisanx\MainActivity.kt

The generated project already has `MainActivity.kt`, a theme folder, and a basic Compose scaffold. **Build from this foundation — do not replace the project structure, extend it.**

---

## Architecture & Code Standards

### Architecture: MVVM + Repository Pattern + Clean-ish Layers

```
com.example.artisanx/
├── ArtisansXApp.kt                  // Application class (Appwrite init, Hilt app)
├── MainActivity.kt                  // Single Activity, NavHost entry
├── di/                              // Hilt modules
│   ├── AppModule.kt                 // Appwrite client, singletons
│   └── RepositoryModule.kt          // Binds repo interfaces to impls
├── data/
│   ├── remote/                      // Appwrite service wrappers
│   │   ├── AppwriteAuthService.kt
│   │   ├── AppwriteDbService.kt
│   │   └── AppwriteStorageService.kt
│   ├── repository/                  // Repository implementations
│   │   ├── AuthRepositoryImpl.kt
│   │   ├── JobRepositoryImpl.kt
│   │   ├── ArtisanRepositoryImpl.kt
│   │   └── ChatRepositoryImpl.kt
│   └── model/                       // Data classes (Appwrite document models)
│       ├── UserProfile.kt
│       ├── ArtisanProfile.kt
│       ├── Job.kt
│       ├── Bid.kt
│       ├── Booking.kt
│       ├── ChatMessage.kt
│       └── Review.kt
├── domain/
│   ├── repository/                  // Repository interfaces
│   │   ├── AuthRepository.kt
│   │   ├── JobRepository.kt
│   │   ├── ArtisanRepository.kt
│   │   └── ChatRepository.kt
│   └── model/                       // Domain models (if distinct from data)
├── ui/
│   ├── navigation/
│   │   ├── AppNavGraph.kt           // NavHost with all routes
│   │   ├── Screen.kt                // Sealed class of routes
│   │   └── BottomNavBar.kt
│   ├── theme/                       // Already generated, extend it
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   └── Theme.kt
│   ├── common/                      // Shared composables
│   │   ├── LoadingIndicator.kt
│   │   ├── ErrorDialog.kt
│   │   ├── StarRating.kt
│   │   └── LocationPicker.kt
│   ├── auth/
│   │   ├── LoginScreen.kt
│   │   ├── RegisterScreen.kt
│   │   ├── RegisterViewModel.kt
│   │   └── LoginViewModel.kt
│   ├── customer/
│   │   ├── CustomerDashboardScreen.kt
│   │   ├── PostJobScreen.kt
│   │   ├── JobDetailScreen.kt
│   │   ├── BidsListScreen.kt
│   │   └── viewmodel/
│   ├── artisan/
│   │   ├── ArtisanDashboardScreen.kt
│   │   ├── ArtisanOnboardingScreen.kt  // Student vs Independent pathway
│   │   ├── JobBrowseScreen.kt
│   │   ├── BidSubmitScreen.kt
│   │   └── viewmodel/
│   ├── map/
│   │   ├── MapScreen.kt
│   │   └── MapViewModel.kt
│   ├── chat/
│   │   ├── ChatListScreen.kt
│   │   ├── ChatScreen.kt
│   │   └── ChatViewModel.kt
│   ├── booking/
│   │   ├── BookingScreen.kt
│   │   ├── BookingStatusScreen.kt
│   │   └── BookingViewModel.kt
│   └── ai/
│       ├── AiAssistantScreen.kt     // AI-powered job matching / suggestions
│       └── AiViewModel.kt
└── util/
    ├── Constants.kt                 // DB IDs, collection IDs, bucket IDs
    ├── Resource.kt                  // sealed class: Loading, Success, Error
    ├── Extensions.kt                // String, Date, etc. extensions
    └── LocationUtils.kt             // Fused location, permissions
```

### Code Standards (STRICT)

1. **DRY** — Extract repeated logic into util functions or shared composables. Never duplicate Appwrite query logic.
2. **Explicit style** — Use full `if-else` blocks, define-process-return pattern. No overly clever one-liners.
3. **Error handling everywhere** — Every Appwrite call wrapped in `try-catch`. Use the `Resource<T>` sealed class (`Loading`, `Success(data)`, `Error(message)`). Surface errors to the UI with user-friendly messages.
4. **Edge cases** — Handle: no network, empty lists, null fields from Appwrite, duplicate submissions, expired sessions, location permission denied, camera permission denied.
5. **Security**:
   - API keys NEVER in source code. Use `local.properties` + `BuildConfig` fields.
   - Appwrite permissions set per-collection (document-level security enabled).
   - Input validation on ALL user inputs before sending to backend.
   - Sanitise text inputs to prevent injection.
6. **Compose best practices** — Use `remember`, `rememberSaveable`, `LaunchedEffect`, `collectAsStateWithLifecycle()`. No side effects in composable body.

---

## Dependencies (add to app-level `build.gradle.kts`)

```kotlin
// Appwrite
implementation("io.appwrite:sdk-for-android:11.4.0")

// Hilt (DI)
implementation("com.google.dagger:hilt-android:2.51.1")
kapt("com.google.dagger:hilt-android-compiler:2.51.1")
implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

// Navigation
implementation("androidx.navigation:navigation-compose:2.8.5")

// Google Maps for Compose
implementation("com.google.maps.android:maps-compose:6.2.1")
implementation("com.google.android.gms:play-services-maps:19.0.0")
implementation("com.google.android.gms:play-services-location:21.3.0")

// Accompanist (permissions)
implementation("com.google.accompanist:accompanist-permissions:0.36.0")

// Coil (image loading)
implementation("io.coil-kt:coil-compose:2.7.0")

// Anthropic API (AI feature — via OkHttp/Retrofit manually)
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// DataStore (local prefs)
implementation("androidx.datastore:datastore-preferences:1.1.1")

// Lifecycle
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
```

Also add the Hilt and Google Maps plugins in the project-level and app-level gradle files, and the Kotlin `kapt` plugin.

---

## Appwrite Setup (DEVELOPER MUST DO MANUALLY)

### 1. Create Appwrite Cloud Project
- Go to https://cloud.appwrite.io
- Create project: **"ArtisansX"**
- Note down: **Project ID**, **Region endpoint** (e.g. `https://fra.cloud.appwrite.io/v1`)
- Add Android platform with package `com.example.artisanx`

### 2. Create Database: `artisansx_db`

**Collections to create** (enable Document Security on each):

| Collection ID           | Purpose                                |
|------------------------|----------------------------------------|
| `user_profiles`        | Customer profiles                      |
| `artisan_profiles`     | Artisan data (student/independent)     |
| `jobs`                 | Job postings by customers              |
| `bids`                 | Bids from artisans on jobs             |
| `bookings`             | Confirmed bookings                     |
| `chat_messages`        | In-app chat messages                   |
| `reviews`              | Post-job reviews                       |
| `credits`              | Artisan credit balance & transactions  |

**Attributes per collection** — create these in the Appwrite console:

**`user_profiles`**: `userId` (string), `fullName` (string), `email` (string), `phone` (string), `addresses` (string[]), `profileImageId` (string, optional), `createdAt` (datetime)

**`artisan_profiles`**: `userId` (string), `fullName` (string), `email` (string), `phone` (string), `isStudent` (boolean), `institutionName` (string, optional), `studentNumber` (string, optional), `studentCardFileId` (string, optional), `courseField` (string, optional), `gradYear` (integer, optional), `idFileId` (string, optional), `tradeCategory` (string), `skills` (string), `serviceArea` (string), `serviceRadiusKm` (float), `latitude` (float), `longitude` (float), `workPhotoIds` (string[]), `certifications` (string, optional), `yearsExperience` (integer, optional), `verified` (boolean, default false), `badge` (string — "Student Artisan" or "Verified Artisan"), `avgRating` (float, default 0), `reviewCount` (integer, default 0), `createdAt` (datetime)

**`jobs`**: `customerId` (string), `title` (string), `category` (string), `description` (string), `photoIds` (string[]), `latitude` (float), `longitude` (float), `address` (string), `budget` (float, optional), `urgency` (string — "standard" | "urgent"), `jobType` (string — "standard" | "bidding"), `status` (string — "open" | "assigned" | "in_progress" | "completed" | "cancelled"), `assignedArtisanId` (string, optional), `createdAt` (datetime)

**`bids`**: `jobId` (string), `artisanId` (string), `priceOffer` (float), `message` (string), `estimatedHours` (float), `status` (string — "pending" | "accepted" | "rejected"), `createdAt` (datetime)

**`bookings`**: `jobId` (string), `customerId` (string), `artisanId` (string), `status` (string — "requested" | "accepted" | "in_progress" | "completed"), `startedAt` (datetime, optional), `completedAt` (datetime, optional), `isPaid` (boolean, default false), `createdAt` (datetime)

**`chat_messages`**: `bookingId` (string), `senderId` (string), `message` (string), `imageFileId` (string, optional), `createdAt` (datetime)

**`reviews`**: `bookingId` (string), `customerId` (string), `artisanId` (string), `rating` (integer, 1-5), `comment` (string), `createdAt` (datetime)

**`credits`**: `artisanId` (string), `balance` (integer), `lastUpdated` (datetime)

### 3. Create Storage Bucket: `artisansx_files`
- For profile images, student cards, ID uploads, work photos, chat images.
- Set max file size: 10MB.
- Permissions: Users can read/write their own files.

### 4. Set Collection Permissions
- **`user_profiles`**: Users — read own, create, update own. 
- **`artisan_profiles`**: Users — read any (so customers can browse), create, update own.
- **`jobs`**: Any authenticated — read. Creator — create, update, delete.
- **`bids`**: Artisan (creator) — create, read own. Job owner — read all bids on their job.
- **`bookings`**: Participants — read, update.
- **`chat_messages`**: Booking participants — read, create.
- **`reviews`**: Any authenticated — read. Customer — create.
- **`credits`**: Artisan (owner) — read. Server-side only for writes (or use Appwrite Functions).

### 5. Indexes
Create indexes for: `jobs.status`, `jobs.category`, `bids.jobId`, `bookings.customerId`, `bookings.artisanId`, `chat_messages.bookingId`, `reviews.artisanId`, `artisan_profiles.tradeCategory`.

---

## Google Cloud / Maps Setup (DEVELOPER MUST DO MANUALLY)

1. Go to https://console.cloud.google.com
2. Create project: **"ArtisansX"**
3. Enable these APIs:
   - Maps SDK for Android
   - Places API (for address search)
   - Geocoding API
   - Directions API
4. Create an API key → restrict it to your Android app (package name + SHA-1 fingerprint)
5. Add to `local.properties`:
   ```
   MAPS_API_KEY=your_key_here
   ```
6. In `app/build.gradle.kts`, read it into BuildConfig:
   ```kotlin
   android {
       buildFeatures { buildConfig = true }
       defaultConfig {
           val props = java.util.Properties()
           props.load(rootProject.file("local.properties").inputStream())
           buildConfigField("String", "MAPS_API_KEY", "\"${props["MAPS_API_KEY"]}\"")
           manifestPlaceholders["MAPS_API_KEY"] = props["MAPS_API_KEY"] as String
       }
   }
   ```
7. In `AndroidManifest.xml`:
   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="${MAPS_API_KEY}" />
   ```

---

## Anthropic Claude API Setup (AI Feature)

1. Get an API key from https://console.anthropic.com
2. Add to `local.properties`:
   ```
   ANTHROPIC_API_KEY=your_key_here
   ```
3. Read into BuildConfig same pattern as Maps key.
4. **IMPORTANT: In production, this call should go through YOUR backend proxy to avoid exposing the key on-device. For this academic MVP, calling directly from the app is acceptable but document this limitation clearly in comments.**

### AI Feature: Smart Job Matching & Artisan Assistant

Create a Retrofit service that calls the Anthropic Messages API:

```kotlin
// data/remote/ClaudeApiService.kt
interface ClaudeApiService {
    @POST("v1/messages")
    suspend fun sendMessage(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Body request: ClaudeRequest
    ): ClaudeResponse
}

data class ClaudeRequest(
    val model: String = "claude-sonnet-4-20250514",
    val max_tokens: Int = 1024,
    val messages: List<ClaudeMessage>
)
data class ClaudeMessage(val role: String, val content: String)
data class ClaudeResponse(val content: List<ClaudeContent>)
data class ClaudeContent(val type: String, val text: String)
```

**AI Use Cases to implement (document each one with comments):**
1. **Smart Job Description Generator** — Customer describes job roughly, AI refines into a structured, clear description.
2. **Artisan Bid Helper** — Artisan pastes job details, AI suggests a fair price range and professional message template.
3. **Job-Artisan Matching** — Given a job's category/description and a list of artisans (skills, ratings, distance), AI ranks best matches with explanations.

Each AI feature must have a dedicated UI with:
- Clear labelling: "Powered by AI" / "AI-generated suggestion"
- User can edit/override AI output before submitting
- Loading state while waiting for API response
- Error handling if API call fails (graceful fallback, not crash)

---

## Implementation Order (phased — see PROJECT-PLAN.md for details)

### Phase 1: Foundation (Mini MVP)
1. Project setup, dependencies, Hilt DI, Appwrite init
2. Auth screens (Register/Login with email+password via Appwrite)
3. Role selection (Customer vs Artisan) after registration
4. Basic navigation shell (bottom nav for each role)
5. Customer: Post a job (CRUD — title, category, description, location)
6. Artisan: Browse jobs list
7. Basic profile screens

### Phase 2: Core Marketplace
8. Google Maps integration — map on job post, artisan location
9. Artisan onboarding — Student vs Independent pathway with file uploads
10. Bidding system — artisan submits bid, customer views bids
11. Booking flow — accept bid → booking created → status tracking
12. Credits system — basic balance, spend to unlock jobs

### Phase 3: Communication & Trust
13. In-app chat (per-booking thread)
14. Ratings & reviews (post-completion)
15. Push notifications (Appwrite + Firebase Cloud Messaging)

### Phase 4: AI & Polish
16. AI job description generator
17. AI bid helper
18. AI job-artisan matching
19. Edge case hardening, error states, empty states
20. UI polish, animations, loading skeletons

---

## Key Compose Patterns to Use

```kotlin
// Resource sealed class
sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
}

// ViewModel pattern
@HiltViewModel
class JobViewModel @Inject constructor(
    private val jobRepo: JobRepository
) : ViewModel() {
    private val _jobs = MutableStateFlow<Resource<List<Job>>>(Resource.Loading)
    val jobs: StateFlow<Resource<List<Job>>> = _jobs.asStateFlow()

    fun loadJobs() {
        viewModelScope.launch {
            _jobs.value = Resource.Loading
            try {
                val result = jobRepo.getOpenJobs()
                _jobs.value = Resource.Success(result)
            } catch (e: Exception) {
                _jobs.value = Resource.Error(e.message ?: "Failed to load jobs")
            }
        }
    }
}

// Screen pattern
@Composable
fun JobListScreen(viewModel: JobViewModel = hiltViewModel()) {
    val jobsState by viewModel.jobs.collectAsStateWithLifecycle()

    when (val state = jobsState) {
        is Resource.Loading -> LoadingIndicator()
        is Resource.Error -> ErrorDialog(message = state.message)
        is Resource.Success -> {
            if (state.data.isEmpty()) {
                EmptyStateMessage("No jobs available yet")
            } else {
                LazyColumn { items(state.data) { job -> JobCard(job) } }
            }
        }
    }
}
```

---

## Permissions to declare in AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

Use Accompanist's `rememberPermissionState` for runtime permission requests with proper rationale dialogs.

---

## .gitignore additions

```
local.properties
*.jks
*.keystore
google-services.json
```

**NEVER commit API keys, keystore files, or `local.properties`.**

---

## Notes for the Developer (README notes)

- **AI Documentation**: For each AI feature, add a comment block at the top of the file explaining: what AI does, why it's used here, what the prompt is, and how the user interacts with it. This is required for academic submission.
- **Testing**: Write at least basic unit tests for Repository and ViewModel layers. Use `kotlinx-coroutines-test` and Mockk.
- **Accessibility**: Add `contentDescription` to all images and icons. Use semantic properties on interactive elements.
- **Offline handling**: Show cached data where possible (DataStore for user profile), and clear "No connection" states elsewhere.