# ScrollBill Phase 0 architecture

## Current architecture

The single `app` module uses a small separation of concerns:

- `domain/model`: `UsagePeriod`, `AppUsage`, and `WeeklyUsageSummary` contain framework-free reporting data.
- `domain/usage`: `UsageAggregator` filters, groups, ranks, and calculates summary values from plain records.
- `data/usage`: Android AppOps access checking, `UsageStatsManager` queries, LauncherApps-first metadata resolution, and dynamic HOME exclusion resolution.
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

LauncherApps is queried for `Process.myUserHandle()` only. Its launchable activity list is sorted by activity class name so multiple launcher activities produce one deterministic application metadata result. PackageManager is the permitted fallback, followed by package-name and neutral-icon fallbacks.

Usage totals exclude ScrollBill, the dynamically resolved HOME package, `android`, and `com.android.systemui`. The exclusion policy is deliberately conservative: preinstalled/system status alone is not a reason to exclude a user-facing application. These totals are ScrollBill's aggregation and are not guaranteed to match Digital Wellbeing.

## Why no backend and no broad package visibility

Phase 0 needs only local system data and local calculation, so a backend would add privacy and operational cost without providing a required capability. `QUERY_ALL_PACKAGES` and manifest `<queries>` visibility expansion are intentionally avoided because app inventory and metadata can be sensitive. LauncherApps covers the ordinary current user's launchable applications; if neither local metadata mechanism resolves a recorded package, ScrollBill uses its package name and a neutral icon.
