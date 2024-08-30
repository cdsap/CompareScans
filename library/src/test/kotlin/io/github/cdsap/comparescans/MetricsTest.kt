package io.github.cdsap.comparescans

import io.github.cdsap.comparescans.model.Entity
import kotlin.test.Test

class MetricsTest {

    @Test
    fun allCollectorsReturnMetrics() {
        val (firstBuild, secondBuild) = MockBuilds().returnBuilds()
        val metrics = MultipleScanMetrics(listOf(firstBuild, secondBuild)).get()
        assert(metrics.any { it.metric.entity == Entity.Module })
        assert(metrics.any { it.metric.entity == Entity.TaskType })
        assert(metrics.any { it.metric.entity == Entity.Task })
        assert(metrics.any { it.metric.entity == Entity.Project })
    }
}
