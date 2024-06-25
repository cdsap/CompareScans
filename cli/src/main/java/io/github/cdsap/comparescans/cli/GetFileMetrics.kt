package io.github.cdsap.comparescans.cli

import io.github.cdsap.comparescans.model.Entity
import io.github.cdsap.comparescans.model.Metric
import io.github.cdsap.comparescans.model.TypeMetric
import java.io.File

class GetFileMetrics(private val file: File) : GetMetrics {
    override suspend fun getMetrics(): List<Metric> {
        println("Processing existing metrics from ${file.name}")

        val metrics = mutableListOf<Metric>()
        val lines = file.readLines()

        for (line in lines.drop(1)) {
            val values = line.split(",")

            val entity = Entity.valueOf(values[0])
            val typeMetric = TypeMetric.valueOf(values[3])
            val outcome = values[2]
            val name = values[1]
            val firstBuild = values[4]
            val secondBuild = values[5]

            val metric =
                Metric(entity, typeMetric, outcome, name, parseNumber(firstBuild)!!, parseNumber(secondBuild)!!)
            metrics.add(metric)
        }

        return metrics
    }

    private fun parseNumber(input: String): Number? {
        return input.toIntOrNull() ?: input.toLongOrNull() ?: input.toDoubleOrNull()
    }
}
