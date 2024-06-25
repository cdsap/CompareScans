package io.github.cdsap.comparescans.collector

import io.github.cdsap.comparescans.model.Entity
import io.github.cdsap.comparescans.model.Metric
import io.github.cdsap.comparescans.model.TypeMetric
import io.github.cdsap.geapi.client.model.Task
import org.nield.kotlinstatistics.median
import org.nield.kotlinstatistics.percentile

class TaskTypeCollector {

    fun taskTypes(
        firstBuildTasks: Array<Task>,
        secondBuildTasks: Array<Task>,
        outcomes: Set<String>
    ): List<Metric> {
        val metrics = mutableListOf<Metric>()
        val firstDurationByTaskType = firstBuildTasks.flatMap { listOf(it.taskType) }.distinct()
        val secondDurationByTaskType = secondBuildTasks.flatMap { listOf(it.taskType) }.distinct()

        val allOutcomes4 = firstDurationByTaskType.union(secondDurationByTaskType)

        allOutcomes4.forEach { taskType ->

            val firstBuildByTasks = firstBuildTasks.filter { it.taskType == taskType }
            val secondBuildByTasks = secondBuildTasks.filter { it.taskType == taskType }
            val cacheFirst =
                firstBuildByTasks.filter { it.cacheArtifactSize != null }.sumOf { it.fingerprintingDuration }
            val cacheSecond =
                secondBuildByTasks.filter { it.cacheArtifactSize != null }.sumOf { it.fingerprintingDuration }
            metrics.add(m(TypeMetric.CacheSize, "all outcomes", taskType, cacheFirst, cacheSecond))
            extracted(firstBuildByTasks, taskType, "all outcomes", secondBuildByTasks, metrics)

            outcomes.forEach { outcome ->
                val first = firstBuildTasks.filter { it.taskType == taskType && it.avoidanceOutcome == outcome }
                val second = secondBuildTasks.filter { it.taskType == taskType && it.avoidanceOutcome == outcome }
                extracted(first, taskType, outcome, second, metrics)
            }
        }
        return metrics
    }

    private fun extracted(
        first: List<Task>,
        taskType: String,
        outcome: String,
        second: List<Task>,
        metrics: MutableList<Metric>
    ) {
        val durationFirst = first.sumOf { it.duration }
        val durationSecond = second.sumOf { it.duration }

        val countFirst = first.count()
        val countSecond = second.count()

        val fingerFirst = first.sumOf { it.fingerprintingDuration }
        val fingerSecond = second.sumOf { it.fingerprintingDuration }

        metrics.add(m(TypeMetric.Duration, outcome, taskType, durationFirst, durationSecond))
        metrics.add(m(TypeMetric.Counter, outcome, taskType, countFirst, countSecond))
        metrics.add(m(TypeMetric.Fingerprinting, outcome, taskType, fingerFirst, fingerSecond))

        val firstDurationMean = if (durationFirst > 0) durationFirst / countFirst else 0
        val secondDurationMean = if (durationSecond > 0) durationSecond / countSecond else 0
        val firstFingerMean = if (fingerFirst > 0) fingerFirst / countFirst else 0
        val secondFingerMean = if (fingerSecond > 0) fingerSecond / countSecond else 0

        val firstDurationMedian = first.map { it.duration }.median().round()
        val secondDurationMedian = second.map { it.duration }.median().round()
        val firstDurationP90 = first.map { it.duration }.percentile(90.0).round()
        val secondDurationP90 = second.map { it.duration }.percentile(90.0).round()
        val firstFingerMedian = first.map { it.fingerprintingDuration }.median().round()
        val secondFingerMedian = second.map { it.fingerprintingDuration }.median().round()
        val firstFingerP90 = first.map { it.fingerprintingDuration }.percentile(90.0).round()
        val secondFingerP90 = second.map { it.fingerprintingDuration }.percentile(90.0).round()
        metrics.add(m(TypeMetric.DurationMean, outcome, taskType, firstDurationMean, secondDurationMean))
        metrics.add(m(TypeMetric.DurationMedian, outcome, taskType, firstDurationMedian, secondDurationMedian))
        metrics.add(m(TypeMetric.DurationP90, outcome, taskType, firstDurationP90, secondDurationP90))
        metrics.add(m(TypeMetric.FingerprintingMean, outcome, taskType, firstFingerMean, secondFingerMean))
        metrics.add(m(TypeMetric.FingerprintingMedian, outcome, taskType, firstFingerMedian, secondFingerMedian))
        metrics.add(m(TypeMetric.FingerprintingP90, outcome, taskType, firstFingerP90, secondFingerP90))
    }

    private fun m(
        typeMetric: TypeMetric,
        outcome: String,
        module: String,
        durationFirst: Number,
        durationSecond: Number
    ) = Metric(
        entity = Entity.TaskType,
        type = typeMetric,
        name = module,
        subcategory = outcome,
        firstBuild = durationFirst,
        secondBuild = durationSecond
    )
}
