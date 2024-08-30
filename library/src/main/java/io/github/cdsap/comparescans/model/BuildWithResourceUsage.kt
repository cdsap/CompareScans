package io.github.cdsap.comparescans.model

import io.github.cdsap.geapi.client.model.Build
import io.github.cdsap.geapi.client.model.PerformanceUsage

data class BuildWithResourceUsage(
    val build: Build,
    val usage: PerformanceUsage? = null
)
