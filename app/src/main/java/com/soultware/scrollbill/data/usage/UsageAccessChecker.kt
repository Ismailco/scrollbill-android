package com.soultware.scrollbill.data.usage

import android.app.AppOpsManager
import android.content.Context

interface UsageAccessChecker {
    fun hasUsageAccess(): Boolean
}

class AndroidUsageAccessChecker(
    private val context: Context,
) : UsageAccessChecker {
    override fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java)
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            context.applicationInfo.uid,
            context.packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
