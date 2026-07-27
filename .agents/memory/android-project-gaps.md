---
name: Android project completeness
description: Files that were missing or incomplete in the AI Resume Builder project after the initial import, and how they were resolved.
---

# AI Resume Builder — missing-file audit results

## What was actually missing (non-derivable lessons)

### Build dependency
`androidx.gridlayout:gridlayout:1.0.0` was absent from `app/build.gradle`
even though `activity_lock.xml` already used `androidx.gridlayout.widget.GridLayout`.
**Why:** The layout was written before the dependency was added; Gradle does not
auto-detect layout dependencies.

### FileProvider
`ExportUtils.shareFile()` called `context.getPackageName() + ".fileprovider"` but
no `<provider>` block existed in `AndroidManifest.xml` and `res/xml/file_paths.xml`
was absent. Both were created.
**How to apply:** Any project using `FileProvider` needs both the manifest entry
(`android:authorities="${applicationId}.fileprovider"`) and the XML paths file.

### Resource arrays
`res/xml/preferences.xml` referenced `@array/tones`, `@array/lengths`,
`@array/languages` — none existed. Created `res/values/arrays.xml`.
**How to apply:** Check `preferences.xml` array references when importing any
Android project that uses `PreferenceScreen`.

### Missing string resources
Several keys used in `preferences.xml` were absent from `strings.xml`:
`groq_api_settings`, `enter_api_key`, `default_settings`, `default_tone`,
`default_length`, `default_language`. Added inline.

### Stub activities
- `ProfileActivity.java` — `updateUi()` was empty; wired `tvName`, `tvHeadline`,
  and `btnEditProfile` → `EditProfileActivity`.
- `TemplatesActivity.java` — 27-line stub; replaced with full 21-template grid
  using an inner `RecyclerView.Adapter`.

### Missing item layout
`item_template_card.xml` did not exist; `TemplatesActivity` now requires it.
Created with emoji preview, name, description, and "Use Template" button.
