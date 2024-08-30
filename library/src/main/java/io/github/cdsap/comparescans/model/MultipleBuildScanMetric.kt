package io.github.cdsap.comparescans.model

data class MultipleBuildScanMetric(
    val metric: Metric,
    val values: Map<String, Number>
)
