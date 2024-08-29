package io.github.cdsap.comparescans.cli.view

import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import io.github.cdsap.compare2buildscans.output.formatBytes
import io.github.cdsap.comparescans.model.Entity
import io.github.cdsap.comparescans.model.TypeMetric
import io.github.cdsap.comparescans.rules.RuleMatched
import java.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class CompareView(
    private val rulesMatched: List<RuleMatched>
) {

    fun print() {
        if (rulesMatched.isNotEmpty()) {
            println(table())
            println(summaryRules())
        } else {
            println("No rules matched")
        }
    }

    fun generateReport() = table()

    private fun table() = table {
        val buildIds = rulesMatched.flatMap { it.metric.values.keys }.distinct()
        require(buildIds.size == 2) { "This implementation only supports exactly two builds." }

        val firstBuildId = buildIds[0]
        val secondBuildId = buildIds[1]

        cellStyle {
            border = true
            alignment = TextAlignment.MiddleLeft
            paddingLeft = 1
            paddingRight = 1
        }
        body {
            row {
                cell("Rules matching") {
                    columnSpan = 7
                    alignment = TextAlignment.MiddleCenter
                }
            }
            row {
                cell("Rule Entity")
                cell("Rule Type")
                cell("Name")
                cell("Category")
                cell("Diff")
                cell("Build $firstBuildId")
                cell("Build $secondBuildId")
            }
            rulesMatched.sortedBy { it.metric.metric.entity }.forEach {
                row {
                    val diffFormatted = when (it.diff) {
                        is Double -> {
                            if (it.metric.metric.type == TypeMetric.Counter) {
                                (it.diff as Double).toInt()
                            } else {
                                "${it.diff}%"
                            }
                        }
                        else -> {
                            ""
                        }
                    }

                    val name = if (it.metric.metric.entity == Entity.TaskType) {
                        it.metric.metric.name.substringAfterLast(".")
                    } else {
                        formatStringWithNewlines(it.metric.metric.name)
                    }

                    val firstBuildValue = it.metric.values[firstBuildId]?.toLong()
                    val secondBuildValue = it.metric.values[secondBuildId]?.toLong()

                    val firstBuildDisplay = if (firstBuildValue != null) {
                        when (it.metric.metric.type) {
                            TypeMetric.Counter -> firstBuildValue
                            TypeMetric.CacheSize -> formatBytes(firstBuildValue)
                            else -> Duration.ofMillis(firstBuildValue).toString()
                        }
                    } else {
                        "N/A"
                    }

                    val secondBuildDisplay = if (secondBuildValue != null) {
                        when (it.metric.metric.type) {
                            TypeMetric.Counter -> secondBuildValue
                            TypeMetric.CacheSize -> formatBytes(secondBuildValue)
                            else -> Duration.ofMillis(secondBuildValue).toString()
                        }
                    } else {
                        "N/A"
                    }

                    cell(it.rule.entity)
                    cell(it.rule.type)
                    cell(name)
                    cell(it.metric.metric.subcategory)
                    cell(diffFormatted) {
                        alignment = TextAlignment.MiddleRight
                    }
                    cell(firstBuildDisplay) {
                        alignment = TextAlignment.MiddleRight
                    }
                    cell(secondBuildDisplay) {
                        alignment = TextAlignment.MiddleRight
                    }
                }
            }
        }
    }

    private fun summaryRules() = table {
        cellStyle {
            border = true
            alignment = TextAlignment.MiddleLeft
            paddingLeft = 1
            paddingRight = 1
        }
        body {
            row {
                cell("Summary.\nTotal rules matched: ${rulesMatched.size}") {
                    columnSpan = 5
                    alignment = TextAlignment.MiddleCenter
                }
            }
            row {
                cell("Entity")
                cell("Type")
                cell("Threshold")
                cell("Value")
                cell("Occurrences")
            }
            rulesMatched.groupBy {
                it.rule
            }.forEach {
                row {
                    cell(it.key.entity)
                    cell(it.key.type)
                    cell(if (it.key.threshold != null) it.key.threshold!!.toDuration(DurationUnit.MILLISECONDS) else "") {
                        alignment = TextAlignment.MiddleRight
                    }
                    cell(if (it.key.value != null) "${it.key.value}%" else "") {
                        alignment = TextAlignment.MiddleRight
                    }
                    cell(it.value.size) {
                        alignment = TextAlignment.MiddleRight
                    }
                }
            }
        }
    }

    fun formatStringWithNewlines(input: String, maxLineLength: Int = 70): String {
        val sb = StringBuilder()
        var currentIndex = 0

        while (currentIndex < input.length) {
            val endIndex = (currentIndex + maxLineLength).coerceAtMost(input.length)
            sb.append(input.substring(currentIndex, endIndex))
            if (endIndex < input.length) {
                sb.append("\n")
            }
            currentIndex = endIndex
        }

        return sb.toString()
    }
}
