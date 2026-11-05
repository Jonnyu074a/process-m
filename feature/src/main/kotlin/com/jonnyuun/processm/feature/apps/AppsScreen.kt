package com.jonnyuun.processm.feature.apps

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Process
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonnyuun.processm.core.common.formatBytes
import com.jonnyuun.processm.core.model.AppSummary
import com.jonnyuun.processm.core.model.ProcessSnapshot
import com.jonnyuun.processm.domain.AppsRepository
import com.jonnyuun.processm.domain.ProcessRepository
import com.jonnyuun.processm.feature.R
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource

@Composable
fun AppsRoute(appsRepository: AppsRepository, processRepository: ProcessRepository) {
    val viewModel: AppsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = AppsViewModel.factory(appsRepository, processRepository),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    AppsScreen(
        state = state,
        onQueryChanged = { viewModel.onQueryChanged(it) },
        onRunningQueryChanged = { viewModel.onRunningQueryChanged(it) },
    )
}

@Composable
fun AppsScreen(
    state: AppsUiState,
    onQueryChanged: (String) -> Unit,
    onRunningQueryChanged: (String) -> Unit,
) {
    val context = LocalContext.current
    val pm = context.packageManager

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = stringResource(R.string.apps_title),
                subtitle = stringResource(R.string.apps_subtitle),
            )
        },
        bottomBar = {
            top.yukonga.miuix.kmp.basic.NavigationBar {
                top.yukonga.miuix.kmp.basic.NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = ImageVector.vectorResource(R.drawable.ic_nav_apps),
                    label = stringResource(R.string.nav_installed),
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.apps_section),
                fontSize = 13.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )

            val hasUsageStats = hasUsageStatsPermission(context)

            if (!hasUsageStats) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    insideMargin = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.apps_usage_needed),
                            fontSize = 14.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            color = MiuixTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.apps_usage_body),
                            fontSize = 13.sp,
                            color = MiuixTheme.colorScheme.onSurfaceSecondary,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            text = stringResource(R.string.apps_usage_open_settings),
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            },
                        )
                    }
                }
            }

            InstalledAppsSection(
                state = state,
                onQueryChanged = onQueryChanged,
                pm = pm,
            )

            RunningAppsSection(
                state = state,
                onQueryChanged = onRunningQueryChanged,
                pm = pm,
            )
        }
    }
}

@Composable
private fun InstalledAppsSection(
    state: AppsUiState,
    onQueryChanged: (String) -> Unit,
    pm: PackageManager,
) {
    Text(
        text = stringResource(R.string.apps_installed),
        fontSize = 15.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        color = MiuixTheme.colorScheme.onSurface,
    )

    val queryState = remember { mutableStateOf(state.query) }
    BasicTextField(
        value = queryState.value,
        onValueChange = {
            queryState.value = it
            onQueryChanged(it)
        },
        modifier = Modifier.fillMaxWidth(),
    )

    val apps = state.apps
    if (apps.isEmpty() && state.query.isNotBlank()) {
        Text(
            text = stringResource(R.string.apps_no_results),
            fontSize = 14.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            textAlign = TextAlign.Center,
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val pairs = apps.chunked(2)
        pairs.forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                pair.forEach { app ->
                    Column(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    ) {
                        InstalledAppCard(app = app, pm = pm)
                    }
                }
            }
        }
    }
}

@Composable
private fun InstalledAppCard(app: AppSummary, pm: PackageManager) {
    val painter = remember(app.packageName) {
        val drawable = pm.getApplicationIcon(app.packageName)
        val w = drawable.intrinsicWidth.coerceAtLeast(48)
        val h = drawable.intrinsicHeight.coerceAtLeast(48)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, w, h)
        drawable.draw(canvas)
        ImageBitmap.imageBitmap(bitmap)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.size(48.dp).background(MiuixTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = BitmapPainter(painter),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                )
            }
            Text(
                text = app.label,
                fontSize = 12.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                color = MiuixTheme.colorScheme.onSurface,
                maxLines = 2,
                textAlign = TextAlign.Center,
            )
            Text(
                text = app.packageName,
                fontSize = 10.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun RunningAppsSection(
    state: AppsUiState,
    onQueryChanged: (String) -> Unit,
    pm: PackageManager,
) {
    Text(
        text = stringResource(R.string.apps_running),
        fontSize = 15.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        color = MiuixTheme.colorScheme.onSurface,
    )

    val queryState = remember { mutableStateOf(state.runningQuery) }
    BasicTextField(
        value = queryState.value,
        onValueChange = {
            queryState.value = it
            onQueryChanged(it)
        },
        modifier = Modifier.fillMaxWidth(),
    )

    val running = state.running
    if (running.isEmpty() && state.runningQuery.isNotBlank()) {
        Text(
            text = stringResource(R.string.apps_no_results),
            fontSize = 14.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            textAlign = TextAlign.Center,
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val pairs = running.chunked(2)
        pairs.forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                pair.forEach { proc ->
                    Column(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    ) {
                        RunningAppCard(proc = proc, pm = pm)
                    }
                }
            }
        }
    }
}

@Composable
private fun RunningAppCard(proc: ProcessSnapshot, pm: PackageManager) {
    val painter = remember(proc.packageName) {
        val drawable: android.graphics.drawable.Drawable? = try {
            pm.getApplicationIcon(proc.packageName ?: proc.processName)
        } catch (_: Exception) {
            null
        }
        if (drawable == null) {
            val w = 48
            val h = 48
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            ImageBitmap.imageBitmap(bitmap)
        } else {
            val w = drawable.intrinsicWidth.coerceAtLeast(48)
            val h = drawable.intrinsicHeight.coerceAtLeast(48)
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, w, h)
            drawable.draw(canvas)
            ImageBitmap.imageBitmap(bitmap)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.size(48.dp).background(MiuixTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = BitmapPainter(painter),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                )
            }
            Text(
                text = proc.packageName ?: proc.processName,
                fontSize = 12.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                color = MiuixTheme.colorScheme.onSurface,
                maxLines = 2,
                textAlign = TextAlign.Center,
            )
            Text(
                text = proc.processName,
                fontSize = 10.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = formatBytes(proc.ramBytes),
                    fontSize = 12.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    color = MiuixTheme.colorScheme.primary,
                )
                Text(
                    text = "PID ${proc.pid}",
                    fontSize = 10.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
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
