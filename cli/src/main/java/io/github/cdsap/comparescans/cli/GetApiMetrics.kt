package io.github.cdsap.comparescans.cli

import io.github.cdsap.comparescans.GetBuildsData
import io.github.cdsap.comparescans.MultipleScanMetrics
import io.github.cdsap.comparescans.model.MultipleBuildScanMetric
import io.github.cdsap.geapi.client.model.ClientType
import io.github.cdsap.geapi.client.network.GEClient
import io.github.cdsap.geapi.client.repository.impl.GradleRepositoryImpl

class GetApiMetrics(
    private val apiKey: String,
    private val url: String,
    private val scans: List<String>
) : GetMetrics {
    override suspend fun getMetrics(): List<MultipleBuildScanMetric> {
        val repository = GradleRepositoryImpl(GEClient(apiKey, url))
        val data = GetBuildsData(repository, scans, ClientType.CLI).get()
        return MultipleScanMetrics(data).get()
    }
}
