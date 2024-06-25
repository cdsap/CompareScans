package io.github.cdsap.comparescans.cli

import io.github.cdsap.comparescans.model.Metric

interface GetMetrics {
    suspend fun getMetrics(): List<Metric>
}
