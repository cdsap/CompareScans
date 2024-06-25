package io.github.cdsap.comparescans.rules

import io.github.cdsap.comparescans.model.Metric
import io.github.cdsap.comparescans.model.Rule
import io.github.cdsap.comparescans.model.TypeMetric

class RuleProcessor {

    fun ruleProcessor(metrics: List<Metric>, rules: List<Rule>): List<RuleMatched> {
        // in cases like TaskTypes, it's possible we have a duplicated metric in case of the task type is only executing
        // a given outcome
        val filteredMetrics = metrics.groupBy { metric ->
            Quintuple(metric.entity, metric.type, metric.name, metric.firstBuild, metric.secondBuild)
        }.flatMap { (_, groupedMetrics) ->
            if (groupedMetrics.size > 1) {
                groupedMetrics.filter { it.subcategory != "all" }
            } else {
                groupedMetrics
            }
        }

        val ruleMatches = mutableListOf<RuleMatched>()

        rules.forEach { rule ->
            filteredMetrics.filter {
                it.entity == rule.entity && it.type == rule.type
            }.forEach {
                when (it.type) {
                    TypeMetric.Duration, TypeMetric.Fingerprinting, TypeMetric.DurationMedian, TypeMetric.DurationMean,
                    TypeMetric.FingerprintingMedian, TypeMetric.FingerprintingMean, TypeMetric.FingerprintingP90,
                    TypeMetric.DurationP90 -> {
                        if (rule.threshold != null) {
                            if (it.firstBuild.toLong() > rule.threshold && it.secondBuild.toLong() > rule.threshold) {
                                val diff =
                                    absolutePercentageDifferenceWithSign(
                                        it.firstBuild.toLong(),
                                        it.secondBuild.toLong()
                                    )
                                if (rule.value == null) {
                                    ruleMatches.add(RuleMatched(it, rule, diff.first))
                                } else if (diff.first >= rule.value!!.toDouble()) {
                                    ruleMatches.add(RuleMatched(it, rule, diff.first))
                                }
                            }
                        }
                    }

                    TypeMetric.Counter -> {
                        if (it.firstBuild != it.secondBuild) {
                            val difference = kotlin.math.abs(it.firstBuild.toLong() - it.secondBuild.toLong())
                            ruleMatches.add(RuleMatched(it, rule, difference.toDouble()))
                        }
                    }

                    TypeMetric.CacheSize -> {
                        if (rule.threshold != null) {
                            if (it.firstBuild.toLong() > rule.threshold && it.secondBuild.toLong() > rule.threshold) {
                                val diff =
                                    absolutePercentageDifferenceWithSign(
                                        it.firstBuild.toLong(),
                                        it.secondBuild.toLong()
                                    )
                                if (rule.value == null) {
                                    ruleMatches.add(RuleMatched(it, rule, diff.first))
                                } else if (diff.first >= rule.value!!.toDouble()) {
                                    ruleMatches.add(RuleMatched(it, rule, diff.first))
                                }
                            }
                        }
                    }
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
    val metric: Metric,
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
