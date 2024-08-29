package io.github.cdsap.comparescans.cli

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import io.github.cdsap.compare2buildscans.output.CsvWriter
import io.github.cdsap.comparescans.cli.model.RulesYaml
import io.github.cdsap.comparescans.cli.view.CompareView
import io.github.cdsap.comparescans.model.MultipleBuildScanMetric
import io.github.cdsap.comparescans.model.Rule
import io.github.cdsap.comparescans.rules.DefaultRules
import io.github.cdsap.comparescans.rules.RuleMatched
import io.github.cdsap.comparescans.rules.RuleProcessor
import kotlinx.coroutines.runBlocking
import java.io.File

fun main(args: Array<String>) {
    Experiment().main(args)
//    val gson = Gson()
//    val buildListType = object : TypeToken<List<BuildWithResourceUsage>>() {}.type
//    GetApiMetrics(apiKey!!, url!!, scans).getMetrics()
// //    val data: List<BuildWithResourceUsage> = gson.fromJson(File("builds.json").readText(), buildListType)
// //
// //
// //    val a = MultipleScanMetrics(data).get()
// //    a.filter { it.metric.entity == Entity.Project }.forEach {
// //        println("-")
// //        println(it.metric.entity)
// //        println(it.values)
// //    }
//    CsvWriter(CsvWriter).metricsCsvMultipleScans(a, listOf("fqlukj7otcqek", "sctmqbxo3vd4q", "mky4xonu5cuwg", "wc5cbudsoalse"))
}

class Experiment : CliktCommand() {
    val from by option().choice("api", "file").required()
    private val apiKey by option()
    private val url by option()
    private val withDefaultRules by option().flag(default = false)
    private val customRules by option().file()
    private val metrics by option().file()
    private val scans by option().multiple()

    override fun run() {
        runBlocking {
            // if custom rules have been provided, we need to validate that the file exists
            if (customRules != null) {
                parseYaml(customRules!!)
            }

            val metricsBuild = when (from) {
                "api" -> {
                    validate(apiKey != null && url != null && (scans != null || scans.isEmpty())) {
                        "Missing required parameters for api: example: --from api --apiKey <apiKey> --url <url> --scans <firstBuildScan> --scans <secondBuildScan>"
                    }
                    GetApiMetrics(apiKey!!, url!!, scans).getMetrics()
                }

                "file" -> {
                    validate(metrics != null) {
                        "Missing required parameters for file: example: --from file --existingMetrics <existingMetrics.csv>"
                    }
                    val a = GetFileMetrics(metrics!!).getMetrics()
                    println(a.size)
                    a
                }

                else -> {
                    throw IllegalArgumentException("Invalid value for --from: $from")
                }
            }

            val rulesMatched = applyRulesIfPresent(metricsBuild, withDefaultRules, customRules)

            if (rulesMatched.isNotEmpty()) {
                if (from == "api") {
                    CompareView(rulesMatched).print()
                    CsvWriter().matchedRulesCsv(rulesMatched, scans[0]!!, scans[1]!!)
                } else {
                    val buildScans = getLastTwoItemsAsPair(metrics!!.readLines().first())
                    CompareView(rulesMatched).print()
                    CsvWriter().matchedRulesCsv(rulesMatched, buildScans.first, buildScans.second)
                }
            }
            if (from == "api") {
                CsvWriter().metricsCsvMultipleScans(metricsBuild, scans)
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
    metrics: List<MultipleBuildScanMetric>,
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
