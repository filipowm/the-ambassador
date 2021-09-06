package com.filipowm.gitlab.api

import com.filipowm.gitlab.api.groups.GroupsApi
import com.filipowm.gitlab.api.project.ProjectsApi
import io.ktor.client.*

internal class GitLabApi(basePath: String, httpClient: HttpClient) : Api(basePath, httpClient), GitLab {
    override fun projects() = ProjectsApi("${basePath}/projects", client)

    override fun groups() = GroupsApi("${basePath}/groups", client)

    override fun issueStatistics() = IssueStatisticsApi("${basePath}/issues_statistics", client)
}