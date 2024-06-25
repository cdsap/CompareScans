package io.github.cdsap.comparescans.cli.view

import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import io.github.cdsap.compare2buildscans.output.formatBytes
import io.github.cdsap.comparescans.model.Entity
import io.github.cdsap.comparescans.model.TypeMetric
import io.github.cdsap.comparescans.rules.RuleMatched
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class CompareView(
    private val rulesMatched: List<RuleMatched>,
    private val firstBuild: String,
    private val secondBuild: String
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
                cell("Subcategory")
                cell("Diff")
                cell("Build $firstBuild")
                cell("Build $secondBuild")
            }
            rulesMatched.sortedBy { it.metric.entity }.forEach {
                row {
                    val diffFormatted = when (it.diff) {
                        is Double -> {
                            if (it.metric.type == TypeMetric.Counter) {
                                (it.diff as Double).toInt()
                            } else {
                                "${it.diff}%"
                            }
                        }
                        else -> {
                            ""
                        }
                    }

                    cell(it.rule.entity)
                    cell(it.rule.type)
                    cell(
                        if (it.metric.entity == Entity.TaskType) {
                            it.metric.name.substringAfterLast(".")
                        } else {
                            formatStringWithNewlines(it.metric.name)
                        }
                    )
                    cell(it.metric.subcategory)
                    cell(diffFormatted) {
                        alignment = TextAlignment.MiddleRight
                    }
                    cell(
                        if (it.metric.type == TypeMetric.Counter) {
                            it.metric.firstBuild
                        } else if (it.metric.type == TypeMetric.CacheSize) {
                            formatBytes(it.metric.firstBuild.toLong())
                        } else {
                            it.metric.firstBuild.toLong().toDuration(
                                DurationUnit.MILLISECONDS
                            )
                        }
                    ) {
                        alignment = TextAlignment.MiddleRight
                    }
                    cell(
                        if (it.metric.type == TypeMetric.Counter) {
                            it.metric.secondBuild
                        } else if (it.metric.type == TypeMetric.CacheSize) {
                            formatBytes(it.metric.secondBuild.toLong())
                        } else {
                            it.metric.secondBuild.toLong().toDuration(
                                DurationUnit.MILLISECONDS
                            )
                        }
                    ) {
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
