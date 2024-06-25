package io.github.cdsap.comparescans.cli.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.cdsap.comparescans.model.Entity
import io.github.cdsap.comparescans.model.TypeMetric

data class RuleYaml(
    @JsonProperty("entity") val entity: Entity,
    @JsonProperty("type") val type: TypeMetric,
    @JsonProperty("threshold") val threshold: Long = -1L,
    @JsonProperty("value") val value: Int = -1
)

data class RulesYaml(
    @JsonProperty("rules") val rules: List<RuleYaml>
)
