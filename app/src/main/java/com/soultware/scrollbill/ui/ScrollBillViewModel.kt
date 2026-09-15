package com.soultware.scrollbill.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.soultware.scrollbill.data.usage.PackageMetadataResolver
import com.soultware.scrollbill.data.usage.UsageAccessChecker
import com.soultware.scrollbill.data.usage.UsageStatsAccessException
import com.soultware.scrollbill.data.usage.UsageStatsRepository
import com.soultware.scrollbill.domain.model.AppUsage
import com.soultware.scrollbill.domain.model.UsagePeriod
import com.soultware.scrollbill.domain.model.WeeklyUsageSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.ZoneId

sealed interface ScrollBillUiState {
    data object CheckingPermission : ScrollBillUiState
    data object UsageAccessRequired : ScrollBillUiState
    data object Loading : ScrollBillUiState
    data class Loaded(val summary: UsageSummaryUi) : ScrollBillUiState
    data object NoUsageData : ScrollBillUiState
    data object RecoverableError : ScrollBillUiState
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
    private val clock: Clock,
    private val zoneId: ZoneId,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ScrollBillUiState>(ScrollBillUiState.CheckingPermission)
    val uiState: StateFlow<ScrollBillUiState> = _uiState.asStateFlow()
    private var refreshJob: Job? = null

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
                    _uiState.value = ScrollBillUiState.Loaded(summary.toUiModel())
                }
            } catch (_: UsageStatsAccessException) {
                _uiState.value = ScrollBillUiState.RecoverableError
            }
        }
    }

    fun onAppResumed() {
        refresh()
    }

    private suspend fun WeeklyUsageSummary.toUiModel(): UsageSummaryUi {
        val apps = withContext(Dispatchers.IO) {
            rankedApplications.map { app ->
                AppUsageUi(
                    packageName = app.packageName,
                    displayLabel = app.displayLabel,
                    foregroundDurationMillis = app.foregroundDurationMillis,
                    icon = metadataResolver.resolve(app.packageName).icon,
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
            clock = clock,
            zoneId = zoneId,
        ) as T
    }
}
