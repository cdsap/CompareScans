package io.github.cdsap.comparescans

import com.google.gson.Gson
import io.github.cdsap.geapi.client.domain.impl.GetSingleBuildCachePerformanceRequest
import io.github.cdsap.geapi.client.domain.impl.GetSingleBuildScanAttributesRequest
import io.github.cdsap.geapi.client.model.Build
import io.github.cdsap.geapi.client.model.ClientType
import io.github.cdsap.geapi.client.repository.impl.GradleRepositoryImpl
import java.io.File

class GetBuildsData(
    private val repository: GradleRepositoryImpl,
    private val firstBuildScan: String,
    private val secondBuildScan: String,
    clientType: ClientType = ClientType.API
) {
    private val logger = Logger(clientType)

    suspend fun get(): Pair<Build, Build> {
        logger.log("getting attributes first build scan $firstBuildScan")
        val firstBuildScanAttributes = GetSingleBuildScanAttributesRequest(repository).get(firstBuildScan)
        logger.log("getting attributes second build scan $secondBuildScan")
        val secondBuildScanAttributes = GetSingleBuildScanAttributesRequest(repository).get(secondBuildScan)
        logger.log("getting cache performance first build scan $firstBuildScan")
        val firstBuildCachePerformance = GetSingleBuildCachePerformanceRequest(repository).get(firstBuildScan)
        File("text1.json").writeText(Gson().toJson(firstBuildCachePerformance))

        logger.log("getting cache performance second build scan $secondBuildScan")
        val secondBuildCachePerformance = GetSingleBuildCachePerformanceRequest(repository).get(secondBuildScan)

        if (firstBuildScanAttributes == null || secondBuildScanAttributes == null || firstBuildCachePerformance == null || secondBuildCachePerformance == null) {
            throw IllegalArgumentException(
                "Some of the expected data is null:\n" +
                    if (firstBuildScanAttributes == null) {
                        "BuildScanAttributes for $firstBuildScan is null\n"
                    } else {
                        "" +
                            if (secondBuildScanAttributes == null) {
                                "BuildScanAttributes for $secondBuildScan is null\n"
                            } else {
                                "" +
                                    if (firstBuildCachePerformance == null) {
                                        "BuildCachePerformance for $firstBuildScan is null\n"
                                    } else {
                                        "" +
                                            if (secondBuildCachePerformance == null) "BuildCachePerformance for $secondBuildScan is null\n" else ""
                                    }
                            }
                    }
            )
        }
        return firstBuildCachePerformance to secondBuildCachePerformance
    }
}
