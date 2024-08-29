package io.github.cdsap.comparescans.collector

import io.github.cdsap.comparescans.model.Entity
import io.github.cdsap.comparescans.model.Measurement
import io.github.cdsap.comparescans.model.Metric
import io.github.cdsap.comparescans.model.TypeMetric
import io.github.cdsap.geapi.client.model.Task
import org.nield.kotlinstatistics.median
import org.nield.kotlinstatistics.percentile

class ModuleMetricCollector {

    fun singleModuleMetrics(
        firstBuildTasks: Array<Task>,
        outcomes: Set<String>,
        variant: String
    ): List<Measurement> {
        val modulesFirstBuildScan = firstBuildTasks.flatMap { listOf(moduleParsed(it)) }.distinct()
        return singleMetricsModules(firstBuildTasks, modulesFirstBuildScan.toSet(), variant) + singleModuleWithOutcomeMetrics(
            firstBuildTasks,
            modulesFirstBuildScan.toSet(),
            outcomes,
            variant
        )
    }

    private fun singleModuleWithOutcomeMetrics(
        firstBuildTasks: Array<Task>,
        modules: Set<String>,
        outcomes: Set<String>,
        variant: String
    ): List<Measurement> {
        val metrics = mutableListOf<Measurement>()
        modules.forEach { module ->
            for (outcome in outcomes) {
                val first = firstBuildTasks.filter {
                    moduleParsed(it) == module &&
                        it.avoidanceOutcome == outcome
                }
                extractedSingle(first, metrics, outcome, module, variant)
            }
        }
        return metrics
    }

    private fun extractedSingle(
        first: List<Task>,
        metrics: MutableList<Measurement>,
        outcome: String,
        module: String,
        variant: String
    ) {
        val firstDuration = first.sumOf { it.duration }
        val firstCount = first.count()
        val firstFinger = first.sumOf { it.fingerprintingDuration }
        val firstMeanDuration = if (firstDuration > 0) firstDuration / firstCount else 0
        val firstMedianDuration = first.map { it.duration }.median().round()
        val firstP90Duration = first.map { it.duration }.percentile(90.0).round()
        val firstMeanFinger = if (firstFinger > 0) firstFinger / first.count() else 0
        val firstMedianFinger = first.map { it.fingerprintingDuration }.median().round()
        val firstP90Finger = first.map { it.fingerprintingDuration }.percentile(90.0).round()

        metrics.add(Measurement(variant, singleM(TypeMetric.Duration, outcome, module), firstDuration))
        metrics.add(Measurement(variant, singleM(TypeMetric.Counter, outcome, module), firstCount))
        metrics.add(Measurement(variant, singleM(TypeMetric.Fingerprinting, outcome, module), firstFinger))
        metrics.add(Measurement(variant, singleM(TypeMetric.DurationMean, outcome, module), firstMeanDuration))
        metrics.add(Measurement(variant, singleM(TypeMetric.DurationMedian, outcome, module), firstMedianDuration))
        metrics.add(Measurement(variant, singleM(TypeMetric.DurationP90, outcome, module), firstP90Duration))
        metrics.add(Measurement(variant, singleM(TypeMetric.FingerprintingMean, outcome, module), firstMeanFinger))
        metrics.add(Measurement(variant, singleM(TypeMetric.FingerprintingMedian, outcome, module), firstMedianFinger))
        metrics.add(Measurement(variant, singleM(TypeMetric.FingerprintingP90, outcome, module), firstP90Finger))
    }

    private fun singleMetricsModules(
        firstBuildTasks: Array<Task>,
        modules: Set<String>,
        variant: String
    ): List<Measurement> {
        val metrics = mutableListOf<Measurement>()
        modules.forEach { module ->
            val first = firstBuildTasks.filterByModule(module)
            val firstCache = first.filter { it.cacheArtifactSize != null }.sumOf { it.cacheArtifactSize!! }

            metrics.add(Measurement(variant, singleM(TypeMetric.CacheSize, "all tasks", module), firstCache))
            extractedSingle(first, metrics, "all tasks", module, variant)
        }
        return metrics
    }

    private fun singleM(
        typeMetric: TypeMetric,
        outcome: String,
        module: String
    ) = Metric(
        entity = Entity.Module,
        type = typeMetric,
        name = module,
        subcategory = outcome
    )
}

fun moduleParsed(it: Task) = it.taskPath.split(":").dropLast(1).joinToString(":")

fun Array<Task>.filterByModule(module: String) = this.filter { moduleParsed(it) == module }
