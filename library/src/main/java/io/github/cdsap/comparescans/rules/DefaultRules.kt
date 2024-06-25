package io.github.cdsap.comparescans.rules

import io.github.cdsap.comparescans.model.Entity
import io.github.cdsap.comparescans.model.Rule
import io.github.cdsap.comparescans.model.TypeMetric

class DefaultRules {

    fun get(): List<Rule> {
        return listOf(
            Rule(
                entity = Entity.Task,
                type = TypeMetric.Duration,
                threshold = 5000L,
                value = 10
            ),
            Rule(
                entity = Entity.TaskType,
                type = TypeMetric.Duration,
                threshold = 5000L,
                value = 10
            ),
            Rule(
                entity = Entity.Module,
                type = TypeMetric.Duration,
                threshold = 10000L,
                value = 20
            ),
            Rule(
                entity = Entity.Project,
                type = TypeMetric.Counter
            ),
            Rule(
                entity = Entity.TaskType,
                type = TypeMetric.Counter
            )
        )
    }
}
