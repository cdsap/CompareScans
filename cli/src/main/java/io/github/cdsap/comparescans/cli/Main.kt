package io.github.cdsap.comparescans.cli

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import io.github.cdsap.compare2buildscans.output.CsvWriter
import io.github.cdsap.comparescans.cli.model.RulesYaml
import io.github.cdsap.comparescans.cli.view.CompareView
import io.github.cdsap.comparescans.model.Metric
import io.github.cdsap.comparescans.model.Rule
import io.github.cdsap.comparescans.rules.DefaultRules
import io.github.cdsap.comparescans.rules.RuleMatched
import io.github.cdsap.comparescans.rules.RuleProcessor
import kotlinx.coroutines.runBlocking
import java.io.File

fun main(args: Array<String>) {
    Experiment().main(args)
}

class Experiment : CliktCommand() {
    val from by option().choice("api", "file").required()
    private val apiKey by option()
    private val url by option()
    private val firstBuildScan by option()
    private val secondBuildScan by option()
    private val withDefaultRules by option().flag(default = false)
    private val customRules by option().file()
    private val metrics by option().file()

    override fun run() {
        runBlocking {
            // if custom rules have been provided, we need to validate that the file exists
            if (customRules != null) {
                parseYaml(customRules!!)
            }

            val metricsBuild = when (from) {
                "api" -> {
                    validate(apiKey != null && url != null && firstBuildScan != null && secondBuildScan != null) {
                        "Missing required parameters for api: example: --from api --apiKey <apiKey> --url <url> --firstBuildScan <firstBuildScan> --secondBuildScan <secondBuildScan>"
                    }
                    GetApiMetrics(apiKey!!, url!!, firstBuildScan!!, secondBuildScan!!).getMetrics()
                }

                "file" -> {
                    validate(metrics != null) {
                        "Missing required parameters for file: example: --from file --existingMetrics <existingMetrics.csv>"
                    }
                    GetFileMetrics(metrics!!).getMetrics()
                }

                else -> {
                    throw IllegalArgumentException("Invalid value for --from: $from")
                }
            }

            val rulesMatched = applyRulesIfPresent(metricsBuild, withDefaultRules, customRules)

            if (rulesMatched.isNotEmpty()) {
                if (from == "api") {
                    CompareView(rulesMatched, firstBuildScan!!, secondBuildScan!!).print()
                    CsvWriter(firstBuildScan!!, secondBuildScan!!).matchedRulesCsv(rulesMatched)
                } else {
                    val buildScans = getLastTwoItemsAsPair(metrics!!.readLines().first())
                    CompareView(rulesMatched, buildScans.first, buildScans.second).print()
                    CsvWriter(buildScans.first, buildScans.second).matchedRulesCsv(rulesMatched)
                }
            }
            if (from == "api") {
                CsvWriter(firstBuildScan!!, secondBuildScan!!).metricsCsv(metricsBuild)
            } else if (from == "file" && customRules == null && !withDefaultRules) {
                println("No rules provided. If you want to apply rules from existing metrics file, please provide a custom rules with --custom-rules custom-rules.yaml or use the --with-default-rules flag.")
            }
        }
    }
}

fun getLastTwoItemsAsPair(line: String): Pair<String, String> {
    val items = line.split(",")
    return if (items.size >= 2) {
        Pair(items[items.size - 2], items[items.size - 1])
    } else {
        Pair("First", "Second")
    }
}

private fun validate(b: Boolean, function: () -> String) {
    if (!b) {
        throw IllegalArgumentException(function())
    }
}

private fun applyRulesIfPresent(
    metrics: List<Metric>,
    withDefaultRules: Boolean,
    customRules: File?
): List<RuleMatched> {
    return if (withDefaultRules) {
        RuleProcessor().ruleProcessor(metrics, DefaultRules().get())
    } else if (customRules != null) {
        val rules = parseYaml(customRules).rules.flatMap {
            listOf(
                Rule(
                    it.entity,
                    it.type,
                    if (it.threshold == -1L) null else it.threshold,
                    if (it.value == -1) null else it.value
                )
            )
        }
        RuleProcessor().ruleProcessor(metrics, rules)
    } else {
        emptyList<RuleMatched>()
    }
}

private fun parseYaml(rules: File): RulesYaml {
    val mapper = ObjectMapper(YAMLFactory()).apply {
        registerModule(KotlinModule())
    }
    return mapper.readValue(rules)
}
