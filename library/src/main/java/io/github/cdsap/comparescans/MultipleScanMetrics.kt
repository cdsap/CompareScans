package io.github.cdsap.comparescans

import io.github.cdsap.comparescans.collector.ModuleMetricCollector
import io.github.cdsap.comparescans.collector.ProjectMetricsCollector
import io.github.cdsap.comparescans.collector.ResourceUsageCollector
import io.github.cdsap.comparescans.collector.TaskMetricsCollector
import io.github.cdsap.comparescans.collector.TaskTypeCollector
import io.github.cdsap.comparescans.model.BuildWithResourceUsage
import io.github.cdsap.comparescans.model.Measurement
import io.github.cdsap.comparescans.model.MultipleBuildScanMetric

class MultipleScanMetrics(private val builds: List<BuildWithResourceUsage>) {

    fun get(): List<MultipleBuildScanMetric> {
        val buildTasks = builds.map { it.build.taskExecution }
        val outcomes = buildTasks.map { tasks ->
            tasks.groupBy { it.avoidanceOutcome }.flatMap { listOf(it.key) }.distinct().toSet()
        }

        val projectMetricsList = builds.indices.map { i ->
            ProjectMetricsCollector().projectMetrics(builds[i].build.taskExecution, outcomes[i], builds[i].build.id)
        }

        val projectMetrics = compareMetrics(projectMetricsList)

        val taskTypeMetricsList = builds.indices.map { i ->
            TaskTypeCollector().measurementTaskTypes(builds[i].build.taskExecution, outcomes[i], builds[i].build.id)
        }
        val taskTypeMetrics = compareMetrics(taskTypeMetricsList)

        val moduleMetricsList = builds.indices.map { i ->
            ModuleMetricCollector().singleModuleMetrics(builds[i].build.taskExecution, outcomes[i], builds[i].build.id)
        }
        val moduleMetrics = compareMetrics(moduleMetricsList)

        val taskMetricsList = builds.indices.map { i ->
            TaskMetricsCollector().singleMetricsTasks(builds[i].build.taskExecution, builds[i].build.id)
        }
        val taskMetrics = compareMetrics(taskMetricsList)

        val resourceUsageMetricList = builds.indices.map { i ->
            if (builds[i].usage?.total != null) {
                ResourceUsageCollector().measurementsResourceUsage(builds[i].usage!!, builds[i].build.id)
            } else {
                emptyList()
            }
        }

        val resourceUsageMetrics = compareMetrics(resourceUsageMetricList)

        return moduleMetrics + taskTypeMetrics + projectMetrics + taskMetrics + resourceUsageMetrics
    }

    private fun compareMetrics(metricsList: List<List<Measurement>>): List<MultipleBuildScanMetric> {
        val allMetrics = metricsList.flatten().map { it.metric }.toSet()
        return allMetrics.map { metric ->
            val values = builds.map { it.build }.associate { build ->
                val measurement = metricsList[builds.map { it.build }.indexOf(build)].find {
                    it.metric.type == metric.type &&
                        it.metric.name == metric.name && it.metric.entity == metric.entity &&
                        it.metric.subcategory == metric.subcategory
                }
                build.id to (measurement?.value ?: -1L)
            }
            MultipleBuildScanMetric(
                metric = metric,
                values = values
            )
        }
    }
}
