package io.github.cdsap.comparescans.collector

import io.github.cdsap.comparescans.RegularBuilds
import io.github.cdsap.comparescans.model.Entity
import io.github.cdsap.comparescans.model.TypeMetric
import org.junit.jupiter.api.Test

class ProjectMetricsCollectorTest {

    @Test
    fun projectMetricsAreCollected() {
        val (firstBuild, secondBuild) = RegularBuilds().returnBuilds()
        val firstBuildTasks = firstBuild.taskExecution
        val secondBuildTasks = secondBuild.taskExecution

        val outcomesFirst = firstBuildTasks.groupBy { it.avoidanceOutcome }.flatMap { listOf(it.key) }.distinct()
        val outcomesSecond = secondBuildTasks.groupBy { it.avoidanceOutcome }.flatMap { listOf(it.key) }.distinct()
        val outcomes = outcomesFirst.union(outcomesSecond)

        val metric = ProjectMetricsCollector().compareProjectMetrics(firstBuildTasks, secondBuildTasks, outcomes)
        val firstBuildModules =
            firstBuild.taskExecution.groupBy { it.taskPath.split(":").dropLast(1).joinToString(":") }.count()
        val secondBuildModules =
            secondBuild.taskExecution.groupBy { it.taskPath.split(":").dropLast(1).joinToString(":") }.count()
        val metricProjectModules = metric.filter {
            it.entity == Entity.Project &&
                it.type == TypeMetric.Counter && it.name == "modules"
        }.first()
        val metricProjectTasks = metric.filter {
            it.entity == Entity.Project &&
                it.type == TypeMetric.Counter && it.name == "tasks"
        }.first()

        assert(metric.any { it.entity == Entity.Project })
        assert(metricProjectModules.firstBuild == firstBuildModules)
        assert(metricProjectModules.secondBuild == secondBuildModules)
        assert(metricProjectTasks.firstBuild == firstBuild.taskExecution.size)
        assert(metricProjectTasks.secondBuild == secondBuild.taskExecution.size)
    }

    @Test
    fun projectMetricsCalculateCorrectlyThePercentiles() {
        val (firstBuild, secondBuild) = RegularBuilds().returnSimpleBuilds()
        val firstBuildTasks = firstBuild.taskExecution
        val secondBuildTasks = secondBuild.taskExecution

        val outcomesFirst = firstBuildTasks.groupBy { it.avoidanceOutcome }.flatMap { listOf(it.key) }.distinct()
        val outcomesSecond = secondBuildTasks.groupBy { it.avoidanceOutcome }.flatMap { listOf(it.key) }.distinct()
        val outcomes = outcomesFirst.union(outcomesSecond)

        val metric = ProjectMetricsCollector().compareProjectMetrics(firstBuildTasks, secondBuildTasks, outcomes)
        val p90 = metric.first { it.entity == Entity.Project && it.type == TypeMetric.DurationP90 }
        assert(p90.firstBuild == 99.0)
        val median = metric.first { it.entity == Entity.Project && it.type == TypeMetric.DurationMedian }
        assert(median.firstBuild == 55.0)
    }
}
