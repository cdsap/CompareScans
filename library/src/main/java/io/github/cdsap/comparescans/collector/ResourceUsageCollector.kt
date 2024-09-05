package io.github.cdsap.comparescans.collector

import io.github.cdsap.comparescans.model.Entity
import io.github.cdsap.comparescans.model.Measurement
import io.github.cdsap.comparescans.model.Metric
import io.github.cdsap.comparescans.model.TypeMetric
import io.github.cdsap.geapi.client.model.BuildWithResourceUsage
import io.github.cdsap.geapi.client.model.PerformanceMetrics

class ResourceUsageCollector {

    fun measurementsResourceUsage(
        resourceUsage: BuildWithResourceUsage,
        variant: String
    ): List<Measurement> {
        val metrics = mutableListOf<Measurement>()
        usageResourceMetrics(metrics, variant, resourceUsage.total, "total")
        usageResourceMetrics(metrics, variant, resourceUsage.execution, "execution")
        usageResourceMetrics(metrics, variant, resourceUsage.nonExecution, "non execution")
        return metrics
    }

    private fun usageResourceMetrics(
        metrics: MutableList<Measurement>,
        variant: String,
        performanceMetrics: PerformanceMetrics,
        subcategory: String
    ) {
        addPerformanceMetric(metrics, variant, "processes cpu all", subcategory, performanceMetrics.allProcessesCpu)
        addPerformanceMetric(
            metrics,
            variant,
            "processes memory all",
            subcategory,
            performanceMetrics.allProcessesMemory
        )
        addPerformanceMetric(
            metrics,
            variant,
            "cpu child processes",
            subcategory,
            performanceMetrics.buildChildProcessesCpu
        )
        addPerformanceMetric(
            metrics,
            variant,
            "memory child processes",
            subcategory,
            performanceMetrics.buildChildProcessesMemory
        )
        addPerformanceMetric(metrics, variant, "cpu build process", subcategory, performanceMetrics.buildProcessCpu)
        addPerformanceMetric(
            metrics,
            variant,
            "memory build process",
            subcategory,
            performanceMetrics.buildProcessMemory
        )
        addPerformanceMetric(
            metrics,
            variant,
            "disk read throughput",
            subcategory,
            performanceMetrics.diskReadThroughput
        )
        addPerformanceMetric(
            metrics,
            variant,
            "disk write throughput",
            subcategory,
            performanceMetrics.diskWriteThroughput
        )
        addPerformanceMetric(
            metrics,
            variant,
            "network download throughput",
            subcategory,
            performanceMetrics.networkDownloadThroughput
        )
        addPerformanceMetric(
            metrics,
            variant,
            "network upload throughput",
            subcategory,
            performanceMetrics.networkUploadThroughput
        )
    }

    private fun addPerformanceMetric(
        metrics: MutableList<Measurement>,
        variant: String,
        name: String,
        subcategory: String,
        metric: io.github.cdsap.geapi.client.model.Metric
    ) {
        addMetric(metrics, variant, TypeMetric.ResourceMax, name, subcategory, metric.max)
        addMetric(metrics, variant, TypeMetric.ResourceP25, name, subcategory, metric.p25)
        addMetric(metrics, variant, TypeMetric.ResourceP75, name, subcategory, metric.p75)
        addMetric(metrics, variant, TypeMetric.ResourceP95, name, subcategory, metric.p95)
        addMetric(metrics, variant, TypeMetric.ResourceAverage, name, subcategory, metric.average)
        addMetric(metrics, variant, TypeMetric.ResourceMedian, name, subcategory, metric.median)
    }

    private fun addMetric(
        metrics: MutableList<Measurement>,
        variant: String,
        typeMetric: TypeMetric,
        name: String,
        subcategory: String,
        value: Long
    ) {
        metrics.add(
            Measurement(
                metric = Metric(
                    entity = Entity.ResourceUsage,
                    type = typeMetric,
                    name = name,
                    subcategory = subcategory
                ),
                value = value,
                variant = variant
            )
        )
    }
}
