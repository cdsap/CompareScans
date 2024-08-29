package io.github.cdsap.comparescans.model

data class Measurement(
    val variant: String,
    val metric: Metric,
    val value: Number
)
