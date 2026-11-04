package com.jonnyuun.processm.core.system

import android.content.Context
import android.os.BatteryManager
import com.jonnyuun.processm.core.model.BatterySnapshot
import com.jonnyuun.processm.core.model.Metric
import com.jonnyuun.processm.core.model.MetricKind
import com.jonnyuun.processm.core.model.UnavailableReason
import java.time.Instant

class AndroidBatterySource(
    private val context: Context,
) : BatterySource {
    override suspend fun read(): Metric<BatterySnapshot> {
        val manager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val level = manager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
        val status = manager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) ?: -1
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        return if (level in 0..100) {
            Metric.Available(
                kind = MetricKind.MEASURED,
                source = com.jonnyuun.processm.core.system.SystemSources.Battery,
                timestamp = Instant.now(),
                value = BatterySnapshot(levelPercent = level, isCharging = charging),
            )
        } else {
            Metric.Unavailable(
                kind = MetricKind.MEASURED,
                source = com.jonnyuun.processm.core.system.SystemSources.Battery,
                timestamp = Instant.now(),
                reason = UnavailableReason.NOT_READABLE,
                detail = "BatteryManager no expone el nivel",
            )
        }
    }
}