package io.github.cdsap.comparescans.collector

import io.github.cdsap.comparescans.RegularBuilds
import io.github.cdsap.comparescans.model.TypeMetric
import kotlin.test.Test

class TaskTypeCollectorTest {

    @Test
    fun taskTypeMetricProcessesDurationCounterAndFingerprint() {
        val (firstBuild, secondBuild) = RegularBuilds().returnBuilds()
        val firstBuildTasks = firstBuild.taskExecution
        val secondBuildTasks = secondBuild.taskExecution

        val outcomesFirst = firstBuildTasks.groupBy { it.avoidanceOutcome }.flatMap { listOf(it.key) }.distinct()
        val outcomesSecond = secondBuildTasks.groupBy { it.avoidanceOutcome }.flatMap { listOf(it.key) }.distinct()
        val outcomes = outcomesFirst.union(outcomesSecond)

        val metric = TaskTypeCollector().taskTypes(firstBuildTasks, secondBuildTasks, outcomes)
            .filter { it.name == "org.gradle.api.tasks.bundling.Jar" && it.subcategory == "all outcomes" }
        val firstDuration =
            firstBuild.taskExecution.filter { it.taskType == "org.gradle.api.tasks.bundling.Jar" }.sumOf { it.duration }
        val secondDuration = secondBuild.taskExecution.filter { it.taskType == "org.gradle.api.tasks.bundling.Jar" }
            .sumOf { it.duration }

        val firstFinger =
            firstBuild.taskExecution.filter { it.taskType == "org.gradle.api.tasks.bundling.Jar" }
                .sumOf { it.fingerprintingDuration }
        val secondFinger = secondBuild.taskExecution.filter { it.taskType == "org.gradle.api.tasks.bundling.Jar" }
            .sumOf { it.fingerprintingDuration }

        assert(metric.any { it.type == TypeMetric.Counter })
        assert(metric.any { it.type == TypeMetric.Fingerprinting })
        assert(metric.any { it.type == TypeMetric.Duration })
        assert(firstDuration == metric.first { it.type == TypeMetric.Duration }.firstBuild && secondDuration == metric.first { it.type == TypeMetric.Duration }.secondBuild)
        assert(firstFinger == metric.first { it.type == TypeMetric.Fingerprinting }.firstBuild && secondFinger == metric.first { it.type == TypeMetric.Fingerprinting }.secondBuild)
    }
}
