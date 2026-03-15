---
description: How to run the ArtisansX application
---

### Prerequisites
1. An Android device with USB debugging enabled OR a running Android Emulator.
2. Java 21 installed and `JAVA_HOME` set correctly.
3. Android SDK installed.

### Option 1: Using Android Studio (Recommended)
1. Open the project in Android Studio.
2. Wait for Gradle sync to complete.
3. Select your device/emulator from the dropdown menu in the toolbar.
4. Click the **Run** button (green play icon) or press `Shift + F10`.

### Option 2: Using Command Line (Gradle)

1. Verify device connection:
// turbo
```powershell
& "C:\Users\nkosi\AppData\Local\Android\Sdk\platform-tools\adb.exe" devices
```

2. Clean and build the project:
// turbo
```powershell
./gradlew clean assembleDebug
```

3. Install and run on the connected device:
// turbo
```powershell
./gradlew installDebug
```

4. Launch the app manually on the device or use `adb` to start the main activity:
// turbo
```powershell
& "C:\Users\nkosi\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell am start -n com.example.artisanx/com.example.artisanx.MainActivity
```
