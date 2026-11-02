# MG iSMART India — Android starter

Native Android starter for the MG Windsor EV Pro.

## Included
- Premium dark Jetpack Compose dashboard
- Battery, range, charging, lock, odometer and cabin status placeholders
- Connection setup placeholder
- GitHub Actions APK build workflow

## Important
This is a UI preview only. It does not yet authenticate with MG iSMART India or read live vehicle data. The supplied Home Assistant Python integration is not an Android SDK; a separately implemented and verified Android API adapter is still required. No MG credentials are requested. Remote commands are intentionally disabled.

## Build locally
Open this folder in Android Studio with JDK 17 and Android SDK 35, then build the `app` debug variant.

Build workflow updated: Android SDK setup now installs platform-tools and accepts SDK licenses before the build.

Build fix: Kotlin package namespace corrected to a valid identifier.
