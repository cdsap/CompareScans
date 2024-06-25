package io.github.cdsap.comparescans

import io.github.cdsap.comparescans.model.Entity
import kotlin.test.Test

class MetricsTest {

    @Test
    fun allCollectorsReturnMetrics() {
        val (firstBuild, secondBuild) = RegularBuilds().returnBuilds()
        val metrics = Metrics(firstBuild, secondBuild).get()
        assert(metrics.any { it.entity == Entity.Module })
        assert(metrics.any { it.entity == Entity.TaskType })
        assert(metrics.any { it.entity == Entity.Task })
        assert(metrics.any { it.entity == Entity.Project })
    }
}
