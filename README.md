# AI Email Writer - Android Application

## Overview

AI Email Writer is a professional-grade Android application that leverages the power of the Groq AI API to generate high-quality, context-aware emails. The app is built with Java and follows Material Design 3 guidelines, offering a clean and intuitive user experience.

## Features

### Core Functionality
- **AI-Powered Email Generation**: Generate professional emails using Groq's llama-3.3-70b-versatile model
- **Multiple Email Types**: Support for 20+ email types including Business, Customer Support, Job Application, Sales, Academic, and more
- **Tone Selection**: Choose from 10 different tones (Professional, Friendly, Formal, Casual, etc.)
- **Email Length Control**: Select between Short, Medium, and Long formats
- **Multi-Language Support**: Generate emails in 9 languages including English, Spanish, French, German, Italian, Portuguese, Arabic, Chinese, and Japanese

### Email Management
- **Local SQLite Database**: All emails stored securely on device
- **Email History**: View all previously generated emails
- **Favorites System**: Mark and organize favorite emails
- **Search Functionality**: Real-time search across recipients, purposes, subjects, and content
- **Copy & Share**: Easily copy emails to clipboard or share via other apps

### User Interface
- **Material Design 3**: Modern, responsive UI with smooth animations
- **Dark Mode Support**: Automatic dark theme support
- **Landscape Support**: Full landscape orientation support
- **Empty States**: Friendly empty state illustrations
- **Progress Indicators**: Visual feedback during API calls

### Settings
- **API Key Management**: Secure storage of Groq API keys
- **Default Preferences**: Set default tone, length, and language
- **Data Management**: Clear all history with confirmation
- **About & Privacy**: Access privacy policy and app information

## Technical Stack

### Architecture
- **Pattern**: MVC (Model-View-Controller)
- **Language**: Java (no Kotlin)
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)

### Dependencies
- **AndroidX**: AppCompat, ConstraintLayout, RecyclerView, CardView
- **Material Design 3**: Material Components
- **Networking**: OkHttp 4.11.0
- **JSON**: Gson 2.10.1
- **Database**: SQLite (built-in)

### Key Libraries
```gradle
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.10.0'
implementation 'androidx.recyclerview:recyclerview:1.3.2'
implementation 'androidx.cardview:cardview:1.0.0'
implementation 'com.squareup.okhttp3:okhttp:4.11.0'
implementation 'com.google.code.gson:gson:2.10.1'
```

## Project Structure

```
app/
├── src/main/
│   ├── java/com/julisunkan/aiemail/
│   │   ├── MainActivity.java              # Main email generation screen
│   │   ├── HistoryActivity.java           # Email history with search
│   │   ├── FavoritesActivity.java         # Favorite emails view
│   │   ├── SettingsActivity.java          # App settings and configuration
│   │   ├── model/
│   │   │   └── Email.java                 # Email data model
│   │   ├── database/
│   │   │   └── DatabaseHelper.java        # SQLite database management
│   │   ├── network/
│   │   │   └── ApiClient.java             # Groq API integration
│   │   ├── adapter/
│   │   │   └── EmailAdapter.java          # RecyclerView adapter
│   │   ├── util/
│   │   │   ├── Constants.java             # App constants
│   │   │   ├── PreferenceManager.java     # SharedPreferences wrapper
│   │   │   ├── NetworkUtils.java          # Network connectivity checks
│   │   │   └── DateUtils.java             # Date formatting utilities
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main.xml
│   │   │   ├── activity_history.xml
│   │   │   ├── activity_favorites.xml
│   │   │   ├── activity_settings.xml
│   │   │   └── item_email.xml
│   │   ├── menu/
│   │   │   ├── menu_main.xml
│   │   │   ├── menu_history.xml
│   │   │   └── menu_email_options.xml
│   │   ├── drawable/            # Vector icons
│   │   ├── values/
│   │   │   ├── strings.xml       # String resources
│   │   │   ├── colors.xml        # Color definitions
│   │   │   ├── styles.xml        # Theme styles
│   │   │   ├── arrays.xml        # Spinner arrays
│   │   │   ├── dimens.xml        # Dimension values
│   │   │   └── bools.xml         # Boolean resources
│   │   └── xml/
│   │       ├── preferences.xml
│   │       ├── backup_rules.xml
│   │       └── data_extraction_rules.xml
│   └── AndroidManifest.xml
├── build.gradle
├── proguard-rules.pro
└── settings.gradle
```

## Database Schema

### Table: emails

| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER PRIMARY KEY | Auto-increment ID |
| recipient | TEXT NOT NULL | Email recipient name/email |
| purpose | TEXT NOT NULL | Email purpose/subject |
| notes | TEXT | Additional notes/context |
| tone | TEXT | Email tone (Professional, Friendly, etc.) |
| type | TEXT | Email type (Business, Job Application, etc.) |
| length | TEXT | Email length (Short, Medium, Long) |
| language | TEXT | Language of email (English, Spanish, etc.) |
| subject | TEXT | Generated email subject |
| email_body | TEXT | Generated email content |
| date_created | LONG | Timestamp of creation |
| favorite | INTEGER | 0 = not favorite, 1 = favorite |

## API Integration

### Groq API Configuration
- **Base URL**: `https://api.groq.com/openai/v1/chat/completions`
- **Model**: `llama-3.3-70b-versatile`
- **Temperature**: 0.7 (balanced creativity)
- **Max Tokens**: 2048
- **Timeout**: 60 seconds
- **Retry Logic**: Up to 2 retries with exponential backoff

