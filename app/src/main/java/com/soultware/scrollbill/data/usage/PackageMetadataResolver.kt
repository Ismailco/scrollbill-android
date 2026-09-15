package com.soultware.scrollbill.data.usage

import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
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
    launcher: MetadataCandidate<T>?,
    packageManager: MetadataCandidate<T>?,
): MetadataCandidate<T> = MetadataCandidate(
    label = launcher?.label.takeMeaningful() ?: packageManager?.label.takeMeaningful() ?: packageName,
    icon = launcher?.icon ?: packageManager?.icon,
)

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

    override fun resolve(packageName: String): AppMetadata {
        val launcherMetadata = resolveFromLauncherApps(packageName)
        val packageManagerMetadata = resolveFromPackageManager(packageName)
        val merged = mergeMetadataCandidates(packageName, launcherMetadata, packageManagerMetadata)
        return AppMetadata(label = merged.label ?: packageName, icon = merged.icon)
    }

    private fun resolveFromLauncherApps(packageName: String): MetadataCandidate<Bitmap>? {
        val activityList = try {
            launcherApps?.getActivityList(packageName, currentUser).orEmpty()
        } catch (_: SecurityException) {
            emptyList()
        }

        return selectLauncherMetadata(activityList.map { activityInfo ->
            LauncherMetadataEntry(
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
        }
        val icon = try {
            packageManager.getApplicationIcon(applicationInfo).toBitmapSafely()
        } catch (_: SecurityException) {
            null
        }
        return MetadataCandidate(label = label, icon = icon)
    }
}

private fun LauncherActivityInfo.iconSafely(density: Int): Bitmap? = try {
    getIcon(density).toBitmapSafely()
} catch (_: SecurityException) {
    null
}

private fun Drawable.toBitmapSafely(): Bitmap {
    val width = intrinsicWidth.takeIf { it > 0 } ?: 48
    val height = intrinsicHeight.takeIf { it > 0 } ?: 48
    return toBitmap(width = width, height = height)
}
