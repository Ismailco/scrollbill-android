package com.soultware.scrollbill.data.usage

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap

data class AppMetadata(
    val label: String,
    val icon: Bitmap?,
)

interface PackageMetadataResolver {
    fun resolve(packageName: String): AppMetadata
}

class AndroidPackageMetadataResolver(
    context: Context,
) : PackageMetadataResolver {
    private val packageManager = context.packageManager

    override fun resolve(packageName: String): AppMetadata {
        val applicationInfo = try {
            packageManager.getApplicationInfo(packageName, 0)
        } catch (_: PackageManager.NameNotFoundException) {
            return AppMetadata(label = packageName, icon = null)
        }

        val label = try {
            packageManager.getApplicationLabel(applicationInfo).toString()
                .takeIf { it.isNotBlank() }
                ?: packageName
        } catch (_: SecurityException) {
            packageName
        }

        val icon = try {
            packageManager.getApplicationIcon(applicationInfo).toBitmapSafely()
        } catch (_: SecurityException) {
            null
        }

        return AppMetadata(label = label, icon = icon)
    }
}

private fun Drawable.toBitmapSafely(): Bitmap {
    val width = intrinsicWidth.takeIf { it > 0 } ?: 48
    val height = intrinsicHeight.takeIf { it > 0 } ?: 48
    return toBitmap(width = width, height = height)
}
