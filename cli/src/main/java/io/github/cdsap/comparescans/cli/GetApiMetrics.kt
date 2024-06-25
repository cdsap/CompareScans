package io.github.cdsap.comparescans.cli

import io.github.cdsap.comparescans.GetBuildsData
import io.github.cdsap.comparescans.Metrics
import io.github.cdsap.comparescans.model.Metric
import io.github.cdsap.geapi.client.model.ClientType
import io.github.cdsap.geapi.client.network.GEClient
import io.github.cdsap.geapi.client.repository.impl.GradleRepositoryImpl

class GetApiMetrics(
    private val apiKey: String,
    private val url: String,
    private val firstBuildScan: String,
    private val secondBuildScan: String
) : GetMetrics {
    override suspend fun getMetrics(): List<Metric> {
        println("Processing metrics from API: Build scans $firstBuildScan-$secondBuildScan")
        val repository = GradleRepositoryImpl(GEClient(apiKey, url))
        val data = GetBuildsData(repository, firstBuildScan, secondBuildScan, ClientType.CLI).get()
        return Metrics(data.first, data.second).get()
    }
}
