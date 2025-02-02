package eu.kanade.domain.track.repository

import eu.kanade.domain.track.model.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {

    suspend fun getTracksByMangaId(mangaId: Long): List<Track>

    suspend fun subscribeTracksByMangaId(mangaId: Long): Flow<List<Track>>

    suspend fun delete(mangaId: Long, syncId: Long)

    suspend fun insert(track: Track)

    suspend fun insertAll(tracks: List<Track>)
}
