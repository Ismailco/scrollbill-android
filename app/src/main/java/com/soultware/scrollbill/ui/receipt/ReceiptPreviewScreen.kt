package com.soultware.scrollbill.ui.receipt

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.soultware.scrollbill.ui.ReceiptPreviewUiState

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ReceiptPreviewScreen(
    state: ReceiptPreviewUiState,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onRetry: () -> Unit,
) {
    BackHandler(onBack = onBack)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Weekly receipt", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (state) {
                ReceiptPreviewUiState.Idle -> PreviewError(
                    message = "This receipt is no longer available.",
                    actionLabel = "Back",
                    onAction = onBack,
                )

                ReceiptPreviewUiState.Rendering -> LoadingPreview()

                is ReceiptPreviewUiState.Ready -> ReceiptImageAndAction(
                    bitmap = state.bitmap,
                    actionLabel = "Share receipt",
                    onAction = onShare,
                )

                is ReceiptPreviewUiState.Sharing -> ReceiptImageAndAction(
                    bitmap = state.bitmap,
                    actionLabel = "Preparing image…",
                    onAction = {},
                    actionEnabled = false,
                )

                is ReceiptPreviewUiState.ShareReady -> ReceiptImageAndAction(
                    bitmap = state.bitmap,
                    actionLabel = "Share receipt",
                    onAction = onShare,
                )

                ReceiptPreviewUiState.RenderError -> PreviewError(
                    message = "We couldn't create your receipt.",
                    actionLabel = "Try again",
                    onAction = onRetry,
                )

                is ReceiptPreviewUiState.ShareError -> ReceiptImageAndAction(
                    bitmap = state.bitmap,
                    actionLabel = "Try sharing again",
                    onAction = onShare,
                    errorMessage = "The receipt could not be prepared for sharing.",
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.LoadingPreview() {
    Box(
        modifier = Modifier.weight(1f).fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ColumnScope.ReceiptImageAndAction(
    bitmap: android.graphics.Bitmap,
    actionLabel: String,
    onAction: () -> Unit,
    actionEnabled: Boolean = true,
    errorMessage: String? = null,
) {
    Box(
        modifier = Modifier.weight(1f).fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            bitmap.asImageBitmap(),
            contentDescription = "Weekly ScrollBill receipt",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
    }
    errorMessage?.let {
        Text(
            text = it,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
    }
    Button(
        onClick = onAction,
        enabled = actionEnabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Text(actionLabel)
    }
}

@Composable
private fun ColumnScope.PreviewError(message: String, actionLabel: String, onAction: () -> Unit) {
    Column(
        modifier = Modifier.weight(1f).fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Button(onClick = onAction, modifier = Modifier.padding(top = 20.dp).heightIn(min = 52.dp)) {
            Text(actionLabel)
        }
    }
}
