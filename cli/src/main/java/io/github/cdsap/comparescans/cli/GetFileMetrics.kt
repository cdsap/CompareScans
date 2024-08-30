package io.github.cdsap.comparescans.cli

import io.github.cdsap.comparescans.model.Entity
import io.github.cdsap.comparescans.model.Metric
import io.github.cdsap.comparescans.model.MultipleBuildScanMetric
import io.github.cdsap.comparescans.model.TypeMetric
import java.io.File

class GetFileMetrics(private val file: File) : GetMetrics {
    override suspend fun getMetrics(): List<MultipleBuildScanMetric> {
        println("Processing existing metrics from ${file.name}")

        val metrics = mutableListOf<MultipleBuildScanMetric>()
        val lines = file.readLines()

        if (lines.isEmpty()) {
            throw IllegalArgumentException("The file is empty")
        }

        // Read the header to get build IDs
        val headers = lines.first().split(",")
        val buildIds = headers.drop(4) // Skip the first 4 columns: entity, name, category, type

        for (line in lines.drop(1)) {
            val values = line.split(",")

            val entity = Entity.valueOf(values[0])
            val name = values[1]
            val category = values[2]
            val typeMetric = TypeMetric.valueOf(values[3])

            val metricValues = buildIds.mapIndexed { index, buildId ->
                buildId to parseNumber(values[4 + index])!!
            }.toMap()

            val metric = MultipleBuildScanMetric(Metric(entity, typeMetric, category, name), metricValues)
            metrics.add(metric)
        }

        return metrics
    }

    private fun parseNumber(input: String): Number? {
        return input.toIntOrNull() ?: input.toLongOrNull() ?: input.toDoubleOrNull()
    }
}
