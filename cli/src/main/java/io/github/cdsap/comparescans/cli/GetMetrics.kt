package io.github.cdsap.comparescans.cli

import io.github.cdsap.comparescans.model.MultipleBuildScanMetric

interface GetMetrics {
    suspend fun getMetrics(): List<MultipleBuildScanMetric>
}
