package io.github.cdsap.comparescans.collector

import io.github.cdsap.comparescans.model.Entity
import io.github.cdsap.comparescans.model.Measurement
import io.github.cdsap.comparescans.model.Metric
import io.github.cdsap.comparescans.model.TypeMetric
import io.github.cdsap.geapi.client.model.Task
import org.nield.kotlinstatistics.median
import org.nield.kotlinstatistics.percentile

class ProjectMetricsCollector {

    fun projectMetrics(
        tasks: Array<Task>,
        outcomes: Set<String>,
        variant: String
    ): List<Measurement> {
        val metrics = mutableListOf<Measurement>()
        val first = tasks.groupBy { it.taskPath.split(":").dropLast(1).joinToString(":") }
        metrics.add(Measurement(variant, metric(TypeMetric.Counter, "", "modules"), first.count()))
        metrics.add(Measurement(variant, metric(TypeMetric.Counter, "", "tasks"), tasks.count()))
        outcomes.forEach { outcome ->

            val firstOutcomes = tasks.filter { it.avoidanceOutcome == outcome }
            val countFirst = firstOutcomes.count()
            val durationFirst = firstOutcomes.sumOf { it.duration }
            val fingerFirst = firstOutcomes.sumOf { it.fingerprintingDuration }

            val firstDurationMean = if (durationFirst > 0) durationFirst / countFirst else 0
            val firstFingerMean = if (fingerFirst > 0) fingerFirst / countFirst else 0

            val firstDurationMedian = firstOutcomes.map { it.duration }.median().round()
            val firstDurationP90 = firstOutcomes.map { it.duration }.percentile(90.0).round()
            val firstFingerMedian = firstOutcomes.map { it.fingerprintingDuration }.median().round()
            val firstFingerP90 = firstOutcomes.map { it.fingerprintingDuration }.percentile(90.0).round()

            metrics.add(Measurement(variant, metric(TypeMetric.Counter, "", outcome), countFirst))
            metrics.add(Measurement(variant, metric(TypeMetric.Duration, "", outcome), durationFirst))
            metrics.add(Measurement(variant, metric(TypeMetric.Fingerprinting, "", outcome), fingerFirst))
            metrics.add(Measurement(variant, metric(TypeMetric.DurationMean, "", outcome), firstDurationMean))
            metrics.add(Measurement(variant, metric(TypeMetric.FingerprintingMean, "", outcome), firstFingerMean))
            metrics.add(Measurement(variant, metric(TypeMetric.DurationMedian, "", outcome), firstDurationMedian))
            metrics.add(Measurement(variant, metric(TypeMetric.FingerprintingMedian, "", outcome), firstFingerMedian))
            metrics.add(Measurement(variant, metric(TypeMetric.DurationP90, "", outcome), firstDurationP90))
            metrics.add(Measurement(variant, metric(TypeMetric.FingerprintingP90, "", outcome), firstFingerP90))
        }
        return metrics
    }

    private fun metric(
        typeMetric: TypeMetric,
        subcategory: String,
        module: String
    ) = Metric(
        entity = Entity.Project,
        type = typeMetric,
        name = module,
        subcategory = subcategory
    )
}
