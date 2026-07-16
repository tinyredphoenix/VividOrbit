package com.custom.dth.data.epg

import android.content.Context
import android.media.tv.TvContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TvProviderEpgSource(private val context: Context) {

    /**
     * Fetches programs for a specific channel within a time window.
     */
    suspend fun getPrograms(channelId: Long, startTimeMillis: Long, endTimeMillis: Long): List<Program> = withContext(Dispatchers.IO) {
        val programs = mutableListOf<Program>()
        
        val uri = TvContract.buildProgramsUriForChannel(channelId, startTimeMillis, endTimeMillis)
        val projection = arrayOf(
            TvContract.Programs._ID,
            TvContract.Programs.COLUMN_TITLE,
            TvContract.Programs.COLUMN_SHORT_DESCRIPTION,
            TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS,
            TvContract.Programs.COLUMN_END_TIME_UTC_MILLIS
        )
        
        context.contentResolver.query(uri, projection, null, null, TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS + " ASC")?.use { cursor ->
            val idIndex = cursor.getColumnIndex(TvContract.Programs._ID)
            val titleIndex = cursor.getColumnIndex(TvContract.Programs.COLUMN_TITLE)
            val descIndex = cursor.getColumnIndex(TvContract.Programs.COLUMN_SHORT_DESCRIPTION)
            val startIndex = cursor.getColumnIndex(TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS)
            val endIndex = cursor.getColumnIndex(TvContract.Programs.COLUMN_END_TIME_UTC_MILLIS)
            
            while (cursor.moveToNext()) {
                programs.add(
                    Program(
                        id = cursor.getLong(idIndex),
                        channelId = channelId,
                        title = cursor.getString(titleIndex) ?: "Unknown Program",
                        description = cursor.getString(descIndex),
                        startTimeUtcMillis = cursor.getLong(startIndex),
                        endTimeUtcMillis = cursor.getLong(endIndex)
                    )
                )
            }
        }
        
        programs
    }

    /**
     * Gets the "Now" and "Next" programs for a channel banner.
     */
    suspend fun getNowAndNext(channelId: Long): Pair<Program?, Program?> {
        val now = System.currentTimeMillis()
        // Query next 12 hours to guarantee we find the current and next
        val programs = getPrograms(channelId, now, now + 12 * 60 * 60 * 1000L)
        
        val currentProgram = programs.firstOrNull { it.startTimeUtcMillis <= now && it.endTimeUtcMillis >= now }
        val nextProgram = programs.firstOrNull { it.startTimeUtcMillis > now }
        
        return Pair(currentProgram, nextProgram)
    }
}
