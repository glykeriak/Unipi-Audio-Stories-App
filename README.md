# Unipi-Audio-Stories-App

## Overview
This project is an **Android application** developed in **Java** that provides interactive **audio stories**.  
Users can log in with email or Google, browse stories, listen to them, track usage statistics, and manage their preferences.  
The app integrates **Firebase Authentication** for user login and **Firebase Realtime Database** for storing stories and user listening history.  

Built with:
- **Java (Android SDK)** → core app logic  
- **XML layouts** → UI design  
- **Firebase Authentication** → email/password & Google Sign-In  
- **Firebase Realtime Database** → story data & user stats  

---

## System Architecture

### Authentication
- `MainActivity.java` → login (email/password + Google Sign-In).  
- `SplashScreenActivity.java` → initial splash with app logo.  

### Stories
- `HomeActivity.java` → main screen with grid of story images.  
- `DetailActivity.java` → story detail: title, image, text, play button.  
- `AddInitialStoriesActivity.java` → imports initial stories into Firebase.  

### Analytics
- `AnalyticsActivity.java` → shows:
  - User statistics (favorite story, total listens).  
  - Global statistics (most popular story, total listens).  

### Settings
- `SettingsActivity.java` → manages:
  - Mute/unmute audio.  
  - Voice selection (spinner).  
  - Volume control (SeekBar).  
  - Language switch (Greek, English, French).  
- `activity_settings.xml` → UI for preferences.
- 
---

## Deployment
1. Install **Android Studio (latest version)**.  
2. Clone the repository:  
   ```bash
   git clone https://github.com/username/Unipi-Audio-Stories-App.git
3. Open the project in Android Studio.
4. Add your Firebase configuration file (google-services.json) into the app/ folder.
5. Sync Gradle and install dependencies.
6. Run the app on an emulator or a physical Android device (API 24+ recommended).

---

## Results

- Splash Screen: App logo displayed on startup.
- Login: Email/password + Google Sign-In.
- Home Screen: Grid of stories with cover images.
- Detail Screen: Story title, image, text, and play button (audio narration).
- Analytics: 
   * User stats (favorite story, listens count).
   * Global stats (most popular story, total listens).
- Settings: Mute/unmute, voice selection, volume control, language switch (GR/EN/FR).
- AddInitialStories: Seeds Firebase database with classic stories.
