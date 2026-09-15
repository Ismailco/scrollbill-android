package com.soultware.scrollbill.data.usage

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.soultware.scrollbill.domain.usage.UsageExclusionPolicy

interface UsageExclusionPolicyProvider {
    fun currentPolicy(): UsageExclusionPolicy
}

class AndroidUsageExclusionPolicyProvider(
    context: Context,
) : UsageExclusionPolicyProvider {
    private val packageManager = context.packageManager
    private val ownPackageName = context.packageName

    override fun currentPolicy(): UsageExclusionPolicy {
        val homePackageName = try {
            Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .resolveActivity(packageManager)
                ?.packageName
        } catch (_: SecurityException) {
            null
        }

        return UsageExclusionPolicy.forApplication(
            ownPackageName = ownPackageName,
            homePackageName = homePackageName,
        )
    }
}
