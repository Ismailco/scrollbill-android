package com.soultware.scrollbill.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
                containerColor = MaterialTheme.colorScheme.background,
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
                    ScrollBillUiState.NoUsageData -> EmptyState(Modifier.padding(paddingValues), viewModel::refresh)
                    ScrollBillUiState.RecoverableError -> ErrorState(Modifier.padding(paddingValues), viewModel::refresh)
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
                AppMark(Modifier.size(36.dp))
                Spacer(Modifier.width(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text("ScrollBill", fontWeight = FontWeight.Bold)
                    Text(
                        "PRIVATE SCREEN-TIME REPORT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background,
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
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 4.dp,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawCircle(Color.White.copy(alpha = 0.08f), radius = size.minDimension * 0.55f, center = androidx.compose.ui.geometry.Offset(size.width * 0.98f, 0f))
                            drawCircle(Color.White.copy(alpha = 0.06f), radius = size.minDimension * 0.28f, center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.82f))
                        }
                        .padding(22.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        AppMark(Modifier.size(62.dp))
                        Text(
                            "SEE WHERE\nYOUR TIME WENT.",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "A private weekly report made from the Usage Access history already on your phone.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.84f),
                        )
                        Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.14f)) {
                            Text(
                                "PRIVATE BY DESIGN",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AccessBenefit(number = "01", text = "See your last 7 completed days")
                AccessBenefit(number = "02", text = "Understand which apps took the most time")
                AccessBenefit(number = "03", text = "Create a shareable weekly receipt")
            }
        }
        item { PrivacyBanner() }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onGrantUsageAccess,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                    shape = RoundedCornerShape(17.dp),
                ) {
                    Text("Grant usage access")
                }
                Text(
                    "Android will open system settings. ScrollBill cannot read your history until access is enabled.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (settingsError) {
                    Text(
                        "Android settings could not be opened. Try again.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun AccessBenefit(number: String, text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(number, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PrivacyBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("↗", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Your usage data stays on this device.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("No account. No cloud upload. No tracking.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "loading-pulse")
    val haloColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    val alpha by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "loading-alpha",
    )
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppMark(Modifier.size(72.dp).drawBehind { drawCircle(haloColor, radius = size.minDimension * 0.65f) })
        Spacer(Modifier.height(22.dp))
        Text("Building your report", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text("Reading only the local history you approved.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        CircularProgressIndicator(modifier = Modifier.size(26.dp), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = alpha))
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
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("YOUR WEEK, IN VIEW", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text("Last 7 days", style = MaterialTheme.typography.headlineLarge)
                Text(formatDateRange(summary.reportingPeriod), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item { HeroSummary(summary) }
        item { SummaryCards(summary) }
        item {
            Button(
                onClick = onCreateReceipt,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.background,
                ),
            ) {
                Text("Create my receipt", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(10.dp))
                Text("↗", style = MaterialTheme.typography.titleLarge)
            }
        }
        item {
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Top applications", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Where your minutes went", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("TOP 5", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
        itemsIndexed(summary.rankedApplications.take(5), key = { _, app -> app.packageName }) { index, app ->
            AppUsageRow(rank = index + 1, app = app)
        }
        item {
            OutlinedButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Refresh report")
            }
        }
    }
}

@Composable
private fun HeroSummary(summary: UsageSummaryUi) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawCircle(Color.White.copy(alpha = 0.08f), radius = size.minDimension * 0.72f, center = androidx.compose.ui.geometry.Offset(size.width * 0.98f, 0f))
                drawCircle(Color.White.copy(alpha = 0.05f), radius = size.minDimension * 0.42f, center = androidx.compose.ui.geometry.Offset(size.width * 0.98f, 0f))
            },
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 5.dp,
    ) {
        Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("TOTAL SCREEN TIME", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f), modifier = Modifier.weight(1f))
                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.14f)) {
                    Text("7 DAYS", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            }
            Text(formatDuration(summary.totalDurationMillis), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            Text("A clear view of the time your apps asked for this week.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f))
        }
    }
}

@Composable
private fun SummaryCards(summary: UsageSummaryUi) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            SummaryCard("Daily average", formatDuration(summary.averageDailyDurationMillis), Modifier.weight(1f))
            SummaryCard("Projected / year", formatProjectedAnnualUsage(summary.projectedAnnualDurationMillis), Modifier.weight(1f))
        }
        summary.highestUsageApplication?.let { app ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(21.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("MOST-USED APP", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    AppUsageRow(rank = null, app = app, showDuration = false)
                    Text("Leading your weekly usage", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier) {
    Card(
        modifier = modifier.heightIn(min = 100.dp),
        shape = RoundedCornerShape(21.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun AppUsageRow(rank: Int?, app: AppUsageUi, showDuration: Boolean = true) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        rank?.let {
            Surface(modifier = Modifier.size(28.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Box(contentAlignment = Alignment.Center) {
                    Text("$it", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                }
            }
        }
        AppIcon(app.icon, app.displayLabel)
        Text(
            app.displayLabel,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (showDuration) {
            Text(formatDuration(app.foregroundDurationMillis), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AppIcon(icon: Bitmap?, label: String) {
    if (icon != null) {
        Image(
            painter = BitmapPainter(icon.asImageBitmap()),
            contentDescription = "$label app icon",
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)),
        )
    } else {
        Surface(
            modifier = Modifier.size(40.dp).semantics { contentDescription = "App icon unavailable for $label" },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier, onRefresh: () -> Unit) {
    MessageState(modifier, "No usage data yet", "Android has not provided enough usage history for the selected period.", "Refresh report", onRefresh)
}

@Composable
private fun ErrorState(modifier: Modifier, onRetry: () -> Unit) {
    MessageState(modifier, "We couldn't load your report", "Usage Access may have changed. Check access and try again.", "Retry", onRetry)
}

@Composable
private fun MessageState(modifier: Modifier, title: String, message: String, actionLabel: String, onAction: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, tonalElevation = 2.dp) {
            AppMark(Modifier.padding(16.dp).size(58.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAction, modifier = Modifier.heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) { Text(actionLabel) }
    }
}

private fun formatDateRange(period: UsagePeriod): String {
    val start = period.localStartDate
    val end = period.localEndExclusiveDate.minusDays(1)
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    return if (start.year == end.year) {
        "${DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()).format(start)} – ${formatter.format(end)}"
    } else {
        "${formatter.format(start)} – ${formatter.format(end)}"
    }
}

@Preview(showBackground = true)
@Composable
private fun AccessRequiredPreview() {
    ScrollBillTheme {
        AccessRequiredState(Modifier.fillMaxSize(), {}, false)
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
