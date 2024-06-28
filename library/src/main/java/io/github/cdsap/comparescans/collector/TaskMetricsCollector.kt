package io.github.cdsap.comparescans.collector

import io.github.cdsap.comparescans.model.Entity
import io.github.cdsap.comparescans.model.Metric
import io.github.cdsap.comparescans.model.TypeMetric
import io.github.cdsap.geapi.client.model.Task

class TaskMetricsCollector {

    fun metricsTasks(
        tasksFirst: Array<Task>,
        tasksSecond: Array<Task>
    ): List<Metric> {
        val metrics = mutableListOf<Metric>()
        tasksFirst.forEach { outcome ->
            val taskSecond = tasksSecond.filter { it.taskPath == outcome.taskPath }.firstOrNull()
            if (taskSecond != null) {
                metrics.add(
                    Metric(
                        entity = Entity.Task,
                        type = TypeMetric.Duration,
                        subcategory = "",
                        name = outcome.taskPath,
                        firstBuild = outcome.duration,
                        secondBuild = taskSecond.duration
                    )
                )
                if (outcome.cacheArtifactSize != null || taskSecond.cacheArtifactSize != null) {
                    metrics.add(
                        Metric(
                            entity = Entity.Task,
                            type = TypeMetric.CacheSize,
                            subcategory = "",
                            name = outcome.taskPath,
                            firstBuild = if (outcome.cacheArtifactSize != null) outcome.cacheArtifactSize!! else 0L,
                            secondBuild = if (taskSecond.cacheArtifactSize != null) taskSecond.cacheArtifactSize!! else 0L
                        )
                    )
                }
            }
        }
        return metrics
    }
}
