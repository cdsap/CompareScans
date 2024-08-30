package io.github.cdsap.comparescans

import com.google.gson.Gson
import io.github.cdsap.comparescans.model.BuildWithResourceUsage

class MockBuilds {
    fun returnBuilds(): Pair<BuildWithResourceUsage, BuildWithResourceUsage> {
        val classLoader = this.javaClass.classLoader
        val buildA = classLoader.getResource("no_usage_build_a.json")
        val buildB = classLoader.getResource("no_usage_build_a.json")
        return Gson().fromJson(buildA.readText(), BuildWithResourceUsage::class.java) to Gson().fromJson(
            buildB.readText(),
            BuildWithResourceUsage::class.java
        )
    }

    fun returnSimpleBuilds(): BuildWithResourceUsage {
        val classLoader = this.javaClass.classLoader
        val buildA = classLoader.getResource("no_usage_build_a.json")
        return Gson().fromJson(buildA.readText(), BuildWithResourceUsage::class.java)
    }

    fun returnBuildsWithUsage(): Pair<BuildWithResourceUsage, BuildWithResourceUsage> {
        val classLoader = this.javaClass.classLoader
        val buildA = classLoader.getResource("usage_build_a.json")
        val buildB = classLoader.getResource("usage_build_a.json")
        return Gson().fromJson(buildA.readText(), BuildWithResourceUsage::class.java) to Gson().fromJson(
            buildB.readText(),
            BuildWithResourceUsage::class.java
        )
    }
}
