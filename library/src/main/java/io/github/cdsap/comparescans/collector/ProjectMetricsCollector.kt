package io.github.cdsap.comparescans.collector

import io.github.cdsap.comparescans.model.Entity
import io.github.cdsap.comparescans.model.Metric
import io.github.cdsap.comparescans.model.TypeMetric
import io.github.cdsap.geapi.client.model.Task
import org.nield.kotlinstatistics.median
import org.nield.kotlinstatistics.percentile

class ProjectMetricsCollector {
    fun projectMetrics(
        firstBuildTasks: Array<Task>,
        secondBuildTasks: Array<Task>,
        outcomes: Set<String>
    ): List<Metric> {
        val metrics = mutableListOf<Metric>()
        val first = firstBuildTasks.groupBy { it.taskPath.split(":").dropLast(1).joinToString(":") }
        val second = secondBuildTasks.groupBy { it.taskPath.split(":").dropLast(1).joinToString(":") }

        metrics.add(m(TypeMetric.Counter, "", "modules", first.count(), second.count()))
        metrics.add(m(TypeMetric.Counter, "", "tasks", firstBuildTasks.count(), secondBuildTasks.count()))

        outcomes.forEach { outcome ->

            val firstOutcomes = firstBuildTasks.filter { it.avoidanceOutcome == outcome }
            val secondOutcomes = secondBuildTasks.filter { it.avoidanceOutcome == outcome }
            val countFirst = firstOutcomes.count()
            val countSecond = secondOutcomes.count()
            val durationFirst = firstOutcomes.sumOf { it.duration }
            val durationSecond = secondOutcomes.sumOf { it.duration }
            val fingerFirst = firstOutcomes.sumOf { it.fingerprintingDuration }
            val fingerSecond = secondOutcomes.sumOf { it.fingerprintingDuration }

            val firstDurationMean = if (durationFirst > 0) durationFirst / countFirst else 0
            val secondDurationMean = if (durationSecond > 0) durationSecond / countSecond else 0
            val firstFingerMean = if (fingerFirst > 0) fingerFirst / countFirst else 0
            val secondFingerMean = if (fingerSecond > 0) fingerSecond / countSecond else 0

            val firstDurationMedian = firstOutcomes.map { it.duration }.median().round()
            val secondDurationMedian = secondOutcomes.map { it.duration }.median().round()
            val firstDurationP90 = firstOutcomes.map { it.duration }.percentile(90.0).round()
            val secondDurationP90 = secondOutcomes.map { it.duration }.percentile(90.0).round()
            val firstFingerMedian = firstOutcomes.map { it.fingerprintingDuration }.median().round()
            val secondFingerMedian = secondOutcomes.map { it.fingerprintingDuration }.median().round()
            val firstFingerP90 = firstOutcomes.map { it.fingerprintingDuration }.percentile(90.0).round()
            val secondFingerP90 = secondOutcomes.map { it.fingerprintingDuration }.percentile(90.0).round()

            metrics.add(m(TypeMetric.Counter, "", outcome, countFirst, countSecond))
            metrics.add(m(TypeMetric.Duration, "", outcome, durationFirst, durationSecond))
            metrics.add(m(TypeMetric.Fingerprinting, "", outcome, fingerFirst, fingerSecond))
            metrics.add(m(TypeMetric.DurationMean, "", outcome, firstDurationMean, secondDurationMean))
            metrics.add(m(TypeMetric.FingerprintingMean, "", outcome, firstFingerMean, secondFingerMean))
            metrics.add(m(TypeMetric.DurationMedian, "", outcome, firstDurationMedian, secondDurationMedian))
            metrics.add(m(TypeMetric.FingerprintingMedian, "", outcome, firstFingerMedian, secondFingerMedian))
            metrics.add(m(TypeMetric.DurationP90, "", outcome, firstDurationP90, secondDurationP90))
            metrics.add(m(TypeMetric.FingerprintingP90, "", outcome, firstFingerP90, secondFingerP90))
        }
        return metrics
    }

    private fun m(
        typeMetric: TypeMetric,
        subcategory: String,
        module: String,
        durationFirst: Number,
        durationSecond: Number
    ) = Metric(
        entity = Entity.Project,
        type = typeMetric,
        name = module,
        subcategory = subcategory,
        firstBuild = durationFirst,
        secondBuild = durationSecond
    )
}
