package com.soultware.scrollbill.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.soultware.scrollbill.data.receipt.ReceiptFileException
import com.soultware.scrollbill.data.receipt.ReceiptFileStore
import com.soultware.scrollbill.data.usage.PackageMetadataResolver
import com.soultware.scrollbill.data.usage.UsageAccessChecker
import com.soultware.scrollbill.data.usage.UsageStatsAccessException
import com.soultware.scrollbill.data.usage.UsageStatsRepository
import com.soultware.scrollbill.domain.model.UsagePeriod
import com.soultware.scrollbill.domain.model.WeeklyUsageSummary
import com.soultware.scrollbill.domain.receipt.ReceiptFactory
import com.soultware.scrollbill.domain.receipt.ReceiptSnapshot
import com.soultware.scrollbill.ui.receipt.ReceiptRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.ZoneId

sealed interface ScrollBillUiState {
    data object CheckingPermission : ScrollBillUiState
    data object UsageAccessRequired : ScrollBillUiState
    data object Loading : ScrollBillUiState
    data class Loaded(
        val summary: UsageSummaryUi,
        val report: WeeklyUsageSummary,
    ) : ScrollBillUiState
    data object NoUsageData : ScrollBillUiState
    data object RecoverableError : ScrollBillUiState
}

sealed interface ScrollBillScreen {
    data object Dashboard : ScrollBillScreen
    data object ReceiptPreview : ScrollBillScreen
}

sealed interface ReceiptPreviewUiState {
    data object Idle : ReceiptPreviewUiState
    data object Rendering : ReceiptPreviewUiState
    data class Ready(
        val snapshot: ReceiptSnapshot,
        val bitmap: Bitmap,
    ) : ReceiptPreviewUiState
    data class Sharing(
        val snapshot: ReceiptSnapshot,
        val bitmap: Bitmap,
    ) : ReceiptPreviewUiState
    data class ShareReady(
        val snapshot: ReceiptSnapshot,
        val bitmap: Bitmap,
        val uri: Uri,
    ) : ReceiptPreviewUiState
    data object RenderError : ReceiptPreviewUiState
    data class ShareError(
        val snapshot: ReceiptSnapshot,
        val bitmap: Bitmap,
    ) : ReceiptPreviewUiState
}

data class UsageSummaryUi(
    val reportingPeriod: UsagePeriod,
    val totalDurationMillis: Long,
    val averageDailyDurationMillis: Long,
    val projectedAnnualDurationMillis: Long,
    val highestUsageApplication: AppUsageUi?,
    val rankedApplications: List<AppUsageUi>,
)

data class AppUsageUi(
    val packageName: String,
    val displayLabel: String,
    val foregroundDurationMillis: Long,
    val icon: Bitmap?,
)

