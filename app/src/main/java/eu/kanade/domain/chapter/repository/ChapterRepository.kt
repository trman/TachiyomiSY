package eu.kanade.domain.chapter.repository

import eu.kanade.domain.chapter.model.Chapter
import eu.kanade.domain.chapter.model.ChapterUpdate
import kotlinx.coroutines.flow.Flow

interface ChapterRepository {

    suspend fun addAll(chapters: List<Chapter>): List<Chapter>

    suspend fun update(chapterUpdate: ChapterUpdate)

    suspend fun updateAll(chapterUpdates: List<ChapterUpdate>)

    suspend fun removeChaptersWithIds(chapterIds: List<Long>)

    suspend fun getChapterByMangaId(mangaId: Long): List<Chapter>

    suspend fun getChapterByMangaIdAsFlow(mangaId: Long): Flow<List<Chapter>>

    // SY -->
    suspend fun getMergedChapterByMangaId(mangaId: Long): List<Chapter>

    suspend fun getMergedChapterByMangaIdAsFlow(mangaId: Long): Flow<List<Chapter>>
    // SY <--
}
