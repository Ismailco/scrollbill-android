package com.soultware.scrollbill.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.soultware.scrollbill.BuildConfig

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsScreen(
    usageAccessGranted: Boolean,
    onBack: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenUsageAccess: () -> Unit,
) {
    InfoScaffold(title = "Settings", onBack = onBack) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 34.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 4.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        InfoMark(Modifier.size(70.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("ScrollBill", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            Text("Version ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f))
                        }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("A quieter way to see your week.", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "ScrollBill turns your Android usage history into a simple weekly report and a shareable phone-time receipt.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                SettingsRow(
                    title = "Privacy",
                    description = "What ScrollBill reads, keeps, and shares",
                    leading = "01",
                    onClick = onOpenPrivacy,
                )
            }
            item {
                SettingsRow(
                    title = "Usage Access",
                    description = if (usageAccessGranted) "Granted on this device" else "Not granted",
                    leading = "02",
                    onClick = onOpenUsageAccess,
                    status = if (usageAccessGranted) "ON" else "OFF",
                )
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("LOCAL FIRST", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        Text("Your usage data stays on this device.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PrivacyScreen(onBack: () -> Unit) {
    InfoScaffold(title = "Privacy", onBack = onBack) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 34.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.large,
                ) {
                    Text(
                        "ScrollBill calculates your report locally. There is no account, cloud service, or automatic publishing.",
                        modifier = Modifier.padding(18.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            item { PrivacySection("01", "WHAT SCROLLBILL READS", "Android application usage history, plus application names and icons when Android makes that metadata available for report presentation.") }
            item { PrivacySection("02", "WHAT STAYS ON YOUR PHONE", "Usage history, app durations, and the current report are processed on this device. A generated receipt remains in app cache temporarily for sharing and is not saved to public storage.") }
            item { PrivacySection("03", "WHAT SCROLLBILL DOES NOT DO", "No account, advertising, analytics, cloud upload, location collection, or contact access.") }
            item { PrivacySection("04", "SHARING", "When you tap Share receipt, Android lets you choose where to send the generated image. ScrollBill does not automatically upload or publish it. The receiving app's handling is outside ScrollBill's control.") }
            item { PrivacySection("05", "USAGE ACCESS", "Android Usage Access is a special system setting required to read the history used for the weekly report. You can revoke it at any time from Android Settings.") }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun InfoScaffold(title: String, onBack: () -> Unit, content: @Composable (PaddingValues) -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        content = content,
    )
}

@Composable
private fun InfoMark(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(com.soultware.scrollbill.R.drawable.ic_scrollbill),
        contentDescription = null,
        modifier = modifier,
    )
}

@Composable
private fun SettingsRow(title: String, description: String, leading: String, onClick: () -> Unit, status: String? = null) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .semantics { role = Role.Button },
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Surface(shape = androidx.compose.foundation.shape.CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Text(leading, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            status?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            }
            Text("›", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PrivacySection(number: String, title: String, body: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(13.dp)) {
        Text(number, modifier = Modifier.padding(top = 2.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(body, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