### API Error Handling
- 429 (Rate Limited): Automatic retry with delay
- 401 (Unauthorized): Invalid API key message
- 500/503 (Server Error): Automatic retry with delay
- Network errors: User-friendly error messages
- Timeout handling: 60-second timeout with notification

## Getting Started

### Prerequisites
- Android Studio 4.2 or later
- JDK 11 or later
- Android SDK 24+ (API level 24)
- Groq API key (free at https://console.groq.com)

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/julisunkan/AI-Email-Writer.git
cd AI-Email-Writer
```

2. **Open in Android Studio**
   - File → Open → Select the project directory
   - Wait for Gradle sync to complete

3. **Get Groq API Key**
   - Visit https://console.groq.com
   - Sign up for a free account
   - Generate an API key

4. **Configure API Key**
   - Launch the app
   - Go to Settings
   - Enter your Groq API key
   - Click "Save API Key"

5. **Build and Run**
   - Click Run (Shift+F10) or select Run → Run 'app'
   - Choose target device/emulator
   - App will install and launch

## Usage

### Generating an Email

1. **Fill in the form**:
   - Enter recipient name or email
   - Describe the email purpose
   - (Optional) Add additional notes
   - Select tone, type, length, and language

2. **Generate**:
   - Click "Generate Email" button
   - Wait for AI to process (typically 5-15 seconds)
   - Subject and body will appear below

3. **Actions**:
   - **Copy**: Copy generated email to clipboard
   - **Share**: Share via email, messaging, etc.
   - **Save**: Store in local database
   - **Clear**: Reset form for new email

### Managing Emails

- **View History**: Tap History menu to see all generated emails
- **Search**: Use search bar to find emails by recipient, purpose, subject, or content
- **Favorites**: Mark emails as favorite for quick access
- **Delete**: Long press or tap menu to delete individual emails
- **Clear All**: Settings → Clear All History to delete everything

## Prompt Engineering

The app uses a carefully crafted prompt to ensure high-quality email generation:

```
You are an expert business communication assistant.

Write a professional email.

Recipient: {recipient}
Purpose: {purpose}
Email Type: {type}
Tone: {tone}
Length: {length}
Language: {language}
Additional Notes: {notes}

Return ONLY two parts separated by '---SEPARATOR---':
1. Subject line
2. Email body

Do not include any other text or explanation.
```

## Performance Optimizations

- **Database Queries**: Indexed for fast search
- **Image Optimization**: Vector drawables for scalability
- **Memory Management**: Proper cursor and database closing
- **Thread Safety**: Background API calls on separate threads
- **Lifecycle Handling**: Proper cleanup in onDestroy()

## Security Considerations

- **API Key Storage**: Encrypted SharedPreferences (Android best practice)
- **HTTPS Only**: All network calls use HTTPS
- **Local Storage**: All data stored locally, never transmitted
- **No Tracking**: No analytics or telemetry
- **Permissions**: Only INTERNET and ACCESS_NETWORK_STATE required

## Known Limitations

- Requires internet connection for email generation (API calls)
- API rate limits depend on Groq account tier
- Maximum email body length: 2048 tokens
- Language support limited to Groq API capabilities

## Troubleshooting

### API Key Issues
- Ensure API key is valid: Check at https://console.groq.com
- API key not saving: Check device storage permissions
- Invalid format error: Remove extra spaces or special characters

### Network Issues
- No internet connection: Check Wi-Fi or mobile data
- Timeout errors: Check internet speed or try again
- Rate limit exceeded: Wait a few minutes before retrying

### Email Generation Issues
- Empty response: Check API key and internet connection
- Malformed email: Ensure purpose field is detailed
- Unexpected format: Try adjusting tone or email type

## Building for Release

### Generate Signed APK

1. **Build → Generate Signed Bundle/APK**
2. **Select APK** (or Bundle for Play Store)
3. **Create or select keystore**
4. **Enter credentials and sign**
5. **Select release buildType**
6. **APK ready in app/release/**

### ProGuard Configuration

ProGuard rules are configured for:
- GSON serialization
- OkHttp/Okio
- Model classes

## Testing

### Manual Testing Checklist
- [ ] Generate email with all parameters
- [ ] Test search functionality
- [ ] Verify favorite toggling
- [ ] Test copy/share features
- [ ] Clear all history
- [ ] Change API key in settings
- [ ] Test offline mode
- [ ] Verify database persistence
- [ ] Test landscape orientation
- [ ] Test with different email types/tones

## Deployment

The app is ready for deployment on Google Play Store:

1. **Generate signed release APK** (see Building for Release)
2. **Test on device thoroughly**
3. **Create Play Store listing**
4. **Upload signed APK**
5. **Fill in app store details**
6. **Submit for review**

## License

MIT License - See LICENSE file for details

## Author

Agbajelola Olasunkanmi Julius

## Support

For issues, feature requests, or questions:
- Create an issue on GitHub
- Email: julisunkan@gmail.com

## Changelog

### Version 1.0.0 (Initial Release)
- AI email generation using Groq API
- 20+ email types and 10 tones
- Multi-language support (9 languages)
- Email history and favorites
- Search functionality
- Material Design 3 UI
- Dark mode support
- SQLite database
- Full offline support except API calls

## Future Enhancements

- [ ] Email templates
- [ ] Batch email generation
- [ ] Email scheduling
- [ ] Cloud sync (optional)
- [ ] Biometric authentication
- [ ] Email signature management
- [ ] Undo/Redo functionality
- [ ] Advanced search filters
- [ ] Email statistics dashboard
- [ ] Integration with email clients

## Acknowledgments

- Groq for providing powerful AI API
- Material Design team for design guidelines
- Android development community
- All contributors and users

---

**Made with ❤️ for professional communication**
