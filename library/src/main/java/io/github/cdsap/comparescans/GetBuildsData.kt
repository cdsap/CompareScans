package io.github.cdsap.comparescans

import io.github.cdsap.geapi.client.domain.impl.GetSingleBuildCachePerformanceRequest
import io.github.cdsap.geapi.client.domain.impl.GetSingleBuildResourceUsageRequest
import io.github.cdsap.geapi.client.domain.impl.GetSingleBuildScanAttributesRequest
import io.github.cdsap.geapi.client.model.BuildWithResourceUsage
import io.github.cdsap.geapi.client.model.ClientType
import io.github.cdsap.geapi.client.repository.impl.GradleRepositoryImpl

class GetBuildsData(
    private val repository: GradleRepositoryImpl,
    private val buildScans: List<String>,
    clientType: ClientType = ClientType.API
) {
    private val logger = Logger(clientType)

    suspend fun get(): List<BuildWithResourceUsage> {
        val builds = mutableListOf<BuildWithResourceUsage>()

        for (buildScan in buildScans) {
            logger.log("getting attributes for build scan $buildScan")
            val buildScanAttributes = GetSingleBuildScanAttributesRequest(repository).get(buildScan)
            logger.log("getting cache performance for build scan $buildScan")
            val buildCachePerformance = GetSingleBuildCachePerformanceRequest(repository).get(buildScan)

            logger.log("getting usage resources for build scan $buildScan")
            val usageResources = try {
                GetSingleBuildResourceUsageRequest(repository).get(buildScan)
            } catch (e: Exception) {
                null
            }
            if (buildScanAttributes == null || buildCachePerformance == null) {
                throw IllegalArgumentException(
                    "Some of the expected data is null:\n" +
                        if (buildScanAttributes == null) {
                            "BuildScanAttributes for $buildScan is null\n"
                        } else {
                            "" +
                                if (buildCachePerformance == null) {
                                    "BuildCachePerformance for $buildScan is null\n"
                                } else {
                                    ""
                                }
                        }
                )
            }
            usageResources.let {
                it?.taskExecution = buildCachePerformance.taskExecution
                it?.requestedTask = buildScanAttributes.requestedTasksGoals
                it?.tags = buildScanAttributes.tags
                it?.values = buildScanAttributes.values
                it?.builtTool = buildScanAttributes.buildTool
                it?.buildDuration = buildScanAttributes.buildDuration
                it?.projectName = buildScanAttributes.projectName
                it?.id = buildScan
            }
            builds.add(usageResources!!)
        }
        return builds
    }
}
