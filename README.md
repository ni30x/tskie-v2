# TSKIE

<p align="center">
  <a href="https://github.com/ni30x/tskie-v2/releases/download/v1.1/app-debug.apk">
    <img src="https://img.shields.io/badge/Download-APK-brightgreen?style=for-the-badge&logo=android" alt="Download APK">
  </a>
  <a href="https://github.com/ni30x/tskie-v2/releases/tag/v1.1">
    <img src="https://img.shields.io/badge/version-1.1-blue?style=for-the-badge" alt="Version 1.1">
  </a>
  <a href="#">
    <img src="https://img.shields.io/badge/minSdk-24-orange?style=for-the-badge" alt="Min SDK 24">
  </a>
</p>

## 📥 Direct Install

Grab the latest APK and side-load it on your Android device:

| Step | Action |
|:---|:---|
| 1️⃣ | **[Download the Direct APK (v1.1)](https://github.com/ni30x/tskie-v2/releases/download/v1.1/app-debug.apk)** or view the [Latest Release Page](https://github.com/ni30x/tskie-v2/releases/tag/v1.1) |
| 2️⃣ | On your Android device, enable **Settings → Security → Install unknown apps** |
| 3️⃣ | Open the downloaded APK and tap **Install** |

> **💡 Pro tip:** If you have `adb` connected, run:
> ```bash
> adb install app/build/outputs/apk/debug/app-debug.apk
> ```

---

TSKIE is a minimal, distraction-free Android task-planning application built on a singular, powerful philosophy: 
> **Plan tomorrow tonight. Complete today with focus.**

TSKIE eliminates the overhead of modern productivity software—there are no complex boards, tag managers, projects, or endless settings. The app provides a laser-focused, offline-first workflow to prepare your task list every evening and execute it today.

---

## 📱 App Screenshots

| Today Screen | Tomorrow Screen | Add Task | Calendar View |
| :---: | :---: | :---: | :---: |
| <img src="./screenshots/today%20page.png" width="200" alt="Today Screen"/> | <img src="./screenshots/tommorow%20page.png" width="200" alt="Tomorrow Screen"/> | <img src="./screenshots/add%20task%20page.png" width="200" alt="Add Task Screen"/> | <img src="./screenshots/calendar%20page.png" width="200" alt="Calendar Screen"/> |

| Activity Insights | Activity Detail | Settings Page 1 | Settings Page 2 |
| :---: | :---: | :---: | :---: |
| <img src="./screenshots/actitivity%20insight.png" width="200" alt="Activity Insight"/> | <img src="./screenshots/activity%20inight%202.png" width="200" alt="Activity Insight Detail"/> | <img src="./screenshots/setting%201.png" width="200" alt="Settings 1"/> | <img src="./screenshots/setting%202.png" width="200" alt="Settings 2"/> |

---

## 🏗️ Architectural Design (MVVM + Clean Architecture)

TSKIE is built using strict **Clean Architecture**, **MVVM (Model-View-ViewModel)**, and the **Repository Pattern** to ensure absolute decoupling, maintainability, and scalability.

```
       ┌─────────────────────────────────────────────────────────┐
       │                    Presentation Layer                   │
       │  (TodayScreen, TomorrowScreen, CalendarScreen, etc.)    │
       └────────────────────────────┬────────────────────────────┘
                                    │
                                    ▼
       ┌─────────────────────────────────────────────────────────┐
       │                       Domain Layer                      │
       │       (Use Cases: CreateTask, CompleteTask, etc.)       │
       └────────────────────────────┬────────────────────────────┘
                                    │
                                    ▼
       ┌─────────────────────────────────────────────────────────┐
       │                        Data Layer                       │
       │     (RepositoryImpl, Room Local DB, Firebase Sync)     │
       └─────────────────────────────────────────────────────────┘
```

### Dependency Rules:
- **Presentation -> Domain -> Data**. No backward dependencies are allowed.
- **Single Source of Truth**: The UI reads exclusively from the local Room SQLite Database (`tskie.db`). Cloud sync runs silently in the background and never blocks UI operations.

---

## 🗄️ Database Schema

TSKIE stores all persistent states inside an SQLite database called `tskie.db` using **Room**.

### 1. `tasks` Table:
- `id` (String UUID, Primary Key, Required)
- `title` (String, Required, validated against empty strings)
- `notes` (String?, Optional)
- `priority` (Enum HIGH/MEDIUM/LOW, Optional)
- `reminderTime` (Long?, Optional)
- `reminderEnabled` (Boolean, Required)
- `status` (Enum ACTIVE/COMPLETED/DELETED, Required)
- `taskDate` (LocalDate String "yyyy-MM-dd", Required)
- `createdAt` (Long, Required)
- `updatedAt` (Long, Required)
- `completedAt` (Long?, Optional)
- `syncState` (Enum LOCAL_ONLY/SYNC_PENDING/SYNCED/SYNC_FAILED, Required)

### 2. `settings` Table:
- `id` (Int, Primary Key = 1)
- `reminderEnabled` (Boolean)
- `reminderTime` (Long)
- `priorityEnabled` (Boolean)
- `defaultPriority` (Enum HIGH/MEDIUM/LOW)
- `analyticsEnabled` (Boolean)
- `signedIn` (Boolean)

### 3. `statistics_cache` Table (Optional):
- `date` (String "yyyy-MM-dd", Primary Key)
- `tasksCreated` (Int)
- `tasksCompleted` (Int)
- `completionRate` (Float)

---

## 🔔 Notification & Reminder Laws

TSKIE creates exactly one notification channel named **"TSKIE Notifications"** with default importance. It supports exactly two reminder types:

1. **Daily Planning Reminder**:
   - Promptly reminds you to plan tomorrow's task list.
   - Configurable (enabled/disabled in Settings).
   - Tapping it opens the app directly to the **Tomorrow** screen.
2. **Task Reminder**:
   - Fires at a scheduled task reminder time (default is 2:00 PM).
   - Cancelled immediately if the task is completed or deleted before it fires.
   - Tapping it opens the app directly to the **Today** screen.

---

## 📊 Analytics & Privacy Guidelines

- **Privacy First**: Task titles, notes, and personal content are **never** logged.
- **Opt-In/Opt-Out**: Metrics are only collected anonymously if `analyticsEnabled` is turned on in Settings.
- **Allowed Predefined Events**: `app_open`, `first_launch`, `open_today`, `open_tomorrow`, `open_calendar`, `open_settings`, `task_created`, `task_completed`, `task_restored`, `task_deleted`, `sign_in`, `sign_out`, `sync_started`, `sync_completed`, `sync_failed`, `reminder_enabled`, `reminder_disabled`.

---

## 🧪 Testing Suite

TSKIE includes a robust testing package to ensure reliable operation:
- **Unit Tests**: Full stubs and mock assertions verifying the core Use Cases under `UseCaseTests.kt`.
- **UI & Visual Regression Tests**: Fully configured screenshot tests leveraging **Robolectric** and **Roborazzi** inside `GreetingScreenshotTest.kt`.

---

## 🛠️ Building & Compiling APKs

> [!IMPORTANT]
> **AI Assistant Directive / Initial Setup Prompt:**
> Before running any build commands, instruct the AI or run the setup command below to ensure all mandatory tools, JDK 17, Android SDK tools, and project dependencies are properly installed and synchronized:
>
> ```text
> "AI Prompt: Please verify and install Java 17, Android SDK (API 34/35), Android Build-Tools, accept SDK licenses, and download/sync all Gradle project dependencies before building debug or release APKs."
> ```

### 1. Mandatory Environment & Dependencies Installation
Run these commands first in your environment to install all required SDKs and dependencies:

```bash
# 1. Update package list and install OpenJDK 17
sudo apt-get update && sudo apt-get install -y openjdk-17-jdk gradle

# 2. Set JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# 3. Accept Android SDK Licenses & Sync Gradle Dependencies
# Note: In standard local environments, run wrapper or gradle sync:
gradle --refresh-dependencies
```

### 2. Prerequisites
- **JDK**: Java 17 or higher
- **Android SDK**: API Level 34 (Android 14) or higher installed
- **Gradle**: 8.x or Gradle Wrapper

### 3. Build Debug APK
To compile a Debug APK for local testing and debugging:

```bash
# Using installed Gradle
gradle :app:assembleDebug

# Or using Gradle Wrapper (if available on your system)
./gradlew assembleDebug
```
- **Output Path**: `app/build/outputs/apk/debug/app-debug.apk`

### 4. Build Release APK
To compile a Release APK for production deployment:

```bash
# Using installed Gradle
gradle :app:assembleRelease

# Or using Gradle Wrapper
./gradlew assembleRelease
```
- **Output Path**: `app/build/outputs/apk/release/app-release.apk`

### 5. Build Release Android App Bundle (AAB)
For Google Play Store submission:

```bash
gradle :app:bundleRelease
```
- **Output Path**: `app/build/outputs/bundle/release/app-release.aab`

### Signing Configuration (For Release Builds)
To sign release builds with your custom production keystore, configure `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("path/to/your/my-release-key.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "your_password"
            keyAlias = System.getenv("KEY_ALIAS") ?: "your_key_alias"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "your_key_password"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

