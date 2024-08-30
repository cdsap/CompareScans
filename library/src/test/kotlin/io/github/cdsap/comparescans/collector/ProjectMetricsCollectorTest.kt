package io.github.cdsap.comparescans.collector

import io.github.cdsap.comparescans.MockBuilds
import io.github.cdsap.comparescans.model.Entity
import io.github.cdsap.comparescans.model.TypeMetric
import org.junit.jupiter.api.Test

class ProjectMetricsCollectorTest {

    @Test
    fun projectMetricsAreCollected() {
        val (firstBuild, secondBuild) = MockBuilds().returnBuilds()
        val firstBuildTasks = firstBuild.build.taskExecution
        val secondBuildTasks = secondBuild.build.taskExecution

        val outcomesFirst = firstBuildTasks.groupBy { it.avoidanceOutcome }.flatMap { listOf(it.key) }.distinct()
        val outcomesSecond = secondBuildTasks.groupBy { it.avoidanceOutcome }.flatMap { listOf(it.key) }.distinct()
        val outcomes = outcomesFirst.union(outcomesSecond)

        val metric = ProjectMetricsCollector().projectMetrics(firstBuildTasks, outcomes, firstBuild.build.id)
        val firstBuildModules =
            firstBuild.build.taskExecution.groupBy { it.taskPath.split(":").dropLast(1).joinToString(":") }.count()
        val secondBuildModules =
            secondBuild.build.taskExecution.groupBy { it.taskPath.split(":").dropLast(1).joinToString(":") }.count()
        val firstMetricProjectModules = metric.filter {
            it.metric.entity == Entity.Project &&
                it.metric.type == TypeMetric.Counter && it.metric.name == "modules" &&
                it.variant == firstBuild.build.id
        }.first()
        val secondMetricProjectModules = metric.filter {
            it.metric.entity == Entity.Project &&
                it.metric.type == TypeMetric.Counter && it.metric.name == "modules" &&
                it.variant == secondBuild.build.id
        }.first()

        val firstMetricProjectTasks = metric.filter {
            it.metric.entity == Entity.Project &&
                it.metric.type == TypeMetric.Counter && it.metric.name == "tasks" &&
                it.variant == firstBuild.build.id
        }.first()
        val secondMetricProjectTasks = metric.filter {
            it.metric.entity == Entity.Project &&
                it.metric.type == TypeMetric.Counter && it.metric.name == "tasks" &&
                it.variant == secondBuild.build.id
        }.first()

        assert(metric.any { it.metric.entity == Entity.Project })
        assert(metric.any { it.metric.type == TypeMetric.Counter })
        assert(metric.any { it.metric.name == "modules" })
        assert(firstBuildModules == firstMetricProjectModules.value)
        assert(secondBuildModules == secondMetricProjectModules.value)
        assert(firstBuild.build.taskExecution.size == firstMetricProjectTasks.value)
        assert(secondBuild.build.taskExecution.size == secondMetricProjectTasks.value)
    }

    @Test
    fun projectMetricsCalculateCorrectlyThePercentiles() {
        val firstBuild = MockBuilds().returnSimpleBuilds()
        val firstBuildTasks = firstBuild.build.taskExecution

        val outcomesFirst = firstBuildTasks.groupBy { it.avoidanceOutcome }.flatMap { listOf(it.key) }.distinct()
        val metric =
            ProjectMetricsCollector().projectMetrics(firstBuildTasks, outcomesFirst.toSet(), firstBuild.build.id)
        val p90 = metric.first { it.metric.entity == Entity.Project && it.metric.type == TypeMetric.DurationP90 }
        assert(p90.value == 1540.0)
        val median = metric.first { it.metric.entity == Entity.Project && it.metric.type == TypeMetric.DurationMedian }
        assert(median.value == 26.0)
    }
}
