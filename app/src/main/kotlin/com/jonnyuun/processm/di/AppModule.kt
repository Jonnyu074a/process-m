package com.jonnyuun.processm.di

import android.content.Context
import com.jonnyuun.processm.core.system.AndroidBatterySource
import com.jonnyuun.processm.core.system.AndroidMemorySource
import com.jonnyuun.processm.core.system.AndroidStorageSource
import com.jonnyuun.processm.core.system.BatterySource
import com.jonnyuun.processm.core.system.CpuSource
import com.jonnyuun.processm.core.system.MemorySource
import com.jonnyuun.processm.core.system.ProcCpuSource
import com.jonnyuun.processm.core.system.StorageSource
import com.jonnyuun.processm.data.AppsRepositoryImpl
import com.jonnyuun.processm.data.DataStoreSettingsRepository
import com.jonnyuun.processm.data.SystemMetricsRepository
import com.jonnyuun.processm.domain.AppsRepository
import com.jonnyuun.processm.domain.MetricsRepository
import com.jonnyuun.processm.domain.SettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single<MemorySource> { AndroidMemorySource(androidContext()) }
    single<CpuSource> { ProcCpuSource() }
    single<StorageSource> { AndroidStorageSource() }
    single<BatterySource> { AndroidBatterySource(androidContext()) }
    single<SettingsRepository> { DataStoreSettingsRepository(androidContext()) }
    single<AppsRepository> { AppsRepositoryImpl(androidContext()) }
    single<MetricsRepository> {
        SystemMetricsRepository(
            memorySource = get(),
            cpuSource = get(),
            storageSource = get(),
            batterySource = get(),
        )
    }
}