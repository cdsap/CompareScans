package io.github.cdsap.comparescans.collector

import io.github.cdsap.comparescans.MockBuilds
import io.github.cdsap.comparescans.model.Entity
import io.github.cdsap.comparescans.model.TypeMetric
import kotlin.test.Test

class TaskMetricsCollectorTest {

    @Test
    fun taskMetricProcessesDuration() {
        val (firstBuild, secondBuild) = MockBuilds().returnBuilds()
        val firstBuildTasks = firstBuild.taskExecution
        val secondBuildTasks = secondBuild.taskExecution

        val firstMetric =
            TaskMetricsCollector().singleMetricsTasks(firstBuildTasks, firstBuild.id)
                .first { it.metric.name == ":app:preBuild" && it.metric.type == TypeMetric.Duration }
        val secondMetric =
            TaskMetricsCollector().singleMetricsTasks(secondBuildTasks, secondBuild.id)
                .first { it.metric.name == ":app:preBuild" && it.metric.type == TypeMetric.Duration }

        val firstDuration = firstBuild.taskExecution.first { it.taskPath == ":app:preBuild" }.duration
        val secondDuration = secondBuild.taskExecution.first { it.taskPath == ":app:preBuild" }.duration
        assert(firstMetric.metric.entity == Entity.Task)
        assert(firstMetric.metric.type == TypeMetric.Duration)
        assert(firstDuration == firstMetric.value)
        assert(secondDuration == secondMetric.value)
    }
}
