package com.soultware.scrollbill

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.soultware.scrollbill.data.usage.AndroidPackageMetadataResolver
import com.soultware.scrollbill.data.usage.AndroidUsageAccessChecker
import com.soultware.scrollbill.data.usage.AndroidUsageStatsRepository
import com.soultware.scrollbill.domain.usage.UsageAggregator
import com.soultware.scrollbill.ui.ScrollBillApp
import com.soultware.scrollbill.ui.ScrollBillViewModel
import com.soultware.scrollbill.ui.ScrollBillViewModelFactory
import java.time.ZoneId

class MainActivity : ComponentActivity() {
    private val viewModel: ScrollBillViewModel by viewModels {
        val metadataResolver = AndroidPackageMetadataResolver(applicationContext)
        val usageAccessChecker = AndroidUsageAccessChecker(applicationContext)
        ScrollBillViewModelFactory(
            usageAccessChecker = usageAccessChecker,
            usageStatsRepository = AndroidUsageStatsRepository(
                context = applicationContext,
                ownPackageName = applicationContext.packageName,
                aggregator = UsageAggregator(),
                metadataResolver = metadataResolver,
            ),
            metadataResolver = metadataResolver,
            clock = java.time.Clock.systemDefaultZone(),
            zoneId = ZoneId.systemDefault(),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScrollBillApp(
                viewModel = viewModel,
                onGrantUsageAccess = ::openUsageAccessSettings,
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (isFinishing) return
        viewModel.onAppResumed()
    }

    private fun openUsageAccessSettings() {
        val usageAccessIntent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        try {
            startActivity(usageAccessIntent)
        } catch (_: ActivityNotFoundException) {
            try {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            } catch (_: ActivityNotFoundException) {
                viewModel.onAppResumed()
            }
        }
    }
}
