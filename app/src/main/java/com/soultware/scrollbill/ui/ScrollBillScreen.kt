package com.soultware.scrollbill.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.soultware.scrollbill.domain.model.UsagePeriod
import com.soultware.scrollbill.ui.receipt.ReceiptPreviewScreen
import com.soultware.scrollbill.ui.theme.ScrollBillTheme
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ScrollBillApp(
    viewModel: ScrollBillViewModel,
    onGrantUsageAccess: () -> Unit,
    onShareReceipt: (Uri) -> Boolean,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val screen by viewModel.screen.collectAsStateWithLifecycle()
    val receiptState by viewModel.receiptState.collectAsStateWithLifecycle()
    val usageAccessGranted by viewModel.usageAccessGranted.collectAsStateWithLifecycle()
    val usageAccessSettingsError by viewModel.usageAccessSettingsError.collectAsStateWithLifecycle()

    if (screen == ScrollBillScreen.Settings || screen == ScrollBillScreen.Privacy) {
        BackHandler(onBack = viewModel::closeInfoScreen)
    }

    LaunchedEffect(receiptState) {
        val shareReady = receiptState as? ReceiptPreviewUiState.ShareReady ?: return@LaunchedEffect
        if (onShareReceipt(shareReady.uri)) {
            viewModel.onShareIntentLaunched()
        } else {
            viewModel.onShareIntentUnavailable()
        }
    }

    ScrollBillTheme {
        when (screen) {
            ScrollBillScreen.Dashboard -> Scaffold(
                topBar = {
                    ScrollBillTopBar(
                        showRefresh = state is ScrollBillUiState.Loaded ||
                            state is ScrollBillUiState.NoUsageData ||
                            state is ScrollBillUiState.RecoverableError,
                        showSettings = state !is ScrollBillUiState.CheckingPermission &&
                            state !is ScrollBillUiState.UsageAccessRequired,
                        onRefresh = viewModel::refresh,
                        onSettings = viewModel::openSettings,
                    )
                },
            ) { paddingValues ->
                when (val currentState = state) {
                    ScrollBillUiState.CheckingPermission -> LoadingState(Modifier.padding(paddingValues))
                    ScrollBillUiState.UsageAccessRequired -> AccessRequiredState(
                        modifier = Modifier.padding(paddingValues),
                        onGrantUsageAccess = onGrantUsageAccess,
                        settingsError = usageAccessSettingsError,
                    )
                    ScrollBillUiState.Loading -> LoadingState(Modifier.padding(paddingValues))
                    is ScrollBillUiState.Loaded -> DashboardState(
                        modifier = Modifier.padding(paddingValues),
                        summary = currentState.summary,
                        onRefresh = viewModel::refresh,
                        onCreateReceipt = viewModel::createReceipt,
                    )
                    ScrollBillUiState.NoUsageData -> EmptyState(
                        modifier = Modifier.padding(paddingValues),
                        onRefresh = viewModel::refresh,
                    )
                    ScrollBillUiState.RecoverableError -> ErrorState(
                        modifier = Modifier.padding(paddingValues),
                        onRetry = viewModel::refresh,
                    )
                }
            }
            ScrollBillScreen.ReceiptPreview -> ReceiptPreviewScreen(
                state = receiptState,
                onBack = viewModel::closeReceiptPreview,
                onShare = viewModel::shareReceipt,
                onRetry = viewModel::retryReceipt,
            )
            ScrollBillScreen.Settings -> SettingsScreen(
                usageAccessGranted = usageAccessGranted,
                onBack = viewModel::closeInfoScreen,
                onOpenPrivacy = viewModel::openPrivacy,
                onOpenUsageAccess = onGrantUsageAccess,
            )
            ScrollBillScreen.Privacy -> PrivacyScreen(onBack = viewModel::closeInfoScreen)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ScrollBillTopBar(
    showRefresh: Boolean,
    showSettings: Boolean,
    onRefresh: () -> Unit,
    onSettings: () -> Unit,
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppMark(Modifier.size(32.dp))
                Spacer(Modifier.width(10.dp))
                Text("ScrollBill", fontWeight = FontWeight.SemiBold)
            }
        },
        actions = {
            if (showRefresh) {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh report")
                }
            }
            if (showSettings) {
                IconButton(onClick = onSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@Composable
private fun AppMark(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(com.soultware.scrollbill.R.drawable.ic_scrollbill),
        contentDescription = null,
        modifier = modifier,
    )
}

@Composable
private fun AccessRequiredState(
    modifier: Modifier,
    onGrantUsageAccess: () -> Unit,
    settingsError: Boolean,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AppMark(Modifier.size(64.dp))
                Text("See where your time went.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    "ScrollBill reads Android's Usage Access history to create a private weekly report and shareable receipt.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AccessBenefit("See your last 7 completed days")
                AccessBenefit("Understand which apps took the most time")
                AccessBenefit("Create a shareable weekly receipt")
            }
        }
        item {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    "Your usage data stays on this device.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onGrantUsageAccess,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                ) {
                    Text("Grant usage access")
                }
                Text(
                    "Android will open a system settings screen. ScrollBill cannot read your usage history until access is enabled.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (settingsError) {
                    Text(
                        "Android settings could not be opened. Try again.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun AccessBenefit(text: String) {
    Card {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun DashboardState(
    modifier: Modifier,
    summary: UsageSummaryUi,
    onRefresh: () -> Unit,
    onCreateReceipt: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column {
                Spacer(Modifier.height(8.dp))
                Text("Last 7 days", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    formatDateRange(summary.reportingPeriod),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item { SummaryCards(summary) }
        item {
            Button(onClick = onCreateReceipt, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                Text("Create my receipt")
            }
        }
        item {
            Text("Top applications", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
        itemsIndexed(summary.rankedApplications.take(5), key = { _, app -> app.packageName }) { index, app ->
            AppUsageRow(rank = index + 1, app = app)
        }
        item {
            Button(onClick = onRefresh, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Refresh report")
            }
        }
    }
}

@Composable
private fun SummaryCards(summary: UsageSummaryUi) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            SummaryCard("Total screen time", formatDuration(summary.totalDurationMillis), Modifier.weight(1f))
            SummaryCard("Daily average", formatDuration(summary.averageDailyDurationMillis), Modifier.weight(1f))
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(18.dp)) {
                Text("Projected phone use", style = MaterialTheme.typography.labelLarge)
                Text(
                    formatProjectedAnnualUsage(summary.projectedAnnualDurationMillis),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "A projection based on this week's daily average.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        summary.highestUsageApplication?.let { app ->
            Card {
                Column(Modifier.padding(18.dp)) {
                    Text("Most-used app", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(8.dp))
                    AppUsageRow(rank = null, app = app, showDuration = false)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AppUsageRow(rank: Int?, app: AppUsageUi, showDuration: Boolean = true) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        rank?.let {
            Text(
                "$it",
                modifier = Modifier.width(28.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AppIcon(app.icon, app.displayLabel)
        Spacer(Modifier.width(12.dp))
        Text(
            app.displayLabel,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 2,
        )
        if (showDuration) {
            Spacer(Modifier.width(12.dp))
            Text(formatDuration(app.foregroundDurationMillis), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun AppIcon(icon: Bitmap?, label: String) {
    if (icon != null) {
        Image(
            painter = BitmapPainter(icon.asImageBitmap()),
            contentDescription = "$label app icon",
            modifier = Modifier.size(40.dp),
        )
    } else {
        Surface(
            modifier = Modifier.size(40.dp).semantics { contentDescription = "App icon unavailable for $label" },
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier, onRefresh: () -> Unit) {
    MessageState(
        modifier = modifier,
        title = "No usage data yet",
        message = "Android has not provided enough usage history for the selected period.",
        actionLabel = "Refresh report",
        onAction = onRefresh,
    )
}

@Composable
private fun ErrorState(modifier: Modifier, onRetry: () -> Unit) {
    MessageState(
        modifier = modifier,
        title = "We couldn't load your report",
        message = "Usage Access may have changed. Check access and try again.",
        actionLabel = "Retry",
        onAction = onRetry,
    )
}

@Composable
private fun MessageState(modifier: Modifier, title: String, message: String, actionLabel: String, onAction: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAction, modifier = Modifier.heightIn(min = 52.dp)) { Text(actionLabel) }
    }
}

private fun formatDateRange(period: UsagePeriod): String {
    val start = period.localStartDate
    val end = period.localEndExclusiveDate.minusDays(1)
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    return if (start.year == end.year) {
        "${DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()).format(start)} – " +
            formatter.format(end)
    } else {
        "${formatter.format(start)} – ${formatter.format(end)}"
    }
}

@Preview(showBackground = true)
@Composable
private fun AccessRequiredPreview() {
    ScrollBillTheme {
        AccessRequiredState(
            modifier = Modifier.fillMaxSize(),
            onGrantUsageAccess = {},
            settingsError = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardPreview() {
    val period = UsagePeriod(LocalDate.of(2026, 9, 8), LocalDate.of(2026, 9, 15), ZoneId.of("UTC"))
    val sampleApps = listOf(
        AppUsageUi("com.example.video", "Example Video", 3_900_000L, null),
        AppUsageUi("com.example.chat", "Example Chat", 2_640_000L, null),
    )
    ScrollBillTheme {
        DashboardState(
            modifier = Modifier.fillMaxSize(),
            summary = UsageSummaryUi(period, 6_540_000L, 934_285L, 48_171_000_000L, sampleApps.first(), sampleApps),
            onRefresh = {},
            onCreateReceipt = {},
        )
    }
}
