package com.custom.dth.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChannelRepository(
    private val systemSource: TvProviderChannelSource,
    private val channelMetaDao: ChannelMetaDao
) {
    /**
     * The grand join: combines system channels with our local ChannelMeta overrides.
     */
    fun observeChannels(): Flow<List<MergedChannel>> {
        return combine(
            systemSource.observeSystemChannels(),
            channelMetaDao.getAllChannelMeta()
        ) { systemChannels, localMetas ->
            val metaMap = localMetas.associateBy { it.stableKey }
            
            systemChannels.map { sys ->
                val meta = metaMap[sys.stableKey]
                MergedChannel(
                    id = sys.id,
                    stableKey = sys.stableKey,
                    displayName = meta?.customName ?: sys.systemName,
                    displayNumber = meta?.assignedNumber ?: sys.systemNumber,
                    isFavorite = meta?.isFavorite ?: false,
                    logoPath = meta?.localLogoPath
                )
            }.sortedBy { it.displayNumber.toIntOrNull() ?: Int.MAX_VALUE }
        }
    }

    /**
     * Swaps assigned numbers between two channels to prevent unique constraint violations.
     */
    suspend fun renumber(stableKey: String, newNumber: String) = withContext(Dispatchers.IO) {
        val existingWithNewNumber = channelMetaDao.getChannelMetaByNumber(newNumber)
        val currentMeta = channelMetaDao.getChannelMeta(stableKey) ?: ChannelMeta(
            stableKey = stableKey,
            assignedNumber = newNumber // Fallback if meta didn't exist
        )
        
        val oldNumber = currentMeta.assignedNumber
        
        if (existingWithNewNumber != null && existingWithNewNumber.stableKey != stableKey) {
            // Bi-directional swap: give the other channel our old number
            channelMetaDao.update(existingWithNewNumber.copy(assignedNumber = oldNumber))
        }
        
        // Save our new number
        channelMetaDao.insertOrUpdate(currentMeta.copy(assignedNumber = newNumber))
    }
    
    suspend fun updateFavorite(stableKey: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        val currentMeta = channelMetaDao.getChannelMeta(stableKey) ?: ChannelMeta(
            stableKey = stableKey,
            assignedNumber = ""
        )
        channelMetaDao.insertOrUpdate(currentMeta.copy(isFavorite = isFavorite))
    }

    suspend fun renameChannel(stableKey: String, customName: String) = withContext(Dispatchers.IO) {
        val currentMeta = channelMetaDao.getChannelMeta(stableKey) ?: ChannelMeta(
            stableKey = stableKey,
            assignedNumber = ""
        )
        channelMetaDao.insertOrUpdate(currentMeta.copy(customName = customName))
    }

    fun searchChannels(query: String): Flow<List<MergedChannel>> {
        val lowerQuery = query.lowercase()
        return observeChannels().combine(kotlinx.coroutines.flow.flowOf(lowerQuery)) { channels, searchStr ->
            if (searchStr.isBlank()) {
                channels
            } else {
                channels.filter { 
                    it.displayName.lowercase().contains(searchStr) || it.displayNumber.contains(searchStr) 
                }
            }
        }
    }
}

data class MergedChannel(
    val id: Long,
    val stableKey: String,
    val displayName: String,
    val displayNumber: String,
    val isFavorite: Boolean,
    val logoPath: String?
)
