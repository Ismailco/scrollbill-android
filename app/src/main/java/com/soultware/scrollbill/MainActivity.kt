package com.soultware.scrollbill

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.soultware.scrollbill.data.usage.AndroidPackageMetadataResolver
import com.soultware.scrollbill.data.usage.AndroidUsageAccessChecker
import com.soultware.scrollbill.data.usage.AndroidUsageExclusionPolicyProvider
import com.soultware.scrollbill.data.usage.AndroidUsageStatsRepository
import com.soultware.scrollbill.domain.usage.UsageAggregator
import com.soultware.scrollbill.ui.ScrollBillApp
import com.soultware.scrollbill.ui.ScrollBillViewModel
import com.soultware.scrollbill.ui.ScrollBillViewModelFactory
import com.soultware.scrollbill.data.receipt.CacheReceiptFileStore
import com.soultware.scrollbill.ui.receipt.ClassicReceiptRenderer
import java.time.ZoneId

class MainActivity : ComponentActivity() {
    private val viewModel: ScrollBillViewModel by viewModels {
        val metadataResolver = AndroidPackageMetadataResolver(applicationContext)
        val usageAccessChecker = AndroidUsageAccessChecker(applicationContext)
        val exclusionPolicyProvider = AndroidUsageExclusionPolicyProvider(applicationContext)
        val receiptFileStore = CacheReceiptFileStore(applicationContext)
        ScrollBillViewModelFactory(
            usageAccessChecker = usageAccessChecker,
            usageStatsRepository = AndroidUsageStatsRepository(
                context = applicationContext,
                exclusionPolicyProvider = exclusionPolicyProvider,
                aggregator = UsageAggregator(),
            ),
            metadataResolver = metadataResolver,
            receiptRenderer = ClassicReceiptRenderer(),
            receiptFileStore = receiptFileStore,
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
                onShareReceipt = ::shareReceipt,
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
            viewModel.onUsageAccessSettingsOpened()
        } catch (_: ActivityNotFoundException) {
            try {
                startActivity(Intent(Settings.ACTION_SETTINGS))
                viewModel.onUsageAccessSettingsOpened()
            } catch (_: ActivityNotFoundException) {
                viewModel.onUsageAccessSettingsUnavailable()
            }
        }
    }

    private fun shareReceipt(uri: Uri): Boolean {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newRawUri("ScrollBill receipt", uri)
        }
        return try {
            startActivity(Intent.createChooser(shareIntent, "Share receipt"))
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }
}
