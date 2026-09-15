# ScrollBill

ScrollBill is a privacy-first Android app that turns real Android app-usage statistics into an understandable screen-time summary.

## Phase 0 scope

Phase 0 proves the Android usage foundation only:

- requests and re-checks Android Usage Access
- queries real `UsageStatsManager` data
- summarizes the last seven completed local calendar days
- shows totals, daily average, projected annual use, and ranked apps in Compose
- resolves launchable app labels/icons through `LauncherApps` when available
- excludes the current HOME app and justified Android infrastructure from totals and rankings
- keeps usage data on the device

There is no sharing, receipt-card generation, backend, account, analytics, networking, or persistence in this phase.

## Requirements

- Android Studio with an Android API 36 SDK and Build Tools 36.0.0
- JDK 17
- Gradle is provided by the Gradle wrapper
- Android device or emulator running API 26 or newer

The project targets and compiles against API 36 and supports API 26+.

The current stable Compose BOM (`2026.08.00`) resolves Compose 1.12, whose AAR metadata requires compileSdk 37. Because Phase 0 requires compileSdk 36, this project uses the earlier stable BOM (`2026.06.00`, Compose 1.11 line) compatible with API 36. This is a compatibility constraint, not an alpha/beta dependency. The project uses AGP 9.3.1 with Gradle 9.5.0 and Kotlin 2.4.20, which are mutually compatible according to Kotlin's compatibility guidance.

## Build and run

From this directory:

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

Install the debug APK from `app/build/outputs/apk/debug/app-debug.apk` using Android Studio or `adb`.

## How Usage Access works

`PACKAGE_USAGE_STATS` is a special access controlled by Android Settings, not a normal runtime permission. ScrollBill checks `AppOpsManager.OPSTR_GET_USAGE_STATS`, opens `Settings.ACTION_USAGE_ACCESS_SETTINGS`, and checks again whenever the activity resumes. The report is queried only after access is available.

## Manual device test

1. Install and open the app without Usage Access.
2. Verify the focused access screen.
3. Tap **Grant usage access**.
4. Grant ScrollBill access in Android Settings.
5. Return to the app and verify that real usage appears when history is available.
6. Revoke access.
7. Return to the app and verify it returns to the access-required state.
8. Grant access again.
9. Verify **Refresh report** re-queries the device history.

The platform may return an empty report on a new emulator or device. That is shown as an empty state, not fabricated data.

## Privacy and permissions

The source app manifest explicitly declares only `android.permission.PACKAGE_USAGE_STATS`. AndroidX adds its internal signature-protected dynamic-receiver permission to the merged manifest; it is not a user-granted capability and is unrelated to usage data. Usage history is queried locally and is never uploaded, logged as individual records, persisted in a database, or sent to a service. The app intentionally does not request `QUERY_ALL_PACKAGES` and falls back to package names or a neutral icon when package metadata is not visible or cannot be resolved.

UsageStats supplies package names and foreground durations, not guaranteed consumer-facing labels or icons. ScrollBill first asks `LauncherApps` for the current user's launchable activity metadata, then uses permitted `PackageManager` metadata, then falls back to the package name and a neutral icon. No `<queries>` declaration is used.

Report totals mean ScrollBill's aggregated foreground application usage after excluding ScrollBill, the resolved HOME application, `android`, and `com.android.systemui`. This is a product-specific aggregation and should not be represented as guaranteed identical to Android Digital Wellbeing's proprietary calculation.

There is no Internet permission, backend, API client, analytics, telemetry, crash reporting, advertising, authentication, or remote configuration.

## Known platform limitations

- Android controls Usage Access and the available history; some devices may return no records.
- App labels and icons may be unavailable because package visibility is intentionally narrow or an app was uninstalled.
- `UsageStatsManager` data is system-provided and can vary by Android version and device vendor.
- The annual value is a projection of the selected seven-day average, not a prediction.

## Future work

Future phases may decide how to add receipt-card design and sharing. Those features are deliberately not implemented here.
