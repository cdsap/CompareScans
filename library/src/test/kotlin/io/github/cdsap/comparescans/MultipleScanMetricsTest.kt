package io.github.cdsap.comparescans

import io.github.cdsap.comparescans.model.TypeMetric
import org.junit.jupiter.api.Test

class MultipleScanMetricsTest {

    @Test
    fun metricsWithoutResourceUsageNotIncludeResourceUsageMetrics() {
        val (firstBuild, secondBuild) = MockBuilds().returnBuilds()
        val metrics = MultipleScanMetrics(listOf(firstBuild, secondBuild)).get()
        assert(metrics.none { it.metric.type == TypeMetric.ResourceAverage })
    }

    @Test
    fun metricsWithResourceUsageIncludeResourceUsageMetrics() {
        val (firstBuild, secondBuild) = MockBuilds().returnBuildsWithUsage()
        val metrics = MultipleScanMetrics(listOf(firstBuild, secondBuild)).get()
        assert(metrics.any { it.metric.type == TypeMetric.ResourceAverage })
    }
}
