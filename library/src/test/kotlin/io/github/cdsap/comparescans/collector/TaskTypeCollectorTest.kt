package io.github.cdsap.comparescans.collector

import io.github.cdsap.comparescans.MockBuilds
import io.github.cdsap.comparescans.model.TypeMetric
import kotlin.test.Test

class TaskTypeCollectorTest {

    @Test
    fun taskTypeMetricProcessesDurationCounterAndFingerprint() {
        val (firstBuild, secondBuild) = MockBuilds().returnBuilds()
        val firstBuildTasks = firstBuild.build.taskExecution
        val secondBuildTasks = secondBuild.build.taskExecution

        val outcomesFirst = firstBuildTasks.groupBy { it.avoidanceOutcome }.flatMap { listOf(it.key) }.distinct()
        val outcomesSecond = secondBuildTasks.groupBy { it.avoidanceOutcome }.flatMap { listOf(it.key) }.distinct()

        val firstMetric =
            TaskTypeCollector().measurementTaskTypes(firstBuildTasks, outcomesFirst.toSet(), firstBuild.build.id)
                .filter { it.metric.name == "org.gradle.api.tasks.bundling.Jar" && it.metric.subcategory == "all outcomes" }
        val secondMetric =
            TaskTypeCollector().measurementTaskTypes(secondBuildTasks, outcomesSecond.toSet(), secondBuild.build.id)
                .filter { it.metric.name == "org.gradle.api.tasks.bundling.Jar" && it.metric.subcategory == "all outcomes" }

        val firstDuration =
            firstBuild.build.taskExecution.filter { it.taskType == "org.gradle.api.tasks.bundling.Jar" }
                .sumOf { it.duration }
        val secondDuration =
            secondBuild.build.taskExecution.filter { it.taskType == "org.gradle.api.tasks.bundling.Jar" }
                .sumOf { it.duration }

        val firstFinger =
            firstBuild.build.taskExecution.filter { it.taskType == "org.gradle.api.tasks.bundling.Jar" }
                .sumOf { it.fingerprintingDuration }
        val secondFinger = secondBuild.build.taskExecution.filter { it.taskType == "org.gradle.api.tasks.bundling.Jar" }
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
