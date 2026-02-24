# FitnessAp

An Android fitness and nutrition tracking app built with Jetpack Compose and Material Design 3. Log workouts and meals, get AI-powered calorie estimates, and back up your data to the cloud.

---

## Features

- **Calorie Dashboard** — Live summary of calories burned, consumed, net balance, and a donut chart showing progress toward your daily goal.
- **AI Activity Logging** — Describe a workout in plain English (e.g. "Ran 5 km in 30 minutes") and GPT-4o-mini estimates the calories burned based on your profile.
- **Step Counter** — Uses the device's hardware step sensor to count steps in real time and calculate calories burned from your body weight.
- **AI Food Logging** — Describe a meal (e.g. "Bowl of oatmeal with banana and honey") and get an instant calorie estimate, or enter calories manually.
- **User Profile** — Set your name, age, weight, height, gender, activity level, and daily calorie goal. These are used to personalise AI estimates.
- **Cloud Sync** — Sign in with Google and your logs + profile automatically sync to Firebase Firestore every night at 8 PM ET.
- **Food Reminders** — Optional daily notifications at 7 AM, 1 PM, and 8 PM to remind you to log your meals.

---


## How It Works

### Home Tab
The dashboard reads today's activity and food logs from the local database in real time and displays:
- Calories burned (blue card)
- Calories consumed (orange card)
- Net balance = consumed − burned
- A donut chart showing how the net compares to your daily goal (hidden if no goal is set)

### Track Activity Tab
**AI mode:** Type a description of your workout → tap **Estimate Calories** → the app sends your description along with your profile (age, weight, height, activity level) to OpenAI and returns an estimate. Confirm to save it.

**Step counter mode:** Tap **Start Step Tracking** to begin counting steps via the hardware sensor. The app calculates `calories = steps × weight(kg) × 0.0005` live. Tap **Stop & Log Steps** to save the session.

### Track Food Tab
**AI mode:** Describe what you ate → tap **Estimate Calories** → OpenAI returns a calorie count. Confirm to save.

**Manual mode:** Enter a food name and calorie count directly and tap **Log Food**.

### Profile Tab
- Edit your personal details and save them to DataStore.
- Paste your OpenAI API key (required for AI estimation).
- Toggle food reminders on/off.
- Sign in with Google to enable nightly Firestore sync.

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| UI | Jetpack Compose, Material Design 3, Navigation Compose |
| Architecture | MVVM, StateFlow, ViewModelProvider.Factory |
| Local storage | Room 2.6.1 (ActivityLog, FoodLog), DataStore Preferences |
| Cloud | Firebase Auth (Google Sign-In), Cloud Firestore |
| Background tasks | WorkManager (nightly sync, daily reminders) |
| AI | OpenAI Chat Completions API (gpt-4o-mini) |
| Networking | OkHttp 4.12.0, kotlinx.serialization |
| Min / Target SDK | 29 / 34 |
| Language | Kotlin 1.9.0 |

---

## Project Structure

```
app/src/main/java/com/example/fitnessap/
├── data/
│   ├── datastore/        # DataStore keys and access
│   ├── firebase/         # Firestore sync repository
│   ├── local/            # Room database, DAOs, entities
│   ├── model/            # UserProfile data class
│   └── repository/       # LogRepository, UserProfileRepository
├── navigation/           # Bottom nav items, NavGraph
├── network/              # OpenAI service + request/response models
├── notification/         # FoodReminderWorker
├── sync/                 # FirebaseSyncWorker
├── ui/
│   ├── home/             # HomeScreen + HomeViewModel
│   ├── activity/         # TrackActivityScreen + ViewModel
│   ├── food/             # TrackFoodScreen + ViewModel
│   ├── profile/          # ProfileScreen + ProfileViewModel
│   └── theme/            # Color, Type, Theme
└── util/                 # TimeUtils (timezone-aware delay helpers)
```

---

## Setup

### 1. Clone the repo
```bash
git clone https://github.com/Arkantos1231/FitnessApP.git
cd FitnessApP
```

### 2. Add `google-services.json`
This file is not tracked in git. Download it from your Firebase project:

**Firebase Console → Project Settings → Your apps → Download `google-services.json`**

Place it at `app/google-services.json`.

### 3. Add your OpenAI API key
The app does not ship with an API key. After installing, open the **Profile** tab and paste your key into the **OpenAI API Key** field, then tap **Save**.

You can get a key at [platform.openai.com](https://platform.openai.com).

### 4. Build and run
Open the project in Android Studio (Electric Eel or newer) and run on a device or emulator running Android 10 (API 29) or higher.

---

## Firestore Security Rules

If you use your own Firebase project, set these rules to ensure users can only access their own data:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
  }
}
```

---

## License

This project is for personal use. No license is currently specified.
