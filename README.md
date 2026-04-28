# ExitSense – Smart Reminders Before You Leave

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2026.02.01-green.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Material3](https://img.shields.io/badge/Material%203-Latest-purple.svg?style=flat&logo=materialdesign)](https://m3.material.io)

**ExitSense** is a smart Android application designed to ensure you never forget your essentials again. By intelligently detecting when you are leaving a saved location (like your home, office, or hostel), ExitSense sends timely reminders for the specific items you need.

---

## 🚀 Key Features

- **📍 Smart Exit Detection**: Uses Geofencing and WorkManager to detect when you leave a saved location and triggers reminders instantly.
- **🎒 Item Management**: Organize your belongings with custom icons, importance levels (Critical, High, Medium, Low), and recurring schedules (Weekdays, Weekends, or specific days).
- **🏠 Location Awareness**: Save frequently visited places like Home, College, or Library with easy one-tap location detection.
- **📜 Activity History**: Track your habits! See what you've remembered or forgotten over time. The app learns from your patterns to provide smarter suggestions.
- **🛡️ Secure Auth**: Seamless login experience using **Firebase Google Sign-In** to keep your data synced and secure.
- **🎨 Premium UI/UX**: Built entirely with **Jetpack Compose and Material 3**, featuring smooth animations (Shatter Splash, Pop-in empty states) and a beautiful Dark/Light theme.
- **⚙️ Preferences**: Highly customizable settings including reminder sensitivity, notification toggles, and language support, all persisted via **DataStore**.

---

## 🛠️ Tech Stack

- **UI Framework**: Jetpack Compose (Material 3)
- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Local Database**: Room Database (Items, Locations, History)
- **Local Storage**: Preferences DataStore (User Settings)
- **Background Tasks**: WorkManager
- **Location Services**: Google Play Services Location API
- **Auth**: Firebase Authentication (Google Sign-In)
- **Image Loading**: Coil
- **Navigation**: Compose Navigation

---

## 📸 Screen Gallery

- **Shatter Splash**: A unique "particle shatter" entrance effect.
- **Home Dashboard**: Quick view of today's items and recent activity.
- **Items & Locations**: Beautifully crafted lists with swipe-to-delete and pinning functionality.
- **Settings**: Comprehensive control over app behavior and look.

---

## 🔧 Installation & Setup

1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/rajsv/ExitSense.git
    ```
2.  **Add Firebase**: 
    - Create a project on the [Firebase Console](https://console.firebase.google.com/).
    - Add an Android app and download the `google-services.json` file.
    - Place it in the `app/` directory.
3.  **Build**: Open the project in **Android Studio (Ladybug or newer)** and sync Gradle.
4.  **Run**: Deploy to an Android device with API 26 (Oreo) or higher.

---

## 🤝 Contribution

Contributions are welcome! If you have ideas for new features or find any bugs, feel free to open an issue or submit a pull request.

---

## 👨‍💻 Developer

**Rajsv Mahendra**  
Contact: rajasvmahendra@gmail.com  

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
