package io.github.cdsap.comparescans.model

data class Metric(
    val entity: Entity,
    val type: TypeMetric,
    val subcategory: String,
    val name: String
)
