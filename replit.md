# AI Resume Builder — Android App

## Overview

**AI Resume Builder** is a production-quality Android application that uses the Groq AI API to help users create, review, and manage professional resumes.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 34 (Android 14) |
| Architecture | MVC (Repository pattern) |
| Database | SQLite via `DatabaseHelper` |
| Networking | Retrofit 2 + OkHttp |
| AI | Groq API (`llama-3.3-70b-versatile` default) |
| UI | Material Design 3 |
| Security | `EncryptedSharedPreferences`, Biometric, PIN hash (SHA-256) |

## Package

```
com.airesumebuilder
```

## Project Structure

```
app/src/main/java/com/airesumebuilder/
├── AIResumeApp.java             Application class
├── activities/                  All Activity screens (28 total)
├── adapters/                    RecyclerView adapters
├── database/                    DatabaseHelper (SQLite schema)
├── models/                      POJOs (Resume, Profile, JobApplication, …)
├── network/                     Groq API via Retrofit
├── notifications/               BroadcastReceivers + channels
├── repositories/                Data access layer
├── security/                    EncryptedSharedPreferences, PIN/biometric
└── utils/                       DateUtils, ExportUtils, PreferenceManager, UiUtils
```

## Key Features

- **Resume Builder** — unlimited resumes with 20+ section types, auto-save
- **AI Features** — powered by Groq API (review, cover letter, interview prep, career coach, portfolio generator)
- **Job Tracker** — track applications with status filters
- **Templates** — 21 professional templates
- **Security** — PIN lock + biometric unlock
- **Dark / AMOLED / Light / System themes**
- **Export** — TXT, HTML (PDF planned), share via system sheet

## How to Build

This is an Android Studio / Gradle project. To build an APK:

```bash
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

> **Note:** Replit does not have an Android emulator, so the app cannot be previewed here. Open the project in Android Studio or build the APK above.

## AI API Key

The app requires a free Groq API key:
1. Visit https://console.groq.com/keys
2. Open the app → **Settings → AI Settings**
3. Paste your key and tap **Save**

Keys are stored in `EncryptedSharedPreferences` — never in plain text.

## User Preferences

- Keep the Java-only rule (no Kotlin)
- Do not hardcode API keys — always use `SecurityHelper`
- All DB calls must run on background threads via `ExecutorService`
- Follow Material Design 3 conventions throughout
