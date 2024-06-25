package io.github.cdsap.comparescans.collector

import io.github.cdsap.comparescans.RegularBuilds
import io.github.cdsap.comparescans.model.Entity
import io.github.cdsap.comparescans.model.TypeMetric
import kotlin.test.Test

class TaskMetricsCollectorTest {

    @Test
    fun taskMetricProcessesDuration() {
        val (firstBuild, secondBuild) = RegularBuilds().returnBuilds()
        val firstBuildTasks = firstBuild.taskExecution
        val secondBuildTasks = secondBuild.taskExecution

        val metric =
            TaskMetricsCollector().metricsTasks(firstBuildTasks, secondBuildTasks).first { it.name == ":app:preBuild" }
        val firstDuration = firstBuild.taskExecution.first { it.taskPath == ":app:preBuild" }.duration
        val secondDuration = secondBuild.taskExecution.first { it.taskPath == ":app:preBuild" }.duration
        assert(metric.entity == Entity.Task)
        assert(metric.type == TypeMetric.Duration)
        assert(firstDuration == metric.firstBuild && secondDuration == metric.secondBuild)
    }
}
