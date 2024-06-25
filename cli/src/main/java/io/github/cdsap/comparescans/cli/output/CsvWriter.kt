package io.github.cdsap.compare2buildscans.output

import io.github.cdsap.comparescans.model.Entity
import io.github.cdsap.comparescans.model.Metric
import io.github.cdsap.comparescans.model.TypeMetric
import io.github.cdsap.comparescans.rules.RuleMatched
import java.io.BufferedWriter
import java.io.File
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class CsvWriter(private val firstBuildScan: String, private val secondBuildScan: String) {

    fun metricsCsv(metrics: List<Metric>) {
        val prefixFile = "compare-$firstBuildScan-$secondBuildScan"
        val csv = "$prefixFile.csv"
        val headers =
            "entity,name,category,type,$firstBuildScan,$secondBuildScan\n"
        val startTimestamp = System.currentTimeMillis()
        File(csv).bufferedWriter().use { out: BufferedWriter ->
            out.write(headers)
            metrics.forEach { metric ->
                val line =
                    "${metric.entity.name},${metric.name},${metric.subcategory},${metric.type.name},${metric.firstBuild},${metric.secondBuild}\n"
                out.write(line)
            }
        }
        val endTime = System.currentTimeMillis()
        println("File $csv created in ${endTime - startTimestamp} ms")
    }

    fun matchedRulesCsv(matchedRules: List<RuleMatched>) {
        val prefixFile = "matched-rules-$firstBuildScan-$secondBuildScan"
        val csv = "$prefixFile-${System.currentTimeMillis()}.csv"
        val headers =
            "rule entity,rule type, name, category, diff,$firstBuildScan,$secondBuildScan,$firstBuildScan raw,$secondBuildScan raw, description\n"
        val startTimestamp = System.currentTimeMillis()
        File(csv).bufferedWriter().use { out: BufferedWriter ->
            out.write(headers)
            matchedRules.forEach { matched ->

                val diffFormatted = when (matched.diff) {
                    is Double -> {
                        if (matched.metric.type == TypeMetric.Counter) {
                            (matched.diff as Double).toInt()
                        } else {
                            "${matched.diff}%"
                        }
                    }

                    else -> {
                        ""
                    }
                }
                val name = if (matched.metric.entity == Entity.TaskType) {
                    matched.metric.name.substringAfterLast(".")
                } else {
                    matched.metric.name
                }

                val firstBuild = if (matched.metric.type == TypeMetric.Counter) {
                    matched.metric.firstBuild
                } else if (matched.metric.type == TypeMetric.CacheSize) {
                    formatBytes(matched.metric.firstBuild.toLong())
                } else {
                    matched.metric.firstBuild.toLong().toDuration(
                        DurationUnit.MILLISECONDS
                    )
                }
                val secondBuild = if (matched.metric.type == TypeMetric.Counter) {
                    matched.metric.secondBuild
                } else if (matched.metric.type == TypeMetric.CacheSize) {
                    formatBytes(matched.metric.secondBuild.toLong())
                } else {
                    matched.metric.secondBuild.toLong().toDuration(
                        DurationUnit.MILLISECONDS
                    )
                }

                var desc = ""
                when (matched.rule.type) {
                    TypeMetric.Duration, TypeMetric.Fingerprinting, TypeMetric.DurationMedian, TypeMetric.DurationMean,
                    TypeMetric.FingerprintingMedian, TypeMetric.FingerprintingMean, TypeMetric.FingerprintingP90,
                    TypeMetric.DurationP90, TypeMetric.CacheSize -> {
                        if (matched.rule.threshold != null) {
                            desc = "Threshold: ${matched.rule.threshold} "
                        }
                        if (matched.rule.value != null) {
                            desc += "Value > ${matched.rule.value}%"
                        }
                    }

                    TypeMetric.Counter -> {
                        desc += "Counter diff"
                    }
                }

                val line =
                    "${matched.rule.entity},${matched.rule.type},${matched.metric.subcategory},$name,$diffFormatted,$firstBuild,$secondBuild,${matched.metric.firstBuild},${matched.metric.secondBuild},${desc}\n"
                out.write(line)
            }
        }

        val endTime = System.currentTimeMillis()
        println("File $csv created in ${endTime - startTimestamp} ms")
    }
}

fun formatBytes(bytes: Long): String {
    val bytesInAKilobyte = 1_024.0
    val bytesInAMegabyte = bytesInAKilobyte * 1_024
    val bytesInAGigabyte = bytesInAMegabyte * 1_024

    return when {
        bytes >= bytesInAGigabyte -> String.format("%.2f GB", bytes / bytesInAGigabyte)
        bytes >= bytesInAMegabyte -> String.format("%.2f MB", bytes / bytesInAMegabyte)
        else -> String.format("%.2f KB", bytes / bytesInAKilobyte)
    }
}
