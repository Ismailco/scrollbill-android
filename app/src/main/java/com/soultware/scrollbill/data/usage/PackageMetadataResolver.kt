package com.soultware.scrollbill.data.usage

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Process
import androidx.core.graphics.drawable.toBitmap

data class AppMetadata(
    val label: String,
    val icon: Bitmap?,
)

internal data class MetadataCandidate<T>(
    val label: String?,
    val icon: T?,
)

internal data class LauncherMetadataEntry<T>(
    val packageName: String,
    val activityClassName: String,
    val metadata: MetadataCandidate<T>,
)

internal fun <T> selectLauncherMetadata(
    entries: List<LauncherMetadataEntry<T>>,
): MetadataCandidate<T>? = entries
    .sortedBy { it.activityClassName }
    .firstOrNull()
    ?.metadata

internal fun <T> mergeMetadataCandidates(
    packageName: String,
    vararg candidates: MetadataCandidate<T>?,
): MetadataCandidate<T> = MetadataCandidate(
    label = candidates.asSequence()
        .mapNotNull { it?.label.takeMeaningful() }
        .firstOrNull()
        ?: packageName,
    icon = candidates.asSequence()
        .mapNotNull { it?.icon }
        .firstOrNull(),
)

internal fun <T> buildLauncherMetadataIndex(
    entries: List<LauncherMetadataEntry<T>>,
): Map<String, MetadataCandidate<T>> = entries
    .groupBy { it.packageName }
    .mapNotNull { (packageName, packageEntries) ->
        selectLauncherMetadata(packageEntries)?.let { packageName to it }
    }
    .toMap()

private fun String?.takeMeaningful(): String? = this?.takeIf { it.isNotBlank() }

interface PackageMetadataResolver {
    fun resolve(packageName: String): AppMetadata
}

class AndroidPackageMetadataResolver(
    context: Context,
) : PackageMetadataResolver {
    private val packageManager = context.packageManager
    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val currentUser = Process.myUserHandle()
    private val iconDensity = context.resources.displayMetrics.densityDpi
    private val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    private val launcherMetadataIndex by lazy {
        buildLauncherMetadataIndex(
            queryLauncherActivities().mapNotNull { it.toLauncherMetadataEntry(packageManager) },
        )
    }

    override fun resolve(packageName: String): AppMetadata {
        val merged = mergeMetadataCandidates(
            packageName,
            launcherMetadataIndex[packageName],
            resolveFromLauncherApps(packageName),
            resolveFromPackageManager(packageName),
        )
        return AppMetadata(label = merged.label ?: packageName, icon = merged.icon)
    }

    @Suppress("DEPRECATION")
    private fun queryLauncherActivities(): List<ResolveInfo> = try {
        packageManager.queryIntentActivities(launcherIntent, 0)
    } catch (_: SecurityException) {
        emptyList()
    }

    private fun resolveFromLauncherApps(packageName: String): MetadataCandidate<Bitmap>? {
        val activityList = try {
            launcherApps?.getActivityList(packageName, currentUser).orEmpty()
        } catch (_: SecurityException) {
            emptyList()
        }

        return selectLauncherMetadata(activityList.map { activityInfo ->
            LauncherMetadataEntry(
                packageName = packageName,
                activityClassName = activityInfo.componentName.className,
                metadata = MetadataCandidate(
                    label = activityInfo.label?.toString(),
                    icon = activityInfo.iconSafely(iconDensity),
                ),
            )
        })
    }

    private fun resolveFromPackageManager(packageName: String): MetadataCandidate<Bitmap>? {
        val applicationInfo = try {
            packageManager.getApplicationInfo(packageName, 0)
        } catch (_: PackageManager.NameNotFoundException) {
            return null
        } catch (_: SecurityException) {
            return null
        }

        val label = try {
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (_: SecurityException) {
            null
        } catch (_: Resources.NotFoundException) {
            null
        }
        val icon = try {
            packageManager.getApplicationIcon(applicationInfo).toBitmapSafely()
        } catch (_: SecurityException) {
            null
        } catch (_: Resources.NotFoundException) {
            null
        }
        return MetadataCandidate(label = label, icon = icon)
    }
}

private fun ResolveInfo.toLauncherMetadataEntry(
    packageManager: PackageManager,
): LauncherMetadataEntry<Bitmap>? {
    val activityInfo = activityInfo ?: return null
    val packageName = activityInfo.packageName ?: return null
    val activityClassName = activityInfo.name ?: return null
    val label = try {
        loadLabel(packageManager).toString()
    } catch (_: SecurityException) {
        null
    } catch (_: Resources.NotFoundException) {
        null
    }
    val icon = try {
        loadIcon(packageManager).toBitmapSafely()
    } catch (_: SecurityException) {
        null
    } catch (_: Resources.NotFoundException) {
        null
    }
    return LauncherMetadataEntry(
        packageName = packageName,
        activityClassName = activityClassName,
        metadata = MetadataCandidate(label = label, icon = icon),
    )
}

private fun LauncherActivityInfo.iconSafely(density: Int): Bitmap? = try {
    getIcon(density).toBitmapSafely()
} catch (_: SecurityException) {
    null
} catch (_: Resources.NotFoundException) {
    null
}

private fun Drawable.toBitmapSafely(): Bitmap {
    val width = intrinsicWidth.takeIf { it > 0 } ?: 48
    val height = intrinsicHeight.takeIf { it > 0 } ?: 48
    return toBitmap(width = width, height = height)
}
