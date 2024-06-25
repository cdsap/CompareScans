package io.github.cdsap.comparescans.collector

import io.github.cdsap.comparescans.model.Entity
import io.github.cdsap.comparescans.model.Metric
import io.github.cdsap.comparescans.model.TypeMetric
import io.github.cdsap.geapi.client.model.Task
import org.nield.kotlinstatistics.median
import org.nield.kotlinstatistics.percentile

class ModuleMetricCollector {

    fun process(
        firstBuildTasks: Array<Task>,
        secondBuildTasks: Array<Task>,
        outcomes: Set<String>
    ): List<Metric> {
        val modulesFirstBuildScan = firstBuildTasks.flatMap { listOf(moduleParsed(it)) }.distinct()
        val modulesSecondBuildScan = secondBuildTasks.flatMap { listOf(moduleParsed(it)) }.distinct()
        val modules = modulesFirstBuildScan.union(modulesSecondBuildScan)
        return metricsModules(firstBuildTasks, secondBuildTasks, modules) + moduleWithOutcomeMetrics(
            firstBuildTasks,
            secondBuildTasks,
            modules,
            outcomes
        )
    }

    private fun moduleWithOutcomeMetrics(
        firstBuildTasks: Array<Task>,
        secondBuildTasks: Array<Task>,
        modules: Set<String>,
        outcomes: Set<String>
    ): List<Metric> {
        val metrics = mutableListOf<Metric>()
        modules.forEach { module ->
            for (outcome in outcomes) {
                val first = firstBuildTasks.filter {
                    moduleParsed(it) == module &&
                        it.avoidanceOutcome == outcome
                }
                val second = secondBuildTasks.filter {
                    moduleParsed(it) == module &&
                        it.avoidanceOutcome == outcome
                }
                extracted(first, second, metrics, outcome, module)
            }
        }
        return metrics
    }

    private fun extracted(
        first: List<Task>,
        second: List<Task>,
        metrics: MutableList<Metric>,
        outcome: String,
        module: String
    ) {
        val firstDuration = first.sumOf { it.duration }
        val secondDuration = second.sumOf { it.duration }
        val firstCount = first.count()
        val secondCount = second.count()
        val firstFinger = first.sumOf { it.fingerprintingDuration }
        val secondFinger = second.sumOf { it.fingerprintingDuration }
        val firstMeanDuration = if (firstDuration > 0) firstDuration / firstCount else 0
        val secondMeanDuration = if (secondDuration > 0) secondDuration / secondCount else 0
        val firstMedianDuration = first.map { it.duration }.median().round()
        val secondMedianDuration = second.map { it.duration }.median().round()
        val firstP90Duration = first.map { it.duration }.percentile(90.0).round()
        val secondP90Duration = second.map { it.duration }.percentile(90.0).round()
        val firstMeanFinger = if (firstFinger > 0) firstFinger / first.count() else 0
        val secondMeanFinger = if (secondFinger > 0) secondFinger / second.count() else 0
        val firstMedianFinger = first.map { it.fingerprintingDuration }.median().round()
        val secondMedianFinger = second.map { it.fingerprintingDuration }.median().round()
        val firstP90Finger = first.map { it.fingerprintingDuration }.percentile(90.0).round()
        val secondP90Finger = second.map { it.fingerprintingDuration }.percentile(90.0).round()

        metrics.add(m(TypeMetric.Duration, outcome, module, firstDuration, secondDuration))
        metrics.add(m(TypeMetric.Counter, outcome, module, firstCount, secondCount))
        metrics.add(m(TypeMetric.Fingerprinting, outcome, module, firstFinger, secondFinger))
        metrics.add(m(TypeMetric.DurationMean, outcome, module, firstMeanDuration, secondMeanDuration))
        metrics.add(m(TypeMetric.DurationMedian, outcome, module, firstMedianDuration, secondMedianDuration))
        metrics.add(m(TypeMetric.DurationP90, outcome, module, firstP90Duration, secondP90Duration))
        metrics.add(m(TypeMetric.FingerprintingMean, outcome, module, firstMeanFinger, secondMeanFinger))
        metrics.add(m(TypeMetric.FingerprintingMedian, outcome, module, firstMedianFinger, secondMedianFinger))
        metrics.add(m(TypeMetric.FingerprintingP90, outcome, module, firstP90Finger, secondP90Finger))
    }

    private fun metricsModules(
        firstBuildTasks: Array<Task>,
        secondBuildTasks: Array<Task>,
        modules: Set<String>
    ): List<Metric> {
        val metrics = mutableListOf<Metric>()
        modules.forEach { module ->
            val first = firstBuildTasks.filterByModule(module)
            val second = secondBuildTasks.filterByModule(module)
            val firstCache = first.filter { it.cacheArtifactSize != null }.sumOf { it.cacheArtifactSize!! }
            val secondCache = second.filter { it.cacheArtifactSize != null }.sumOf { it.cacheArtifactSize!! }
            metrics.add(m(TypeMetric.CacheSize, "all tasks", module, firstCache, secondCache))
            extracted(first, second, metrics, "all tasks", module)
        }
        return metrics
    }

    private fun m(
        typeMetric: TypeMetric,
        outcome: String,
        module: String,
        durationFirst: Number,
        durationSecond: Number
    ) = Metric(
        entity = Entity.Module,
        type = typeMetric,
        name = module,
        subcategory = outcome,
        firstBuild = durationFirst,
        secondBuild = durationSecond
    )
}

fun moduleParsed(it: Task) = it.taskPath.split(":").dropLast(1).joinToString(":")

fun Array<Task>.filterByModule(module: String) = this.filter { moduleParsed(it) == module }
