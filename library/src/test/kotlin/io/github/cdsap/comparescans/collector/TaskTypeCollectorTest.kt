package io.github.cdsap.comparescans.collector

import io.github.cdsap.comparescans.MockBuilds
import io.github.cdsap.comparescans.model.TypeMetric
import kotlin.test.Test

class TaskTypeCollectorTest {

    @Test
    fun taskTypeMetricProcessesDurationCounterAndFingerprint() {
        val (firstBuild, secondBuild) = MockBuilds().returnBuilds()
        val firstBuildTasks = firstBuild.taskExecution
        val secondBuildTasks = secondBuild.taskExecution

        val outcomesFirst = firstBuildTasks.groupBy { it.avoidanceOutcome }.flatMap { listOf(it.key) }.distinct()
        val outcomesSecond = secondBuildTasks.groupBy { it.avoidanceOutcome }.flatMap { listOf(it.key) }.distinct()

        val firstMetric =
            TaskTypeCollector().measurementTaskTypes(firstBuildTasks, outcomesFirst.toSet(), firstBuild.id)
                .filter { it.metric.name == "org.gradle.api.DefaultTask" && it.metric.subcategory == "all outcomes" }
        val secondMetric =
            TaskTypeCollector().measurementTaskTypes(secondBuildTasks, outcomesSecond.toSet(), secondBuild.id)
                .filter { it.metric.name == "org.gradle.api.DefaultTask" && it.metric.subcategory == "all outcomes" }

        val firstDuration =
            firstBuild.taskExecution.filter { it.taskType == "org.gradle.api.DefaultTask" }
                .sumOf { it.duration }
        val secondDuration =
            secondBuild.taskExecution.filter { it.taskType == "org.gradle.api.DefaultTask" }
                .sumOf { it.duration }

        val firstFinger =
            firstBuild.taskExecution.filter { it.taskType == "org.gradle.api.DefaultTask" }
                .sumOf { it.fingerprintingDuration }
        val secondFinger = secondBuild.taskExecution.filter { it.taskType == "org.gradle.api.DefaultTask" }
            .sumOf { it.fingerprintingDuration }

        assert(firstMetric.any { it.metric.type == TypeMetric.Counter })
        assert(firstMetric.any { it.metric.type == TypeMetric.Fingerprinting })
        assert(firstMetric.any { it.metric.type == TypeMetric.Duration })
        assert(firstDuration == firstMetric.first { it.metric.type == TypeMetric.Duration }.value)
        assert(secondDuration == secondMetric.first { it.metric.type == TypeMetric.Duration }.value)
        assert(firstFinger == firstMetric.first { it.metric.type == TypeMetric.Fingerprinting }.value)
        assert(secondFinger == firstMetric.first { it.metric.type == TypeMetric.Fingerprinting }.value)
    }
}
