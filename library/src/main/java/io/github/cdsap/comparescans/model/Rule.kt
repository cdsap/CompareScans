package io.github.cdsap.comparescans.model

data class Rule(
    val entity: Entity,
    val type: TypeMetric,
    val threshold: Long? = null,
    val value: Int? = null
)
