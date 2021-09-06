package com.filipowm.ambassador.project.indexer

import com.filipowm.ambassador.ConcurrencyProvider
import com.filipowm.ambassador.configuration.source.ProjectSources
import com.filipowm.ambassador.configuration.source.ProjectSourcesProperties
import com.filipowm.ambassador.extensions.LoggerDelegate
import com.filipowm.ambassador.model.Project
import com.filipowm.ambassador.model.criteria.IndexingCriteria
import com.filipowm.ambassador.model.source.ProjectSource
import com.filipowm.ambassador.storage.ProjectEntityRepository
import org.springframework.stereotype.Component

@Component
internal class ProjectIndexingService(
    private val sources: ProjectSources,
    private val projectEntityRepository: ProjectEntityRepository,
    private val concurrencyProvider: ConcurrencyProvider,
    private val projectSourceProperties: ProjectSourcesProperties,

    ) {
    private val indexingLock: IndexingLock = InMemoryIndexinglock()

    @Volatile
    private var currentIndexerUsed: ProjectIndexer? = null

    companion object {
        private val log by LoggerDelegate()
    }

    suspend fun forciblyStop() {
        log.info("Trying to forcibly stop indexing, if active")
        if (indexingLock.isLocked() && currentIndexerUsed != null) {
            currentIndexerUsed!!.forciblyStop()
            log.warn("Indexing forcibly stopped!")
        } else {
            log.warn("No indexing in progress, nothing to stop")
        }
    }

    suspend fun reindex() {
        log.info("Starting indexing all projects within source repository")
        if (indexingLock.tryLock()) {
            val indexer = createIndexer()
            currentIndexerUsed = indexer
            val stats = Statistics()
            return indexer.indexAll(
                onStarted = { stats.startTiming() },
                onProjectIndexingStarted = { stats.recordStarted() },
                onProjectIndexingFinished = { stats.recordFinished() },
                onProjectIndexingError = { t, _ -> stats.recordError(t) },
                onFinished = {
                    currentIndexerUsed = null
                    indexingLock.unlock()
                    stats.stopTiming()
                    log.warn("Indexing has finished")
                    log.warn("Report:\n{}", stats.getReport())
                }
            )
        } else {
            log.warn("Unable to trigger new indexing, cause indexing is already in progress and locked.")
            throw IndexingAlreadyStartedException("Unable to start new projects indexing, because it is already running")
        }
    }

    suspend fun reindex(id: Long): Project? {
        val indexer = createIndexer()
        return indexer.indexOne(id)
    }

    private fun createIndexer(): ProjectIndexer {
        val source = sources.get("gitlab").get() as ProjectSource<Any>
        val indexer = CoreProjectIndexer(
            source,
            projectEntityRepository,
            concurrencyProvider,
            projectSourceProperties.indexEvery,
            IndexingCriteria(source.getInvalidProjectCriterions().hasRepositorySetUp())
        )
        log.info("Using '{}' for indexing projects", indexer.javaClass.canonicalName)
        return indexer
    }
}