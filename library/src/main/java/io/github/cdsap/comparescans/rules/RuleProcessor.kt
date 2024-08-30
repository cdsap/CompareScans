package io.github.cdsap.comparescans.rules

import io.github.cdsap.comparescans.model.MultipleBuildScanMetric
import io.github.cdsap.comparescans.model.Rule
import io.github.cdsap.comparescans.model.TypeMetric

class RuleProcessor {

    fun ruleProcessor(metrics: List<MultipleBuildScanMetric>, rules: List<Rule>): List<RuleMatched> {
        val scanIds = metrics.flatMap { it.values.keys }.distinct()
        println(scanIds.size)
        require(scanIds.size == 2) { "This implementation only supports exactly two builds." }

        val firstBuildId = scanIds[0]
        val secondBuildId = scanIds[1]

        // Filter metrics to avoid duplicates in case of task type is only executing a given outcome
        val filteredMetrics = metrics.groupBy { metric ->
            Quintuple(metric.metric.entity, metric.metric.type, metric.metric.name, metric.values[firstBuildId], metric.values[secondBuildId])
        }.flatMap { (_, groupedMetrics) ->
            if (groupedMetrics.size > 1) {
                groupedMetrics.filter { it.metric.subcategory != "all" }
            } else {
                groupedMetrics
            }
        }

        val ruleMatches = mutableListOf<RuleMatched>()

        rules.forEach { rule ->
            filteredMetrics.filter {
                it.metric.entity == rule.entity && it.metric.type == rule.type
            }.forEach {
                val firstBuildValue = it.values[firstBuildId]?.toLong() ?: return@forEach
                val secondBuildValue = it.values[secondBuildId]?.toLong() ?: return@forEach

                when (it.metric.type) {
                    TypeMetric.Duration, TypeMetric.Fingerprinting, TypeMetric.DurationMedian, TypeMetric.DurationMean,
                    TypeMetric.FingerprintingMedian, TypeMetric.FingerprintingMean, TypeMetric.FingerprintingP90,
                    TypeMetric.DurationP90 -> {
                        if (rule.threshold != null) {
                            if (firstBuildValue > rule.threshold && secondBuildValue > rule.threshold) {
                                val diff = absolutePercentageDifferenceWithSign(firstBuildValue, secondBuildValue)
                                if (rule.value == null) {
                                    ruleMatches.add(RuleMatched(it, rule, diff.first))
                                } else if (diff.first >= rule.value!!.toDouble()) {
                                    ruleMatches.add(RuleMatched(it, rule, diff.first))
                                }
                            }
                        }
                    }

                    TypeMetric.Counter -> {
                        if (firstBuildValue != secondBuildValue) {
                            val difference = kotlin.math.abs(firstBuildValue - secondBuildValue)
                            ruleMatches.add(RuleMatched(it, rule, difference.toDouble()))
                        }
                    }

                    TypeMetric.CacheSize -> {
                        if (rule.threshold != null) {
                            if (firstBuildValue > rule.threshold && secondBuildValue > rule.threshold) {
                                val diff = absolutePercentageDifferenceWithSign(firstBuildValue, secondBuildValue)
                                if (rule.value == null) {
                                    ruleMatches.add(RuleMatched(it, rule, diff.first))
                                } else if (diff.first >= rule.value!!.toDouble()) {
                                    ruleMatches.add(RuleMatched(it, rule, diff.first))
                                }
                            }
                        }
                    }
                    // not covering for now resource usage metrics
                    else -> {}
                }
            }
        }
        return ruleMatches
    }
}

data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

data class RuleMatched(
    val metric: MultipleBuildScanMetric,
    val rule: Rule,
    val diff: Any
)

fun absolutePercentageDifferenceWithSign(value1: Long, value2: Long): Pair<Double, String> {
    val difference = value1 - value2
    val absoluteDifference = kotlin.math.abs(difference)
    val averageValue = (value1 + value2) / 2.0
    val percentageDifference = (absoluteDifference / averageValue) * 100

    val roundedPercentageDifference = String.format("%.2f", percentageDifference).toDouble()
    val sign = if (difference > 0) "+" else "-"

    return Pair(roundedPercentageDifference, sign)
}
