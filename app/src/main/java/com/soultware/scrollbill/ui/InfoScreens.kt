package com.soultware.scrollbill.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("ScrollBill", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Version ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            item {
                Text("About ScrollBill", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    "ScrollBill turns your Android usage history into a simple weekly report and a shareable phone-time receipt.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            item { HorizontalDivider() }
            item {
                SettingsRow(
                    title = "Privacy",
                    description = "What ScrollBill reads, keeps, and shares",
                    onClick = onOpenPrivacy,
                )
            }
            item {
                SettingsRow(
                    title = "Usage Access",
                    description = if (usageAccessGranted) "Granted" else "Not granted",
                    onClick = onOpenUsageAccess,
                )
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
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
        }
    }
}

@Composable
private fun SettingsRow(title: String, description: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clickable(onClick = onClick)
            .semantics { role = Role.Button },
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PrivacyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            item {
                Text(
                    "ScrollBill calculates your report locally. There is no account, cloud service, or automatic publishing.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            item {
                PrivacySection(
                    title = "WHAT SCROLLBILL READS",
                    body = "Android application usage history, plus application names and icons when Android makes that metadata available for report presentation.",
                )
            }
            item {
                PrivacySection(
                    title = "WHAT STAYS ON YOUR PHONE",
                    body = "Usage history, app durations, and the current report are processed on this device. A generated receipt remains in app cache temporarily for sharing and is not saved to public storage.",
                )
            }
            item {
                PrivacySection(
                    title = "WHAT SCROLLBILL DOES NOT DO",
                    body = "No account, advertising, analytics, cloud upload, location collection, or contact access.",
                )
            }
            item {
                PrivacySection(
                    title = "SHARING",
                    body = "When you tap Share receipt, Android lets you choose where to send the generated image. ScrollBill does not automatically upload or publish it. The receiving app's handling is outside ScrollBill's control.",
                )
            }
            item {
                PrivacySection(
                    title = "USAGE ACCESS",
                    body = "Android Usage Access is a special system setting required to read the history used for the weekly report. You can revoke it at any time from Android Settings.",
                )
            }
        }
    }
}

@Composable
private fun PrivacySection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        Text(body, style = MaterialTheme.typography.bodyLarge)
    }
}
