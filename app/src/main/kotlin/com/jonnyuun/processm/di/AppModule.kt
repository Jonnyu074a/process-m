package com.jonnyuun.processm.di

import android.content.Context
import com.jonnyuun.processm.core.system.AndroidBatterySource
import com.jonnyuun.processm.core.system.AndroidMemorySource
import com.jonnyuun.processm.core.system.AndroidStorageSource
import com.jonnyuun.processm.core.system.ProcCpuSource
import com.jonnyuun.processm.data.SystemMetricsRepository
import com.jonnyuun.processm.domain.MetricsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { AndroidMemorySource(androidContext()) }
    single { ProcCpuSource() }
    single { AndroidStorageSource() }
    single { AndroidBatterySource(androidContext()) }
    single<MetricsRepository> {
        SystemMetricsRepository(
            memorySource = get(),
            cpuSource = get(),
            storageSource = get(),
            batterySource = get(),
        )
    }
}