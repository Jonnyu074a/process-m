package com.jonnyuun.processm.feature.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonnyuun.processm.core.common.formatBytes
import com.jonnyuun.processm.core.common.formatFraction
import com.jonnyuun.processm.core.common.formatLoad
import com.jonnyuun.processm.core.common.formatPercent
import com.jonnyuun.processm.core.designsystem.component.MetricCard
import com.jonnyuun.processm.core.model.CpuSnapshot
import com.jonnyuun.processm.core.model.MemorySnapshot
import com.jonnyuun.processm.core.model.Metric
import com.jonnyuun.processm.core.model.StorageSnapshot
import com.jonnyuun.processm.core.model.UnavailableReason
import com.jonnyuun.processm.domain.MetricsRepository
import com.jonnyuun.processm.feature.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun OverviewRoute(repository: MetricsRepository) {
    val viewModel: OverviewViewModel = viewModel(
        factory = OverviewViewModel.factory(repository),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    OverviewScreen(state = state)
}

@Composable
fun OverviewScreen(state: OverviewUiState) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = stringResource(R.string.app_title),
                subtitle = stringResource(R.string.app_subtitle),
            )
        },
        bottomBar = {
            ProcessMBottomBar(selected = selectedTab, onSelect = { selectedTab = it })
        },
    ) { padding ->
        if (selectedTab == 0) {
            OverviewContent(
                state = state,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        } else {
            PlaceholderContent(
                title = tabLabel(selectedTab),
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        }
    }
}

@Composable
private fun OverviewContent(state: OverviewUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.overview_section),
            fontSize = 13.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )

        MemoryCard(state.memory)
        CpuCard(state.cpu)
        StorageCard(state.storage)

        val updated = state.lastUpdate?.let(::formatTime)
        Text(
            text = if (updated != null) {
                stringResource(R.string.updated_at, updated)
            } else {
                stringResource(R.string.waiting_first_sample)
            },
            fontSize = 11.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
private fun MemoryCard(metric: Metric<MemorySnapshot>?) {
    val title = stringResource(R.string.memory_title)
    val snapshot = metric.availableValue()

    if (snapshot != null) {
        MetricCard(
            title = title,
            value = formatBytes(snapshot.usedBytes),
            caption = stringResource(
                R.string.memory_caption,
                formatBytes(snapshot.totalBytes),
                formatFraction(snapshot.usedFraction),
            ),
            badge = stringResource(R.string.metric_measured),
            source = sourceLine(metric),
        )
    } else {
        UnavailableCard(title = title, metric = metric)
    }
}

@Composable
private fun CpuCard(metric: Metric<CpuSnapshot>?) {
    val title = stringResource(R.string.cpu_title)
    val snapshot = metric.availableValue()

    if (snapshot != null) {
        val cores = stringResource(R.string.cpu_cores, snapshot.perCorePercent.size)
        val load = snapshot.loadAverage1m?.let {
            stringResource(R.string.cpu_load, formatLoad(it))
        }
        MetricCard(
            title = title,
            value = formatPercent(snapshot.usagePercent),
            caption = listOfNotNull(cores, load).joinToString(" · "),
            badge = stringResource(R.string.metric_calculated),
            source = sourceLine(metric),
        )
    } else {
        UnavailableCard(title = title, metric = metric)
    }
}

@Composable
private fun StorageCard(metric: Metric<StorageSnapshot>?) {
    val title = stringResource(R.string.storage_title)
    val snapshot = metric.availableValue()

    if (snapshot != null) {
        MetricCard(
            title = title,
            value = formatBytes(snapshot.usedBytes),
            caption = stringResource(
                R.string.storage_caption,
                formatBytes(snapshot.totalBytes),
                formatFraction(snapshot.usedFraction),
            ),
            badge = stringResource(R.string.metric_measured),
            source = sourceLine(metric),
        )
    } else {
        UnavailableCard(title = title, metric = metric)
    }
}

@Composable
private fun UnavailableCard(title: String, metric: Metric<*>?) {
    val unavailable = metric as? Metric.Unavailable

    if (unavailable == null) {
        MetricCard(
            title = title,
            value = stringResource(R.string.measuring),
        )
    } else {
        val isCollecting = unavailable.reason == UnavailableReason.COLLECTING
        MetricCard(
            title = title,
            value = stringResource(if (isCollecting) R.string.measuring else R.string.unavailable),
            caption = reasonText(unavailable),
            source = sourceLine(unavailable),
        )
    }
}

@Composable
private fun reasonText(unavailable: Metric.Unavailable): String {
    val base = when (unavailable.reason) {
        UnavailableReason.NOT_SUPPORTED -> stringResource(R.string.reason_not_supported)
        UnavailableReason.PERMISSION_REQUIRED -> stringResource(R.string.reason_permission_required)
        UnavailableReason.ACCESS_LEVEL_REQUIRED -> stringResource(R.string.reason_access_level_required)
        UnavailableReason.NOT_READABLE -> stringResource(R.string.reason_not_readable)
        UnavailableReason.COLLECTING -> stringResource(R.string.reason_collecting)
        UnavailableReason.READ_ERROR -> stringResource(R.string.reason_read_error)
    }
    return unavailable.detail?.let { "$base · $it" } ?: base
}

@Composable
private fun sourceLine(metric: Metric<*>?): String? =
    metric?.let { stringResource(R.string.source_format, it.source.label) }

@Composable
private fun ProcessMBottomBar(selected: Int, onSelect: (Int) -> Unit) {
    val tabs = listOf(
        R.string.nav_overview,
        R.string.nav_apps,
        R.string.nav_processes,
        R.string.nav_monitor,
        R.string.nav_settings,
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MiuixTheme.colorScheme.surface)
            .navigationBarsPadding()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEachIndexed { index, labelRes ->
            Text(
                text = stringResource(labelRes),
                fontSize = 11.sp,
                fontWeight = if (index == selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (index == selected) {
                    MiuixTheme.colorScheme.primary
                } else {
                    MiuixTheme.colorScheme.onSurfaceVariantSummary
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelect(index) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun PlaceholderContent(title: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MiuixTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.placeholder_body),
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 14.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun tabLabel(index: Int): String = stringResource(
    when (index) {
        1 -> R.string.nav_apps
        2 -> R.string.nav_processes
        3 -> R.string.nav_monitor
        else -> R.string.nav_settings
    },
)

private fun formatTime(instant: Instant): String =
    DateTimeFormatter.ofPattern("HH:mm:ss")
        .withZone(ZoneId.systemDefault())
        .format(instant)

@Suppress("UNCHECKED_CAST")
private fun <T> Metric<T>?.availableValue(): T? =
    (this as? Metric.Available<*>)?.value as T?
