# ScrollBill

ScrollBill is a privacy-first Android app that turns real Android app-usage statistics into an understandable screen-time summary.

## Current Phase 2 scope

The beta-readiness phase builds on the verified usage and receipt foundation:

- requests and re-checks Android Usage Access
- queries real `UsageStatsManager` data
- summarizes the last seven completed local calendar days
- shows totals, daily average, projected annual use, and ranked apps in Compose
- resolves launchable app labels/icons through an in-memory launcher metadata index
- excludes the current HOME app and justified Android infrastructure from totals and rankings
- creates a frozen receipt snapshot from the current seven-day report
- renders one deterministic Classic receipt at 1080×1920 as PNG
- previews that exact rendered bitmap in-app and shares it through the Android Sharesheet
- explains Usage Access in a focused first-run screen
- provides Settings/About and an in-app privacy explanation
- includes a receipt-inspired adaptive and monochrome launcher identity
- prepares factual privacy, Play Store, and release documentation
- keeps usage data on the device

The receipt currently supports the last seven completed local calendar days only.

There is no monetization, account, analytics, backend, networking, permanent receipt storage, or additional report period in this phase. The receipt remains limited to the last seven completed local calendar days.

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
./gradlew assembleRelease
./gradlew bundleRelease
```

Install the debug APK from `app/build/outputs/apk/debug/app-debug.apk` using Android Studio or `adb`.

Release builds are locally generated and not production-signed by this repository. Configure an upload keystore through secure Gradle properties or CI secrets before a Play upload; never commit the keystore or its passwords. The local release artifacts are `app/build/outputs/apk/release/app-release-unsigned.apk` and `app/build/outputs/bundle/release/app-release.aab`.

## How Usage Access works

`PACKAGE_USAGE_STATS` is a special access controlled by Android Settings, not a normal runtime permission. ScrollBill checks `AppOpsManager.OPSTR_GET_USAGE_STATS`, opens `Settings.ACTION_USAGE_ACCESS_SETTINGS`, and checks again whenever the activity resumes. The report is queried only after access is available.

## Manual device test

1. Install and open the app without Usage Access.
2. Verify the focused first-run explanation and three benefits.
3. Tap **Grant usage access**.
4. Grant ScrollBill access in Android Settings.
5. Return to the app and verify that real usage appears when history is available.
6. Revoke access.
7. Return to the app and verify it returns to the access-required state.
8. Grant access again.
9. Verify **Refresh report** re-queries the device history.
10. From a loaded report, tap **Create my receipt**.
11. Verify the preview shows the weekly receipt and tap **Share receipt**.
12. Choose a target in the Android Sharesheet and verify it receives a PNG image.
13. Return to ScrollBill and verify the dashboard report remains available.
14. Open Settings and verify the version, Usage Access status, privacy row, and on-device privacy message.
15. Open Privacy and verify the local-processing and explicit-sharing explanation.
16. Use Back to return to the dashboard, then relaunch the app.

The platform may return an empty report on a new emulator or device. That is shown as an empty state, not fabricated data.

## Privacy and permissions

The source app manifest explicitly declares only `android.permission.PACKAGE_USAGE_STATS` and a narrow launcher-intent `<queries>` declaration. AndroidX adds its internal signature-protected dynamic-receiver permission to the merged manifest; it is not a user-granted capability and is unrelated to usage data. Usage history is queried locally and is never uploaded, logged as individual records, persisted in a database, or sent to a service. The app intentionally does not request `QUERY_ALL_PACKAGES` and falls back to package names or a neutral icon when package metadata is not visible or cannot be resolved.

UsageStats supplies package names and foreground durations, not guaranteed consumer-facing labels or icons. Android package visibility can limit metadata discovery, so ScrollBill declares visibility only for applications exposing the ordinary `ACTION_MAIN` + `CATEGORY_LAUNCHER` intent. It builds a package-keyed launcher metadata index in memory with `PackageManager.queryIntentActivities()` for the current user, then tries `LauncherApps`, permitted direct `PackageManager` metadata, and finally the package name plus a neutral icon. No individual package names or broad package inventory visibility are declared.

The in-app Privacy screen and the draft at `docs/privacy-policy.md` describe the same current behavior: local report calculation, temporary cache-only receipt handling, and sharing only after an explicit user action. No installed-app inventory or usage history is persisted in a database.

The receipt uses only display labels and durations from the current loaded report. `ReceiptSnapshot` excludes package identifiers and app icons. The Classic receipt is a fixed 1080×1920 text-oriented image containing the last seven completed days, top five apps, reconciled other-app usage, total, daily average, yearly pace, and `Made with ScrollBill`. It is rendered once and the same bitmap is shown in preview and encoded as PNG for sharing.

Raw usage retains millisecond precision. For the public receipt, total and app durations are converted to displayed whole minutes using one consistent truncation rule. `OTHER APPS` is then calculated from displayed total minutes minus displayed top-five minutes, clamped defensively at zero, so the visible arithmetic always balances.

Sharing is explicitly user initiated. The PNG is written to `cacheDir/shared_receipts/scrollbill-weekly-receipt.png`, exposed only through the non-exported FileProvider, and sent with `ACTION_SEND` as `image/png`. It is not saved to public storage or permanently persisted.

Report totals mean ScrollBill's aggregated foreground application usage after excluding ScrollBill, the resolved HOME application, `android`, and `com.android.systemui`. This is a product-specific aggregation and should not be represented as guaranteed identical to Android Digital Wellbeing's proprietary calculation.

There is no Internet permission, backend, API client, analytics, telemetry, crash reporting, advertising, authentication, or remote configuration.

## Known platform limitations

- Android controls Usage Access and the available history; some devices may return no records.
- App labels and icons may be unavailable because package visibility is intentionally narrow or an app was uninstalled.
- `UsageStatsManager` data is system-provided and can vary by Android version and device vendor.
- The annual value is a projection of the selected seven-day average, not a prediction.

## Versioning

The beta application ID is `com.soultware.scrollbill`, with `versionCode 1` and `versionName 0.1.0`. Beta development follows `0.x.y`; every future Play upload must increase `versionCode`.

## Future work

Future phases may decide how to add monetization, additional receipt themes, and additional report periods. Those features are deliberately not implemented here.
