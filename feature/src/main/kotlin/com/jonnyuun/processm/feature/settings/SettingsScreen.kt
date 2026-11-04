package com.jonnyuun.processm.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonnyuun.processm.domain.SettingsRepository
import com.jonnyuun.processm.feature.R
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.ui.res.stringResource

@Composable
fun SettingsScreen(onBack: () -> Unit, settings: SettingsRepository) {
    val scope = rememberCoroutineScope()
    val interval by settings.samplingIntervalMs.collectAsState(initial = 2_000L)

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = stringResource(R.string.settings_title),
                navigationIcon = {
                    TextButton(
                        text = stringResource(R.string.settings_back),
                        onClick = onBack,
                    )
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SettingsSection(R.string.settings_appearance, R.string.settings_appearance_body)
            SettingsSection(R.string.settings_monitoring, R.string.settings_monitoring_body)
            SamplingSection(interval = interval, onSelect = { scope.launch { settings.setSamplingIntervalMs(it) } })
            SettingsSection(R.string.settings_access, R.string.settings_access_body)
            SettingsSection(R.string.settings_privacy, R.string.settings_privacy_body)
            SettingsSection(R.string.settings_about, R.string.settings_about_body)
        }
    }
}

@Composable
private fun SamplingSection(interval: Long, onSelect: (Long) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_sampling),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MiuixTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.settings_sampling_body),
            fontSize = 13.sp,
            color = MiuixTheme.colorScheme.onSurfaceSecondary,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (value in listOf(1_000L, 2_000L, 5_000L)) {
                TextButton(
                    text = "${value / 1000}s",
                    onClick = { onSelect(value) },
                )
            }
        }
        Text(
            text = stringResource(R.string.settings_sampling_current, interval / 1000),
            fontSize = 12.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
private fun SettingsSection(titleRes: Int, bodyRes: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            text = stringResource(titleRes),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MiuixTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(bodyRes),
            fontSize = 13.sp,
            color = MiuixTheme.colorScheme.onSurfaceSecondary,
        )
    }
}
EOF
echo done