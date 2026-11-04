package com.jonnyuun.processm.feature.device

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import top.yukonga.miuix.kmp.basic.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonnyuun.processm.core.designsystem.component.MetricCard
import com.jonnyuun.processm.core.model.Metric
import com.jonnyuun.processm.domain.MetricsRepository
import com.jonnyuun.processm.feature.overview.CpuCard
import com.jonnyuun.processm.feature.overview.DeviceOverviewCard
import com.jonnyuun.processm.feature.overview.MemoryCard
import com.jonnyuun.processm.feature.overview.OverviewUiState
import com.jonnyuun.processm.feature.overview.OverviewViewModel
import com.jonnyuun.processm.feature.overview.StorageCard
import com.jonnyuun.processm.feature.R
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.ui.res.stringResource

@Composable
fun DeviceRoute(repository: MetricsRepository) {
    val viewModel: OverviewViewModel = viewModel(
        factory = OverviewViewModel.factory(repository),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    DeviceScreen(state = state)
}

@Composable
fun DeviceScreen(state: OverviewUiState, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val hasUsageStats = hasUsageStatsPermission(context)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DeviceOverviewCard(state)

        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Text(
                text = stringResource(R.string.device_access_title),
                fontSize = 13.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.device_access_level),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MiuixTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.device_access_note),
                fontSize = 13.sp,
                color = MiuixTheme.colorScheme.onSurfaceSecondary,
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Text(
                text = stringResource(R.string.device_permissions_title),
                fontSize = 13.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.device_permission_usage),
                    modifier = Modifier.weight(1f),
                    fontSize = 14.sp,
                    color = MiuixTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (hasUsageStats) {
                        stringResource(R.string.device_permission_granted)
                    } else {
                        stringResource(R.string.device_permission_denied)
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (hasUsageStats) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            if (!hasUsageStats) {
                Spacer(Modifier.height(8.dp))
                TextButton(
                    text = stringResource(R.string.device_permission_open_settings),
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    },
                )
            }
        }

        MemoryCard(state.memory, state.memoryHistory)
        CpuCard(state.cpu, state.cpuHistory)
        StorageCard(state.storage)
    }
}

private fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName,
    )
    return mode == AppOpsManager.MODE_ALLOWED
}
EOF
echo done