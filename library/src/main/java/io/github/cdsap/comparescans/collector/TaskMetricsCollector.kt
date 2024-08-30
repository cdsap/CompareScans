package io.github.cdsap.comparescans.collector

import io.github.cdsap.comparescans.model.Entity
import io.github.cdsap.comparescans.model.Measurement
import io.github.cdsap.comparescans.model.Metric
import io.github.cdsap.comparescans.model.TypeMetric
import io.github.cdsap.geapi.client.model.Task

class TaskMetricsCollector {

    fun singleMetricsTasks(
        tasksFirst: Array<Task>,
        variant: String
    ): List<Measurement> {
        val metrics = mutableListOf<Measurement>()
        tasksFirst.forEach { outcome ->

            metrics.add(
                Measurement(
                    variant,
                    Metric(
                        entity = Entity.Task,
                        type = TypeMetric.Duration,
                        subcategory = "",
                        name = outcome.taskPath
                    ),
                    outcome.duration
                )
            )
            if (outcome.cacheArtifactSize != null) {
                metrics.add(
                    Measurement(
                        variant,
                        Metric(
                            entity = Entity.Task,
                            type = TypeMetric.CacheSize,
                            subcategory = "",
                            name = outcome.taskPath
                        ),
                        if (outcome.cacheArtifactSize != null) outcome.cacheArtifactSize!! else 0L
                    )
                )
            }
        }

        return metrics
    }
}
