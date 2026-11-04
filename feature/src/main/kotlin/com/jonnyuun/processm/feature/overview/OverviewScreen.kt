package com.jonnyuun.processm.feature.overview

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jonnyuun.processm.feature.R
import com.jonnyuun.processm.feature.settings.SettingsScreen
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
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
import com.jonnyuun.processm.domain.AppsRepository
import com.jonnyuun.processm.domain.MetricsRepository
import com.jonnyuun.processm.domain.SettingsRepository
import org.koin.compose.koinInject
import com.jonnyuun.processm.feature.apps.AppsScreen
import com.jonnyuun.processm.feature.apps.AppsViewModel
import com.jonnyuun.processm.feature.device.DeviceScreen
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun OverviewRoute(repository: MetricsRepository, settings: SettingsRepository, appsRepository: AppsRepository) {
    val viewModel: OverviewViewModel = viewModel(
        factory = OverviewViewModel.factory(repository, settings),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    OverviewScreen(state = state, appsRepository = appsRepository)
}

@Composable
fun OverviewScreen(state: OverviewUiState, appsRepository: AppsRepository) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val settings = koinInject<com.jonnyuun.processm.domain.SettingsRepository>()
    val appsViewModel: AppsViewModel = remember {
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory = AppsViewModel.factory(appsRepository),
        )
    }
    val appsState by appsViewModel.state.collectAsStateWithLifecycle()

    if (showSettings) {
        SettingsScreen(onBack = { showSettings = false }, settings = settings)
        return
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = stringResource(R.string.app_title),
                subtitle = stringResource(R.string.app_subtitle),
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_nav_settings),
                            contentDescription = stringResource(R.string.settings_title),
                        )
                    }
                },
            )
        },
        bottomBar = {
            ProcessMBottomBar(selected = selectedTab, onSelect = { selectedTab = it })
        },
    ) { padding ->
        when (selectedTab) {
            0 -> OverviewContent(
                state = state,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding),
            )
            2 -> AppsScreen(
                state = appsState,
                onQueryChanged = { appsViewModel.onQueryChanged(it) },
            )
            4 -> DeviceScreen(
                state = state,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding),
            )
            else -> PlaceholderContent(
                title = tabLabel(selectedTab),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding),
            )
        }
    }
}
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val battery = state.battery?.availableValue()

        Text(
            text = buildString {
                append("${Build.MANUFACTURER} ${Build.MODEL}".trim())
                if (battery != null) append(" · Batería ${battery.levelPercent}%")
            },
            fontSize = 13.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )

        Text(
            text = stringResource(R.string.overview_section),
            fontSize = 13.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )

        val context = LocalContext.current
        val prefs = context.getSharedPreferences("droidsypm", Context.MODE_PRIVATE)
        var onboardingDismissed by remember { mutableStateOf(prefs.getBoolean("onboarding_dismissed", false)) }
        if (!onboardingDismissed) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                insideMargin = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_title),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.onboarding_body),
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceSecondary,
                )
                Spacer(Modifier.height(8.dp))
                TextButton(
                    text = stringResource(R.string.onboarding_dismiss),
                    onClick = {
                        onboardingDismissed = true
                        prefs.edit().putBoolean("onboarding_dismissed", true).apply()
                    },
                )
            }
        }

        MemoryCard(state.memory, state.memoryHistory)
        CpuCard(state.cpu, state.cpuHistory)
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
fun MemoryCard(metric: Metric<MemorySnapshot>?, history: List<Float>) {
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
            progress = snapshot.usedFraction.toFloat(),
            history = history,
        )
    } else {
        UnavailableCard(title = title, metric = metric)
    }
}

@Composable
fun CpuCard(metric: Metric<CpuSnapshot>?, history: List<Float>) {
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
            progress = (snapshot.usagePercent / 100.0).toFloat(),
            history = history,
        )
    } else {
        UnavailableCard(title = title, metric = metric)
    }
}

@Composable
fun StorageCard(metric: Metric<StorageSnapshot>?) {
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
            progress = snapshot.usedFraction.toFloat(),
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

private data class NavItem(val labelRes: Int, val iconRes: Int)

private val navItems = listOf(
    NavItem(R.string.nav_overview, R.drawable.ic_nav_overview),
    NavItem(R.string.nav_processes, R.drawable.ic_nav_processes),
    NavItem(R.string.nav_apps, R.drawable.ic_nav_apps),
    NavItem(R.string.nav_monitor, R.drawable.ic_nav_monitor),
    NavItem(R.string.nav_device, R.drawable.ic_nav_device),
)

@Composable
private fun ProcessMBottomBar(selected: Int, onSelect: (Int) -> Unit) {
    NavigationBar {
        navItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = index == selected,
                onClick = { onSelect(index) },
                icon = ImageVector.vectorResource(item.iconRes),
                label = stringResource(item.labelRes),
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
fun <T> Metric<T>?.availableValue(): T? =
    (this as? Metric.Available<*>)?.value as T?
