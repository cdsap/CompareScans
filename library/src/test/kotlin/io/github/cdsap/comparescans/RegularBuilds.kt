package io.github.cdsap.comparescans

import com.google.gson.Gson
import io.github.cdsap.geapi.client.model.Build

class RegularBuilds {
    fun returnBuilds(): Pair<Build, Build> {
        val classLoader = this.javaClass.classLoader
        val buildA = classLoader.getResource("buildA.json")
        val buildB = classLoader.getResource("buildB.json")
        return Gson().fromJson(buildA.readText(), Build::class.java) to Gson().fromJson(
            buildB.readText(),
            Build::class.java
        )
    }

    fun returnSimpleBuilds(): Pair<Build, Build> {
        val classLoader = this.javaClass.classLoader
        val buildA = classLoader.getResource("simpleBuildA.json")
        val buildB = classLoader.getResource("simpleBuildA.json")
        return Gson().fromJson(buildA.readText(), Build::class.java) to Gson().fromJson(
            buildB.readText(),
            Build::class.java
        )
    }
}
