# Call Guardian

**Call Guardian** — A modern Android call screening app built with **Kotlin** and **Jetpack Compose**. It protects user privacy by automatically whitelisting trusted contacts, blocking unwanted calls, and guiding users through correct call forwarding setup. The app offers a sleek, intuitive experience while providing robust controls for call handling and privacy.

---

<div align="center">
  <img src="https://github.com/user-attachments/assets/40195c41-d54b-4249-900d-79361e46f815" alt="Call Guardian Screenshot 1" width="30%"/>
  <img src="https://github.com/user-attachments/assets/aeba9b97-04ce-437b-b67c-1b71f79cacb2" alt="Call Guardian Screenshot 2" width="30%"/>
  <img src="https://github.com/user-attachments/assets/d4b0ef3d-5356-46e2-88dd-755c6d87706b" alt="Call Guardian Screenshot 3" width="30%"/>
</div>

---

## What the app does

Call Guardian helps users manage incoming calls with these core capabilities:

* **Automatic Whitelisting:** Trusted contacts (from the address book or user-specified) are automatically whitelisted so important calls always get through.
* **Call Blocking:** Block unknown, spammy, or user-specified numbers with customizable rules and blocklists.
* **Call Screening UI:** A clear, minimal UI for reviewing recent blocked calls, whitelist entries, and block rules.
* **Call Forwarding Setup Assistance:** Guides users through carrier-specific call forwarding setup with step-by-step instructions and validation checks to ensure forwarding is configured correctly.
* **Privacy-first Design:** No unnecessary permissions; local-first storage of rules and whitelist entries.
* **Background Monitoring:** Uses WorkManager for scheduled tasks (e.g., periodic spam list updates or log maintenance).

---

## Project Structure

This project follows a layered approach. Each layer groups related responsibilities.

```
app/
├─ src/main/java/
│  ├─ data/            # Repositories, data sources (local DB, network), models (DTOs)
│  ├─ domain/          # Entities, use-cases, repository interfaces
│  ├─ presentation/    # Compose UI, ViewModels, navigation
│  ├─ service/         # Background services, call listeners, telephony integrations
│  └─ di/              # Hilt modules and bindings
└─ build.gradle
```

* **data/**: Concrete implementations (Room DAOs, network clients, preferences). Handles persistence and translation between DTOs and domain entities.
* **domain/**: Pure business logic, use-cases, and repository contracts so the presentation layer is testable and framework-agnostic.
* **presentation/**: Jetpack Compose screens, view models, and state handling.
* **service/**: Telephony/Call state listeners, WorkManager workers, and permission-handling helpers.
* **di/**: Hilt modules that expose singletons and scoped components for injection.

---

## Dependencies & Purpose

Below are the main dependency versions used in the project and why each is included.

### Versions (selected)

```
agp = 8.11.1
kotlin = 2.1.0
coreKtx = 1.16.0
composeBom = 2024.09.00
navigationCompose = 2.9.2
hiltAndroid = 2.55
room = 2.6.1
kotlinxCoroutines = 1.10.2
accompanistPermissions = 0.37.3
coil = 2.7.0
libphonenumber = 9.0.16
workManager = 2.10.5
androidxHiltWork = 1.3.0
splashscreen = 1.0.1
materialIconsExtended = 1.7.8
landscapistCoil = 2.6.1
```

### Libraries & Purposes

* `androidx.core:core-ktx` — Kotlin extensions for Android framework APIs (convenience helpers).
* `androidx.activity:activity-compose` — Activity integration for Compose.
* `androidx.compose` (BOM + ui/tooling) — Jetpack Compose UI toolkit and testing helpers.
* `androidx.navigation:navigation-compose` — Compose-friendly navigation component for in-app routing.
* `com.google.dagger:hilt-android` & `hilt-android-compiler` — Dependency injection for modular, testable components.
* `androidx.room:room-runtime`, `room-ktx`, `room-compiler` — Local persistence for whitelist, blocklists, and settings.
* `org.jetbrains.kotlinx:kotlinx-coroutines-android` & `core` — Concurrency and background tasks handling (used by ViewModels, repositories, workers).
* `com.googlecode.libphonenumber:libphonenumber` — Parsing and formatting phone numbers reliably across regions (important for matching and normalizing numbers for whitelist/block rules).
* `com.github.skydoves:landscapist-coil` & `io.coil-kt:coil-compose` — Image loading for any avatars or contact images in the UI.
* `androidx.work:work-runtime-ktx` — WorkManager for periodic background tasks (spam list updates, maintenance tasks).
* `androidx.hilt:hilt-work` & Hilt Work integration — Inject dependencies into WorkManager workers.
* `androidx.core:core-splashscreen` — Smooth splash screen integration on modern Android.
* `androidx.compose.material:material-icons-extended` — Extended Material icons for UI affordances.
* `ksp` — Kotlin Symbol Processing for annotation processors if used (Room / other codegen).

Each dependency is chosen to support a modern, robust Compose app with good DX and maintainability: DI (Hilt), persistence (Room), safe phone-number handling (libphonenumber), background scheduling (WorkManager), and polished UI (Compose + Coil).

---

## Architecture Decisions

* **Layered Architecture (data/domain/presentation):** Keeps UI free of business logic and allows the domain to be unit-tested in isolation.
* **Hilt for DI:** Simple, testable bindings and integration with Android components (Activities, Services, Workers).
* **Room for Local Storage:** Stores whitelist/blocklist and app state. Ensures offline-first behavior.
* **WorkManager:** Handles periodic or deferred tasks reliably across device restarts.
* **LibPhoneNumber:** Normalize phone numbers before comparing to avoid mismatches caused by formatting differences.

---

## Security & Permissions

* Request only necessary runtime permissions (READ_CONTACTS, ANSWER_PHONE_CALLS where applicable, POST_NOTIFICATIONS). Use `accompanist-permissions` to make Compose permission flows smoother.
* Store user preferences and lists locally; avoid uploading sensitive contact lists remotely without explicit consent.

---

## Contact

For questions or collaboration reach me at:

Email: engfred88@gmail.com

WhatsApp: 0754348118

LinkedIn: https://www.linkedin.com/in/fred-omongole-a5943b2b0/
