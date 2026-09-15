package com.soultware.scrollbill.ui

import android.graphics.Bitmap
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.soultware.scrollbill.domain.model.UsagePeriod
import com.soultware.scrollbill.ui.theme.ScrollBillTheme
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ScrollBillApp(
    viewModel: ScrollBillViewModel,
    onGrantUsageAccess: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ScrollBillTheme {
        Scaffold(
            topBar = {
                ScrollBillTopBar(
                    showRefresh = state is ScrollBillUiState.Loaded ||
                        state is ScrollBillUiState.NoUsageData ||
                        state is ScrollBillUiState.RecoverableError,
                    onRefresh = viewModel::refresh,
                )
            },
        ) { paddingValues ->
            when (val currentState = state) {
                ScrollBillUiState.CheckingPermission -> LoadingState(Modifier.padding(paddingValues))
                ScrollBillUiState.UsageAccessRequired -> AccessRequiredState(
                    modifier = Modifier.padding(paddingValues),
                    onGrantUsageAccess = onGrantUsageAccess,
                )
                ScrollBillUiState.Loading -> LoadingState(Modifier.padding(paddingValues))
                is ScrollBillUiState.Loaded -> DashboardState(
                    modifier = Modifier.padding(paddingValues),
                    summary = currentState.summary,
                    onRefresh = viewModel::refresh,
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
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ScrollBillTopBar(showRefresh: Boolean, onRefresh: () -> Unit) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppMark()
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
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@Composable
private fun AppMark() {
    Surface(
        modifier = Modifier.size(32.dp),
        shape = RoundedCornerShape(9.dp),
        color = MaterialTheme.colorScheme.primary,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "SB",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun AccessRequiredState(modifier: Modifier, onGrantUsageAccess: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("See where your time went.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(
            "ScrollBill uses Android's Usage Access to calculate a screen-time report from the apps you use.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Your usage data stays on this device. ScrollBill does not upload your usage history.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onGrantUsageAccess,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        ) {
            Text("Grant usage access")
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun DashboardState(modifier: Modifier, summary: UsageSummaryUi, onRefresh: () -> Unit) {
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
    ScrollBillTheme { AccessRequiredState(Modifier.fillMaxSize(), onGrantUsageAccess = {}) }
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
        )
    }
}
