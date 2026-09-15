# ScrollBill Phase 0 architecture

## Current architecture

The single `app` module uses a small separation of concerns:

- `domain/model`: `UsagePeriod`, `AppUsage`, and `WeeklyUsageSummary` contain framework-free reporting data.
- `domain/usage`: `UsageAggregator` filters, groups, ranks, and calculates summary values from plain records.
- `data/usage`: Android AppOps access checking, `UsageStatsManager` queries, and `PackageManager` metadata resolution.
- `ui`: a `ScrollBillViewModel` exposes a typed `StateFlow`; Compose renders access, loading, empty, error, and dashboard states.
- `ui/theme`: Material 3 light/dark color schemes.

The activity owns only system setup, the ViewModel factory, the settings intent fallback, and Compose content. No platform usage query occurs in a composable.

## Dependency direction

Domain code depends on Kotlin and `java.time` only. Android data code depends on the domain. The UI depends on domain models and data interfaces through the ViewModel. The Activity composes the concrete Android implementations manually; no DI framework is needed for this phase.

## Usage-data flow

The ViewModel checks AppOps on a background dispatcher. After access is granted, the repository computes the period, queries `UsageStatsManager.INTERVAL_DAILY` once for each completed local calendar day, and passes plain package/duration records to `UsageAggregator`. The repository resolves labels and the ViewModel resolves optional icons with safe fallbacks before exposing UI state.

## Period semantics

The primary period is `[today - 7 days at local date, today at local date)`. Each day is converted from local midnight to epoch milliseconds in the device timezone before its daily query. This is seven completed calendar days, not a rolling 168-hour interval, and remains correct across timezone offset changes.

## Privacy boundary

Usage data stays in process on the device. There is no network permission, client, backend, database, analytics, telemetry, or production logging of package durations. The only user-facing special access is `PACKAGE_USAGE_STATS`; AndroidX's internal dynamic-receiver signature permission may also appear in the merged manifest.

## Why no backend and no broad package visibility

Phase 0 needs only local system data and local calculation, so a backend would add privacy and operational cost without providing a required capability. `QUERY_ALL_PACKAGES` is intentionally avoided because app inventory and metadata can be sensitive. If `PackageManager` cannot resolve a recorded package, ScrollBill uses its package name and a neutral icon instead of broadening visibility.