class ScrollBillViewModel(
    private val usageAccessChecker: UsageAccessChecker,
    private val usageStatsRepository: UsageStatsRepository,
    private val metadataResolver: PackageMetadataResolver,
    private val receiptRenderer: ReceiptRenderer,
    private val receiptFileStore: ReceiptFileStore,
    private val clock: Clock,
    private val zoneId: ZoneId,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ScrollBillUiState>(ScrollBillUiState.CheckingPermission)
    val uiState: StateFlow<ScrollBillUiState> = _uiState.asStateFlow()
    private val _screen = MutableStateFlow<ScrollBillScreen>(ScrollBillScreen.Dashboard)
    val screen: StateFlow<ScrollBillScreen> = _screen.asStateFlow()
    private val _receiptState = MutableStateFlow<ReceiptPreviewUiState>(ReceiptPreviewUiState.Idle)
    val receiptState: StateFlow<ReceiptPreviewUiState> = _receiptState.asStateFlow()
    private var refreshJob: Job? = null
    private var receiptJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _uiState.value = ScrollBillUiState.CheckingPermission
            val hasAccess = withContext(Dispatchers.IO) { usageAccessChecker.hasUsageAccess() }
            if (!hasAccess) {
                _uiState.value = ScrollBillUiState.UsageAccessRequired
                return@launch
            }

            _uiState.value = ScrollBillUiState.Loading
            val period = UsagePeriod.lastSevenCompletedDays(clock, zoneId)
            try {
                val summary = usageStatsRepository.loadWeeklySummary(period)
                if (summary.totalForegroundDurationMillis <= 0L || summary.rankedApplications.isEmpty()) {
                    _uiState.value = ScrollBillUiState.NoUsageData
                } else {
                    _uiState.value = ScrollBillUiState.Loaded(
                        summary = summary.toUiModel(),
                        report = summary,
                    )
                }
            } catch (_: UsageStatsAccessException) {
                _uiState.value = ScrollBillUiState.RecoverableError
            }
        }
    }

    fun onAppResumed() {
        if (_screen.value == ScrollBillScreen.Dashboard) {
            refresh()
        } else {
            viewModelScope.launch {
                val hasAccess = withContext(Dispatchers.IO) { usageAccessChecker.hasUsageAccess() }
                if (!hasAccess) {
                    closeReceiptPreview()
                    _uiState.value = ScrollBillUiState.UsageAccessRequired
                }
            }
        }
    }

    fun createReceipt() {
        val loaded = _uiState.value as? ScrollBillUiState.Loaded ?: return
        receiptJob?.cancel()
        _screen.value = ScrollBillScreen.ReceiptPreview
        _receiptState.value = ReceiptPreviewUiState.Rendering
        receiptJob = viewModelScope.launch(Dispatchers.Default) {
            try {
                val displayLabels = loaded.summary.rankedApplications.associate { app ->
                    app.packageName to app.displayLabel
                }
                val snapshot = ReceiptFactory.from(loaded.report, displayLabels)
                currentCoroutineContext().ensureActive()
                val bitmap = receiptRenderer.render(snapshot)
                currentCoroutineContext().ensureActive()
                _receiptState.value = ReceiptPreviewUiState.Ready(snapshot, bitmap)
            } catch (_: IllegalArgumentException) {
                _receiptState.value = ReceiptPreviewUiState.RenderError
            } catch (_: OutOfMemoryError) {
                _receiptState.value = ReceiptPreviewUiState.RenderError
            }
        }
    }

    fun retryReceipt() {
        if (_screen.value == ScrollBillScreen.ReceiptPreview) {
            createReceipt()
        }
    }

    fun closeReceiptPreview() {
        receiptJob?.cancel()
        receiptJob = null
        _receiptState.value = ReceiptPreviewUiState.Idle
        _screen.value = ScrollBillScreen.Dashboard
    }

    fun shareReceipt() {
        val ready = when (val state = _receiptState.value) {
            is ReceiptPreviewUiState.Ready -> state
            is ReceiptPreviewUiState.ShareError -> ReceiptPreviewUiState.Ready(state.snapshot, state.bitmap)
            else -> return
        }
        receiptJob?.cancel()
        _receiptState.value = ReceiptPreviewUiState.Sharing(ready.snapshot, ready.bitmap)
        receiptJob = viewModelScope.launch {
            try {
                val file = receiptFileStore.writePng(ready.bitmap)
                val uri = receiptFileStore.contentUri(file)
                _receiptState.value = ReceiptPreviewUiState.ShareReady(ready.snapshot, ready.bitmap, uri)
            } catch (_: ReceiptFileException) {
                _receiptState.value = ReceiptPreviewUiState.ShareError(ready.snapshot, ready.bitmap)
            } catch (_: OutOfMemoryError) {
                _receiptState.value = ReceiptPreviewUiState.ShareError(ready.snapshot, ready.bitmap)
            }
        }
    }

    fun onShareIntentLaunched() {
        val state = _receiptState.value
        if (state is ReceiptPreviewUiState.ShareReady) {
            _receiptState.value = ReceiptPreviewUiState.Ready(state.snapshot, state.bitmap)
        }
    }

    fun onShareIntentUnavailable() {
        val state = _receiptState.value
        if (state is ReceiptPreviewUiState.ShareReady) {
            _receiptState.value = ReceiptPreviewUiState.ShareError(state.snapshot, state.bitmap)
        }
    }

    private suspend fun WeeklyUsageSummary.toUiModel(): UsageSummaryUi {
        val apps = withContext(Dispatchers.IO) {
            rankedApplications.map { app ->
                val metadata = metadataResolver.resolve(app.packageName)
                AppUsageUi(
                    packageName = app.packageName,
                    displayLabel = metadata.label,
                    foregroundDurationMillis = app.foregroundDurationMillis,
                    icon = metadata.icon,
                )
            }
        }
        return UsageSummaryUi(
            reportingPeriod = reportingPeriod,
            totalDurationMillis = totalForegroundDurationMillis,
            averageDailyDurationMillis = averageDailyDurationMillis,
            projectedAnnualDurationMillis = projectedAnnualDurationMillis,
            highestUsageApplication = apps.firstOrNull(),
            rankedApplications = apps,
        )
    }
}

class ScrollBillViewModelFactory(
    private val usageAccessChecker: UsageAccessChecker,
    private val usageStatsRepository: UsageStatsRepository,
    private val metadataResolver: PackageMetadataResolver,
    private val receiptRenderer: ReceiptRenderer,
    private val receiptFileStore: ReceiptFileStore,
    private val clock: Clock,
    private val zoneId: ZoneId,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ScrollBillViewModel::class.java))
        return ScrollBillViewModel(
            usageAccessChecker = usageAccessChecker,
            usageStatsRepository = usageStatsRepository,
            metadataResolver = metadataResolver,
            receiptRenderer = receiptRenderer,
            receiptFileStore = receiptFileStore,
            clock = clock,
            zoneId = zoneId,
        ) as T
    }
}
