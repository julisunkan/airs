# AI Resume Builder

AI Resume Builder is a Java Android application for creating, improving, and
managing professional resumes locally on a device. It combines a guided resume
builder with AI-assisted writing tools powered by the Groq API.

The project follows the product direction in
`attached_assets/Pasted--Comprehensive-Replit-AI-Prompt-Build-AI-Resume-Builder_1785185883017.txt`.
The application is designed for offline-first use: local resume, profile, job
tracker, favorites, and settings data remain on the device, while AI features
require the user's own Groq API key and an internet connection.

## Features

### Resume workspace

- Create and manage multiple resumes
- Add profile information, summaries, objectives, education, experience,
  skills, projects, certifications, awards, volunteer work, languages,
  interests, references, publications, conferences, achievements, military
  service, memberships, and custom sections
- Choose from professional resume templates
- Edit, duplicate, preview, delete, favorite, and search resumes
- Export and share resume content

### AI career tools

- AI resume review and scoring
- Cover letter generation
- Interview preparation
- Career Coach chat
- Portfolio content generation
- Groq model configuration through AI Settings

### Career management

- Job application tracker with statuses, dates, notes, search, and filters
- Analytics for resumes, AI usage, exports, completion, and recent activity
- Favorites and global search
- Local backup and restore
- Optional notifications and reminders

### Privacy and personalization

- No online account is required
- Local SQLite storage
- Encrypted storage for the Groq API key
- PIN lock and biometric unlock support
- Light, dark, AMOLED, and system themes
- Material Design 3 interface
- Portrait and landscape layouts

## Technology

| Layer | Technology |
| --- | --- |
| Language | Java (no Kotlin) |
| Package | `com.airesumebuilder` |
| Minimum SDK | API 26 (Android 8.0) |
| Target / compile SDK | API 34 (Android 14) |
| UI | Material Design 3, AndroidX, View Binding |
| Persistence | SQLite through `DatabaseHelper` and repositories |
| Networking | Retrofit 2 and OkHttp |
| JSON | Gson |
| Images | Glide |
| Preferences | DataStore and secure preferences where appropriate |
| Security | Android Keystore, EncryptedSharedPreferences, Biometric |
| AI provider | Groq API |

## Project structure

```text
app/src/main/
├── java/com/airesumebuilder/
│   ├── AIResumeApp.java
│   ├── activities/       # Dashboard, resume, AI, job, settings, and help screens
│   ├── adapters/         # RecyclerView adapters
│   ├── database/         # SQLite schema and database helper
│   ├── models/           # Resume, profile, job, and supporting models
│   ├── network/          # Groq API client and request/response models
│   ├── notifications/    # Notification channels and receivers
│   ├── repositories/     # Background data access
│   ├── security/         # Secure preferences and lock helpers
│   └── utils/            # Export, date, preference, and UI utilities
├── res/
│   ├── layout/
│   ├── menu/
│   ├── values/
│   └── xml/
└── AndroidManifest.xml
```

## Getting started

### Prerequisites

- Android Studio with Android SDK 34
- JDK 17
- Android SDK platform and build tools for API 34
- A Groq API key for AI features: <https://console.groq.com/keys>

### Build a debug APK

From the project root:

```bash
./gradlew assembleDebug
```

On systems where the wrapper is not executable, run:

```bash
bash ./gradlew assembleDebug
```

The APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Run the app

1. Open the project in Android Studio.
2. Allow Gradle to sync and install the required SDK components.
3. Select an Android 8.0+ device or emulator.
4. Build and run the `app` configuration.
5. Open **Settings → AI Settings** and save your own Groq API key before using
   AI tools.

The API key is entered by the user and stored with encrypted app preferences.
Never commit a key to source control or place one in Gradle files.

## AI integration

The app uses Groq's OpenAI-compatible chat completions endpoint through the
network layer. The default model is `llama-3.3-70b-versatile`; model and
generation settings are exposed through AI Settings where supported.

AI requests handle common offline, timeout, authorization, and service errors
with user-facing messages. Resume creation, editing, local search, favorites,
job tracking, and backup features do not require an AI key or network access.

## Development guidelines

- Keep the Java-only project rule.
- Keep API keys out of source code, logs, and documentation.
- Run database work on background executors.
- Keep user data local unless an explicit future integration requires otherwise.
- Reuse string resources and Material Design 3 components.
- Add validation and clear empty/error states to new screens.
- Preserve the existing package name `com.airesumebuilder`.

## Verification

Useful checks before submitting changes:

```bash
bash ./gradlew assembleDebug
```

Replit does not provide an Android emulator in this workspace, so install the
generated APK on an Android device or use Android Studio for interactive UI
testing.

## License

See the repository license file for licensing terms.