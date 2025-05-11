package com.droidknights.app.core.data.repository

import com.droidknights.app.core.data.api.GithubRawApi
import com.droidknights.app.core.data.mapper.toData
import com.droidknights.app.core.data.repository.api.SessionRepository
import com.droidknights.app.core.datastore.datasource.SessionPreferencesDataSource
import com.droidknights.app.core.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import javax.inject.Inject

internal class DefaultSessionRepository @Inject constructor(
    private val githubRawApi: GithubRawApi,
    private val sessionDataSource: SessionPreferencesDataSource
) : SessionRepository {

    private var cachedSessions: List<Session> = emptyList()

    private val bookmarkIds: Flow<Set<String>> = sessionDataSource.bookmarkedSession

    override suspend fun getSessions(): List<Session> {
        return githubRawApi.getSessions()
            .map { it.toData() }
            .also { cachedSessions = it }
    }

    override suspend fun getSession(sessionId: String): Session {
        val cachedSession = cachedSessions.find { it.id == sessionId }
        if (cachedSession != null) {
            return cachedSession
        }

        return getSessions().find { it.id == sessionId }
            ?: error("Session not found with id: $sessionId")
    }

    override fun getBookmarkedSessionIds(): Flow<Set<String>> {
        return bookmarkIds.filterNotNull()
    }

    override suspend fun bookmarkSession(sessionId: String, bookmark: Boolean) {
        val currentBookmarkedSessionIds = bookmarkIds.first()
        sessionDataSource.updateBookmarkedSession(
            if (bookmark) {
                currentBookmarkedSessionIds + sessionId
            } else {
                currentBookmarkedSessionIds - sessionId
            }
        )
    }

    override suspend fun deleteBookmarkedSessions(sessionIds: Set<String>) {
        val currentBookmarkedSessionIds = bookmarkIds.first()
        sessionDataSource.updateBookmarkedSession(
            currentBookmarkedSessionIds - sessionIds
        )
    }
}
