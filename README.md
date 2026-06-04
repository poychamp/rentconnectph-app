# RentConnectPH — Android

**The native Android app for RentConnectPH** — browse rental listings in Cagayan de Oro, Philippines, connecting renters directly with property owners. A parallel surface to the web and iOS apps, sharing the same backend.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](https://www.android.com)
[![AGP](https://img.shields.io/badge/AGP-9.2-02303A?logo=gradle&logoColor=white)](https://developer.android.com/build)

🌐 **[rentconnectph.com](https://rentconnectph.com)**

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.2 |
| UI | Jetpack Compose + Material 3 (no XML layouts), single-Activity, `NavHost` + bottom `NavigationBar` |
| Architecture | MVVM — ViewModel → Repository → API, `StateFlow` state + sealed `Result` for errors-as-data |
| Concurrency | Kotlin Coroutines + Flow (no RxJava, no callbacks) |
| Networking | Retrofit + OkHttp + kotlinx.serialization, single `OkHttpClient` choke point |
| Persistence | DataStore (preferences — theme, recent filters) |
| Images | Coil |
| Maps | Google Maps SDK + Maps Compose |
| Navigation | Navigation Compose — type-safe `@Serializable` routes |
| Dependencies | Gradle Version Catalog (`gradle/libs.versions.toml`) |
| Backend | Shared Laravel 12 API — public, unauthenticated `/api/v1/*` endpoints |
| Min deployment | Android 8.0+ (API 26), target API 36 |

This is a **read-only, fully public** browsing app — no authentication, accounts, or login. Every API call is anonymous.

## Getting Started

### Prerequisites

- Android Studio (latest stable, with AGP 9.2 support)
- JDK 17 — bundled as the Android Studio JBR (`Android Studio/jbr`)
- An Android 8.0+ (API 26) emulator or device — a Pixel API 34 image is recommended

### Installation

```bash
# Clone and open the project
git clone <repo-url>
cd RentConnectApp
```

Open the folder in Android Studio and let Gradle sync resolve dependencies on first
import. Select the **`localDebug`** build variant and an emulator, then run with
**Shift+F10** (▶).

### Build configurations

Four product flavors, each pinning a backend via a `BASE_URL` `BuildConfig` field. The
`local` flavor points the emulator at a Laravel instance on the host machine; `dev` uses
the hosted dev server, so no local server is required to run the app.

| Variant | Flavor | Backend |
|---|---|---|
| `localDebug` (default) | local | Emulator → host Laravel (`http://10.0.2.2`) |
| `devDebug` | dev | Hosted Vapor dev server |
| `stagingDebug` | staging | `staging.rentconnectph.com` |
| `prodRelease` | prod | Production Laravel (`rentconnectph.com`) |

Only `local` and `staging` carry an `applicationIdSuffix`; `dev` and `prod` ship the bare
`ph.rentconnect.app` package ID.

## Available Commands

Build and run from Android Studio (**▶** / **Ctrl+Shift+F10**), or from the command line.
On Windows, point `JAVA_HOME` at the Android Studio JBR first:

```bash
# Build the default debug variant
JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew :app:assembleLocalDebug

# Run the unit test suite
JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew :app:testLocalDebugUnitTest

# Run a single test class
JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew :app:testLocalDebugUnitTest \
  --tests "ph.rentconnect.app.feature.home.HomeViewModelTest"
```

A Google Maps API key is read from `local.properties` (`MAPS_API_KEY=...`) and injected
via `manifestPlaceholders`. The map renders blank without it; the rest of the app runs
fine.

## Testing

Unit tests run on JUnit 5 with Mockk, Turbine, and `kotlinx.coroutines.test`:

- One test class per ViewModel (state transitions) and per Repository.
- Repository tests drive an OkHttp `MockWebServer` and assert mapped `Result` types.
- `StateFlow` `Loading` visibility is observed by holding the mock suspended with a
  `CompletableDeferred` so Turbine can see the intermediate state.
- Critical user flows are covered by Compose UI tests with `createComposeRule()`.

```bash
JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew :app:testLocalDebugUnitTest
```

## Deployment

Distributed as a direct APK for MVP, moving to Google Play when ready for production.
Release builds are assembled against the `prodRelease` variant and signed with a keystore
configured in `local.properties` (`RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`,
`RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`).

A force-update gate reads the `X-Min-Android-Consumer-Version` response header and compares
it to `BuildConfig.VERSION_NAME`; clients below the threshold are routed to an update
screen. Missing headers soft-fail.

## Security

If you discover a security vulnerability, please report it privately to
**info@rentconnectph.com** rather than opening a public issue. We take all reports
seriously and will respond as quickly as possible.

The Maps API key and release signing config live in `local.properties`, which is
gitignored and never committed.

## License

© 2026 RentConnectPH. All rights reserved. This is proprietary software and is not licensed for redistribution.
