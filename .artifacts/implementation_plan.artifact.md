# Implementation Plan: Firebase UI Cleanup

This plan outlines the changes to remove the manual Firebase URL configuration UI while maintaining the central database connection.

## Proposed Changes

### [service]

#### [FirebaseDbService.kt](file:///D:/safeher%20(4)/app/src/main/java/com/example/service/FirebaseDbService.kt)
- Keep the `firebaseUrl` logic but ensure it defaults to the central project URL.
- (Optional) Remove `SharedPreferences` setter if we want to strictly prevent any future changes, even from other code.

---

### [viewmodel]

#### [SafetyViewModel.kt](file:///D:/safeher%20(4)/app/src/main/java/com/example/viewmodel/SafetyViewModel.kt)
- Remove `updateFirebaseUrl` function to prevent UI-driven changes.

---

### [ui]

#### [Screens.kt](file:///D:/safeher%20(4)/app/src/main/java/com/example/ui/Screens.kt)
- Delete "Section 3: Firebase Cloud Sync Hub" from `LiveMonitoringScreen`.
- This removes the URL input field and the "Link RTDB" button.

## Verification Plan

### Automated Tests
- Run `app:assembleDebug` to ensure no compilation errors after removing the UI code.

### Manual Verification
- Deploy the app to the emulator.
- Navigate to the **Biometrics** screen and verify the "Firebase Cloud Sync Hub" section is gone.
- Check the app logs (Logcat) or the "Database Connections Feed" (if still visible) to confirm successful `PUT` requests to the hardcoded URL.
