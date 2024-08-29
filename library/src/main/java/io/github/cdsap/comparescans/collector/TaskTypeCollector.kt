package io.github.cdsap.comparescans.collector

import io.github.cdsap.comparescans.model.Entity
import io.github.cdsap.comparescans.model.Measurement
import io.github.cdsap.comparescans.model.Metric
import io.github.cdsap.comparescans.model.TypeMetric
import io.github.cdsap.geapi.client.model.Task
import org.nield.kotlinstatistics.median
import org.nield.kotlinstatistics.percentile

class TaskTypeCollector {

    fun measurementTaskTypes(
        firstBuildTasks: Array<Task>,
        outcomes: Set<String>,
        variant: String
    ): List<Measurement> {
        val metrics = mutableListOf<Measurement>()
        val firstDurationByTaskType = firstBuildTasks.flatMap { listOf(it.taskType) }.distinct()

        firstDurationByTaskType.forEach { taskType ->

            val firstBuildByTasks = firstBuildTasks.filter { it.taskType == taskType }
            val cacheFirst =
                firstBuildByTasks.filter { it.cacheArtifactSize != null }.sumOf { it.fingerprintingDuration }
            metrics.add(Measurement(variant, m(TypeMetric.CacheSize, "all outcomes", taskType), cacheFirst))

            extracted(firstBuildByTasks, taskType, "all outcomes", metrics, variant)

            outcomes.forEach { outcome ->
                val first = firstBuildTasks.filter { it.taskType == taskType && it.avoidanceOutcome == outcome }
                extracted(first, taskType, outcome, metrics, variant)
            }
        }
        return metrics
    }

    private fun extracted(
        first: List<Task>,
        taskType: String,
        outcome: String,
        metrics: MutableList<Measurement>,
        variant: String
    ) {
        val durationFirst = first.sumOf { it.duration }

        val countFirst = first.count()

        val fingerFirst = first.sumOf { it.fingerprintingDuration }

        metrics.add(Measurement(variant, m(TypeMetric.Duration, outcome, taskType), durationFirst))
        metrics.add(Measurement(variant, m(TypeMetric.Counter, outcome, taskType), countFirst))
        metrics.add(Measurement(variant, m(TypeMetric.Fingerprinting, outcome, taskType), fingerFirst))

        val firstDurationMean = if (durationFirst > 0) durationFirst / countFirst else 0
        val firstFingerMean = if (fingerFirst > 0) fingerFirst / countFirst else 0

        val firstDurationMedian = first.map { it.duration }.median().round()
        val firstDurationP90 = first.map { it.duration }.percentile(90.0).round()
        val firstFingerMedian = first.map { it.fingerprintingDuration }.median().round()
        val firstFingerP90 = first.map { it.fingerprintingDuration }.percentile(90.0).round()
        metrics.add(Measurement(variant, m(TypeMetric.DurationMean, outcome, taskType), firstDurationMean))
        metrics.add(Measurement(variant, m(TypeMetric.DurationMedian, outcome, taskType), firstDurationMedian))
        metrics.add(Measurement(variant, m(TypeMetric.DurationP90, outcome, taskType), firstDurationP90))
        metrics.add(Measurement(variant, m(TypeMetric.FingerprintingMean, outcome, taskType), firstFingerMean))
        metrics.add(Measurement(variant, m(TypeMetric.FingerprintingMedian, outcome, taskType), firstFingerMedian))
        metrics.add(Measurement(variant, m(TypeMetric.FingerprintingP90, outcome, taskType), firstFingerP90))
    }

    private fun m(
        typeMetric: TypeMetric,
        outcome: String,
        module: String
    ) = Metric(
        entity = Entity.TaskType,
        type = typeMetric,
        name = module,
        subcategory = outcome
    )
}
