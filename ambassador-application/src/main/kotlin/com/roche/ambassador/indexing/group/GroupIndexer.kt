package com.roche.ambassador.indexing.group

import com.roche.ambassador.configuration.properties.IndexerProperties
import com.roche.ambassador.extensions.LoggerDelegate
import com.roche.ambassador.indexing.*
import com.roche.ambassador.indexing.IndexingForciblyStoppedException
import com.roche.ambassador.model.group.Group
import com.roche.ambassador.model.group.GroupFilter
import com.roche.ambassador.model.score.Scores
import com.roche.ambassador.model.source.ProjectSource
import com.roche.ambassador.model.stats.Statistics
import com.roche.ambassador.storage.group.GroupEntity
import com.roche.ambassador.storage.group.GroupEntityRepository
import com.roche.ambassador.storage.project.ProjectEntityRepository
import com.roche.ambassador.storage.project.ProjectGroupProjection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class GroupIndexer(
    private val source: ProjectSource,
    private val projectEntityRepository: ProjectEntityRepository,
    private val groupEntityRepository: GroupEntityRepository,
    private val coroutineScope: CoroutineScope,
    private val indexerProperties: IndexerProperties
) : Indexer<Group, Long, GroupFilter> {

    companion object {
        private val log by LoggerDelegate()
    }

    fun indexAll() {
        val projectGroups = projectEntityRepository.getProjectsAggregatedByGroup()
            .map { it.getGroupId() to it }
            .toMap()

        coroutineScope.launch {
            supervisorScope {
                source.flowGroups(GroupFilter(visibility = indexerProperties.criteria.projects.maxVisibility))
                    .filter { projectGroups.containsKey(it.id) }
                    .map { projectGroups[it.id]!! to it }
                    .collect {
                        val fromProjects = it.first
                        val fromSource = it.second
                        log.info("Indexing group {} (id={})", it.second.name, it.second.id)
                        val stats = createStats(fromProjects, fromSource)
                        val group = Group(
                            fromSource.id, fromSource.name, fromSource.fullName, fromSource.description,
                            fromSource.url, fromSource.avatarUrl, fromSource.visibility, fromSource.createdDate,
                            fromProjects.getType(), fromSource.parentId, stats, Scores(fromProjects.getActivity(), fromProjects.getCriticality(), fromProjects.getScore())
                        )
                        val entity = GroupEntity(
                            group.id, group.name, group.fullName, group, fromProjects.getScore(),
                            fromProjects.getActivity(), fromProjects.getCriticality(), stats.stars ?: 0
                        )
                        groupEntityRepository.save(entity)
                        log.info("Indexed group {} (id={})", it.second.name, it.second.id)
                    }
            }
        }
    }

    private fun createStats(fromProjects: ProjectGroupProjection, fromSource: Group): Statistics {
        val forks = fromProjects.getForks()
        val stars = fromProjects.getStars()
        val sourceStats = fromSource.stats
        return if (sourceStats != null) {
            Statistics(
                forks, stars,
                sourceStats.commits, sourceStats.jobArtifactsSize,
                sourceStats.lfsObjectsSize, sourceStats.packagesSize,
                sourceStats.repositorySize, sourceStats.storageSize,
                sourceStats.wikiSize
            )
        } else {
            Statistics(forks, stars)
        }
    }

    override suspend fun indexOne(id: Long): Group {
        TODO("Not yet implemented")
    }

    override suspend fun indexAll(
        filter: GroupFilter, onStarted: IndexingStartedCallback, onFinished: IndexingFinishedCallback, onError: IndexingErrorCallback,
        onObjectIndexingStarted: ObjectIndexingStartedCallback<Group>, onObjectExcludedByCriteria: ObjectExcludedByCriteriaCallback<Group>,
        onObjectIndexingError: ObjectIndexingErrorCallback<Group>, onObjectIndexingFinished: ObjectIndexingFinishedCallback<Group>
    ) {
        TODO("Not yet implemented")
    }

    override fun forciblyStop(terminateImmediately: Boolean) {
        val cause = IndexingForciblyStoppedException("Indexing forcibly stopped on demand")
        coroutineScope.cancel("Forcibly terminated all indexing in progress", cause)
    }
}