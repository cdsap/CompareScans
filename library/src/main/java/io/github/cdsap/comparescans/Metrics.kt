package io.github.cdsap.comparescans

import io.github.cdsap.comparescans.collector.ModuleMetricCollector
import io.github.cdsap.comparescans.collector.ProjectMetricsCollector
import io.github.cdsap.comparescans.collector.TaskMetricsCollector
import io.github.cdsap.comparescans.collector.TaskTypeCollector
import io.github.cdsap.comparescans.model.Metric
import io.github.cdsap.geapi.client.model.Build

class Metrics(private val firstBuild: Build, private val secondBuild: Build) {

    fun get(): List<Metric> {
        val firstBuildTasks = firstBuild.taskExecution
        val secondBuildTasks = secondBuild.taskExecution

        val outcomesFirst = firstBuildTasks.groupBy { it.avoidanceOutcome }.flatMap { listOf(it.key) }.distinct()
        val outcomesSecond = secondBuildTasks.groupBy { it.avoidanceOutcome }.flatMap { listOf(it.key) }.distinct()
        val outcomes = outcomesFirst.union(outcomesSecond)

        val metricModules = ModuleMetricCollector().process(firstBuildTasks, secondBuildTasks, outcomes)
        val taskTypeMetrics = TaskTypeCollector().taskTypes(firstBuildTasks, secondBuildTasks, outcomes)
        val projectMetrics = ProjectMetricsCollector().projectMetrics(firstBuildTasks, secondBuildTasks, outcomes)
        val taskMetrics = TaskMetricsCollector().metricsTasks(firstBuildTasks, secondBuildTasks)

        return metricModules + taskTypeMetrics + projectMetrics + taskMetrics
    }
}
