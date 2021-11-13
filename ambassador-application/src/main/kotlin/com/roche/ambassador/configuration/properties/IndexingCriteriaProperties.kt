package com.roche.ambassador.configuration.properties

import com.roche.ambassador.model.project.Visibility
import java.time.Duration
import javax.validation.constraints.Min

data class IndexingCriteriaProperties(
    val forks: Forks = Forks(),
    val projects: Projects = Projects(),
    val personalProjects: PersonalProjects = PersonalProjects()
) {
    data class Forks(
        val excludeAll: Boolean = true,
        val lastActivityNotLaterThan: Duration? = null
    )

    data class Projects(
        val includeArchived: Boolean = false,
        val maxVisibility: Visibility = Visibility.INTERNAL,
        val lastActivityWithin: Duration? = Duration.ofDays(365),
        val mustHaveDefaultBranch: Boolean = true,
        val mustHaveNotEmptyRepo: Boolean = true,
        val mustBeAbleToCreateMergeRequest: Boolean = true,
        val mustBeAbleToFork: Boolean = true
    )

    data class PersonalProjects(
        val excludeAll: Boolean = false,
        @Min(0) val mustHaveAtLeastStars: Int = 0
    )
}
