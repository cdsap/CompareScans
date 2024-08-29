package io.github.cdsap.compare2buildscans.output

import io.github.cdsap.comparescans.model.Entity
import io.github.cdsap.comparescans.model.MultipleBuildScanMetric
import io.github.cdsap.comparescans.model.TypeMetric
import io.github.cdsap.comparescans.rules.RuleMatched
import java.io.BufferedWriter
import java.io.File
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class CsvWriter() {

    fun metricsCsvMultipleScans(metrics: List<MultipleBuildScanMetric>, buildScans: List<String>) {
        val prefixFile = "metrics-${buildScans.joinToString("-")}"
        val csv = "$prefixFile.csv"
        val headers = "entity,name,category,type,${buildScans.joinToString(",")}\n"
        val startTimestamp = System.currentTimeMillis()
        File(csv).bufferedWriter().use { out: BufferedWriter ->
            out.write(headers)
            metrics.forEach { metric ->
                val line = buildString {
                    append("${metric.metric.entity.name},${metric.metric.name},${metric.metric.subcategory},${metric.metric.type.name}")
                    buildScans.forEach { buildScan ->
                        append(",${metric.values[buildScan] ?: -1L}")
                    }
                    append("\n")
                }
                out.write(line)
            }
        }
        val endTime = System.currentTimeMillis()
        println("File $csv created in ${endTime - startTimestamp} ms")
    }

    fun matchedRulesCsv(
        matchedRules: List<RuleMatched>,
        firstBuildScan: String,
        secondBuildScan: String
    ) {
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
                        if (matched.metric.metric.type == TypeMetric.Counter) {
                            (matched.diff as Double).toInt()
                        } else {
                            "${matched.diff}%"
                        }
                    }
                    else -> {
                        ""
                    }
                }

                val name = if (matched.metric.metric.entity == Entity.TaskType) {
                    matched.metric.metric.name.substringAfterLast(".")
                } else {
                    matched.metric.metric.name
                }

                val firstBuildValue = matched.metric.values[firstBuildScan]?.toLong()
                val secondBuildValue = matched.metric.values[secondBuildScan]?.toLong()

                val firstBuild = if (firstBuildValue != null) {
                    when (matched.metric.metric.type) {
                        TypeMetric.Counter -> firstBuildValue
                        TypeMetric.CacheSize -> formatBytes(firstBuildValue)
                        else -> firstBuildValue.toDuration(DurationUnit.MILLISECONDS)
                    }
                } else {
                    "N/A"
                }

                val secondBuild = if (secondBuildValue != null) {
                    when (matched.metric.metric.type) {
                        TypeMetric.Counter -> secondBuildValue
                        TypeMetric.CacheSize -> formatBytes(secondBuildValue)
                        else -> secondBuildValue.toDuration(DurationUnit.MILLISECONDS)
                    }
                } else {
                    "N/A"
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
                    // not covering resource usage metrics
                    else -> {
                    }
                }

                val line = "${matched.rule.entity},${matched.rule.type},$name,${matched.metric.metric.subcategory},$diffFormatted,$firstBuild,$secondBuild,${firstBuildValue ?: "N/A"},${secondBuildValue ?: "N/A"},${desc}\n"
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
