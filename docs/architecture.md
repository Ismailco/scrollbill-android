# ScrollBill Phase 1 architecture

## Current architecture

The single `app` module uses a small separation of concerns:

- `domain/model`: `UsagePeriod`, `AppUsage`, and `WeeklyUsageSummary` contain framework-free reporting data.
- `domain/usage`: `UsageAggregator` filters, groups, ranks, and calculates summary values from plain records.
- `domain/receipt`: `ReceiptFactory` converts a loaded report and resolved display labels into an immutable `ReceiptSnapshot` with top-five and other-app reconciliation.
- `data/usage`: Android AppOps access checking, `UsageStatsManager` queries, launcher metadata indexing/resolution, and dynamic HOME exclusion resolution.
- `data/receipt`: `CacheReceiptFileStore` writes the current PNG into the dedicated app-cache directory and creates a FileProvider URI.
- `ui`: a `ScrollBillViewModel` exposes typed report, screen, and receipt-preview states; Compose renders access, loading, empty, error, dashboard, and receipt-preview states.
- `ui/receipt`: `ClassicReceiptRenderer` draws the fixed-size receipt Bitmap and receipt-specific formatting utilities.
- `ui/theme`: Material 3 light/dark color schemes.

The activity owns only system setup, the ViewModel factory, the settings intent fallback, and Compose content. No platform usage query occurs in a composable.

## Dependency direction

Domain code depends on Kotlin and `java.time` only. Android data code depends on the domain. The UI depends on domain models and data interfaces through the ViewModel. The Activity composes the concrete Android implementations manually; no DI framework is needed for this phase.

## Usage-data flow

The ViewModel checks AppOps on a background dispatcher. After access is granted, the repository computes the period, queries `UsageStatsManager.INTERVAL_DAILY` once for each completed local calendar day, obtains a fresh exclusion policy, and passes plain package/duration records to `UsageAggregator`. The ViewModel resolves labels and icons through the metadata resolver before exposing UI state. A loaded report retains the domain summary and resolved display labels; the receipt CTA maps those values to a snapshot, renders it on `Dispatchers.Default`, and exposes the same Bitmap to preview and PNG sharing.

## Period semantics

The primary period is `[today - 7 days at local date, today at local date)`. Each day is converted from local midnight to epoch milliseconds in the device timezone before its daily query. This is seven completed calendar days, not a rolling 168-hour interval, and remains correct across timezone offset changes.

Phase 1 receipts use this same seven-completed-day period only; other periods are future work.

## Privacy boundary

Usage data stays in process on the device. There is no network permission, client, backend, database, analytics, telemetry, or production logging of package durations. The only user-facing special access is `PACKAGE_USAGE_STATS`; AndroidX's internal dynamic-receiver signature permission may also appear in the merged manifest.

The manifest declares one narrow package-visibility query for `ACTION_MAIN` + `CATEGORY_LAUNCHER`. The metadata resolver uses `PackageManager.queryIntentActivities()` to build an in-memory, package-keyed index of ordinary launchable applications for the current user. Multiple launcher activities are sorted by activity class name and collapse to one deterministic application metadata result. `LauncherApps` is then used as a current-user supplemental source, followed by permitted direct `PackageManager` lookup, package-name fallback, and neutral-icon fallback. The launcher inventory is never persisted.

Usage totals exclude ScrollBill, the dynamically resolved HOME package, `android`, and `com.android.systemui`. The exclusion policy is deliberately conservative: preinstalled/system status alone is not a reason to exclude a user-facing application. These totals are ScrollBill's aggregation and are not guaranteed to match Digital Wellbeing.

## Receipt boundary

`ReceiptSnapshot` contains only the public image inputs: completed-period dates, durations, display labels, the top five entries, and reconciled `otherAppsUsageMillis`. It contains no package identifiers, Android framework objects, icons, contexts, or repositories. `ClassicReceiptRenderer` renders a 1080×1920 `ARGB_8888` Bitmap with Canvas; no Activity screenshot or device UI dimensions are involved. The preview displays that Bitmap directly, and the share path encodes the same instance as PNG.

The cache writer uses only `cacheDir/shared_receipts/scrollbill-weekly-receipt.png`. FileProvider exposes only the `shared_receipts/` cache path, is non-exported, and grants read access only through the user-triggered share Intent. A later generation overwrites the one ScrollBill receipt file rather than accumulating history.

## Why no backend and no broad package visibility

Phase 1 still needs only local system data, local calculation, and an explicitly user-selected local share. A backend would add privacy and operational cost without providing a required capability. App inventory and metadata can be sensitive, so the manifest uses only the minimum launcher-intent `<queries>` visibility needed for consumer-facing metadata. `QUERY_ALL_PACKAGES` is not requested, and individual application packages are not enumerated. Non-launchable or otherwise unresolved UsageStats packages still use their package name and a neutral icon. The receipt is not uploaded and is not stored outside app cache.
