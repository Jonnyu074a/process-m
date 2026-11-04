package com.jonnyuun.processm.core.system

import android.os.Environment
import android.os.StatFs
import com.jonnyuun.processm.core.model.Metric
import com.jonnyuun.processm.core.model.MetricKind
import com.jonnyuun.processm.core.model.StorageSnapshot
import com.jonnyuun.processm.core.model.UnavailableReason
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidStorageSource : StorageSource {

    override suspend fun read(): Metric<StorageSnapshot> = withContext(Dispatchers.IO) {
        try {
            val stat = StatFs(Environment.getDataDirectory().absolutePath)
            val total = stat.blockCountLong * stat.blockSizeLong
            val available = stat.availableBlocksLong * stat.blockSizeLong

            Metric.Available(
                kind = MetricKind.MEASURED,
                source = SystemSources.Storage,
                timestamp = Instant.now(),
                value = StorageSnapshot(
                    totalBytes = total,
                    availableBytes = available,
                    usedBytes = (total - available).coerceAtLeast(0L),
                ),
            )
        } catch (e: Exception) {
            Metric.Unavailable(
                kind = MetricKind.MEASURED,
                source = SystemSources.Storage,
                timestamp = Instant.now(),
                reason = UnavailableReason.READ_ERROR,
                detail = e.message,
            )
        }
    }
}
