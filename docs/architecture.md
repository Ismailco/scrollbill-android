# ScrollBill Phase 0 architecture

## Current architecture

The single `app` module uses a small separation of concerns:

- `domain/model`: `UsagePeriod`, `AppUsage`, and `WeeklyUsageSummary` contain framework-free reporting data.
- `domain/usage`: `UsageAggregator` filters, groups, ranks, and calculates summary values from plain records.
- `data/usage`: Android AppOps access checking, `UsageStatsManager` queries, launcher metadata indexing/resolution, and dynamic HOME exclusion resolution.
- `ui`: a `ScrollBillViewModel` exposes a typed `StateFlow`; Compose renders access, loading, empty, error, and dashboard states.
- `ui/theme`: Material 3 light/dark color schemes.

The activity owns only system setup, the ViewModel factory, the settings intent fallback, and Compose content. No platform usage query occurs in a composable.

## Dependency direction

Domain code depends on Kotlin and `java.time` only. Android data code depends on the domain. The UI depends on domain models and data interfaces through the ViewModel. The Activity composes the concrete Android implementations manually; no DI framework is needed for this phase.

## Usage-data flow

The ViewModel checks AppOps on a background dispatcher. After access is granted, the repository computes the period, queries `UsageStatsManager.INTERVAL_DAILY` once for each completed local calendar day, obtains a fresh exclusion policy, and passes plain package/duration records to `UsageAggregator`. The ViewModel resolves labels and icons through the metadata resolver before exposing UI state.

## Period semantics

The primary period is `[today - 7 days at local date, today at local date)`. Each day is converted from local midnight to epoch milliseconds in the device timezone before its daily query. This is seven completed calendar days, not a rolling 168-hour interval, and remains correct across timezone offset changes.

## Privacy boundary

Usage data stays in process on the device. There is no network permission, client, backend, database, analytics, telemetry, or production logging of package durations. The only user-facing special access is `PACKAGE_USAGE_STATS`; AndroidX's internal dynamic-receiver signature permission may also appear in the merged manifest.

The manifest declares one narrow package-visibility query for `ACTION_MAIN` + `CATEGORY_LAUNCHER`. The metadata resolver uses `PackageManager.queryIntentActivities()` to build an in-memory, package-keyed index of ordinary launchable applications for the current user. Multiple launcher activities are sorted by activity class name and collapse to one deterministic application metadata result. `LauncherApps` is then used as a current-user supplemental source, followed by permitted direct `PackageManager` lookup, package-name fallback, and neutral-icon fallback. The launcher inventory is never persisted.

Usage totals exclude ScrollBill, the dynamically resolved HOME package, `android`, and `com.android.systemui`. The exclusion policy is deliberately conservative: preinstalled/system status alone is not a reason to exclude a user-facing application. These totals are ScrollBill's aggregation and are not guaranteed to match Digital Wellbeing.

## Why no backend and no broad package visibility

Phase 0 needs only local system data and local calculation, so a backend would add privacy and operational cost without providing a required capability. App inventory and metadata can be sensitive, so the manifest uses only the minimum launcher-intent `<queries>` visibility needed for consumer-facing metadata. `QUERY_ALL_PACKAGES` is not requested, and individual application packages are not enumerated. Non-launchable or otherwise unresolved UsageStats packages still use their package name and a neutral icon.
