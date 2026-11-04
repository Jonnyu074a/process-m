package com.jonnyuun.processm

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jonnyuun.processm.core.designsystem.theme.ProcessMTheme
import com.jonnyuun.processm.domain.AppsRepository
import com.jonnyuun.processm.domain.MetricsRepository
import com.jonnyuun.processm.domain.ProcessRepository
import com.jonnyuun.processm.domain.SettingsRepository
import com.jonnyuun.processm.feature.overview.OverviewRoute
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )

        val repository: MetricsRepository by inject()
        val settings: SettingsRepository by inject()
        val appsRepository: AppsRepository by inject()
        val processRepository: ProcessRepository by inject()

        setContent {
            ProcessMTheme {
                OverviewRoute(
                    repository = repository,
                    settings = settings,
                    appsRepository = appsRepository,
                    processRepository = processRepository,
                )
            }
        }
    }
}
