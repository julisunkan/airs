# AI Email Writer — Android App

## Overview
A professional Android application (Java, Material Design 3) that generates context-aware emails using the Groq AI API (llama-3.3-70b-versatile model). Emails are stored locally in SQLite. Users can generate, edit, save, copy, share, and organize emails into favorites.

## Stack
- **Language**: Java (no Kotlin)
- **Min SDK**: API 24 (Android 7.0) | **Target SDK**: API 34
- **Architecture**: MVC
- **Database**: SQLite via `DatabaseHelper`
- **Networking**: OkHttp 4.11.0 + Groq REST API
- **UI**: Material Design 3 (`Theme.MaterialComponents.DayNight.NoActionBar`)
- **Build**: Android Gradle Plugin 8.1.1

## Running the Project
This is a native Android app — it **cannot run in Replit's preview pane**. To run it:
1. Build an APK via Android Studio or `./gradlew assembleDebug`
2. Install on a device or emulator

## Key Source Files
- `app/src/main/java/com/julisunkan/aiemail/`
  - `MainActivity.java` — compose & generate emails; subject/body are editable before saving
  - `EditEmailActivity.java` — edit saved emails (subject + body), save changes to DB
  - `HistoryActivity.java` — browse all emails; long-press or ⋮ menu → Edit/Copy/Share/Favorite/Delete
  - `FavoritesActivity.java` — browse favorite emails; same menu options
  - `adapter/EmailAdapter.java` — RecyclerView adapter with rotating 5-color accent palette
  - `database/DatabaseHelper.java` — SQLite CRUD for emails table
  - `network/ApiClient.java` — Groq API HTTP client

## Color Palette (Earth + Vibrant)
Green · Orange · Yellow/Amber · Brown · Red — defined in `res/values/colors.xml`

## User Preferences
- Keep Java (no Kotlin migration)
- Maintain MVC architecture
- Colorful UI: green, orange, yellow, brown, red accent scheme
- All email edits (subject + body) must be captured from the EditText fields, not cached strings
