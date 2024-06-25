package io.github.cdsap.comparescans.model

class Metric(
    val entity: Entity,
    val type: TypeMetric,
    val subcategory: String,
    val name: String,
    val firstBuild: Number,
    val secondBuild: Number
)
