package com.custom.dth.features.backup

import com.custom.dth.data.local.AppDatabase
import com.custom.dth.features.settings.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

/**
 * Handles exporting and importing the entire application state (Room + Prefs) to JSON.
 * We avoid Android TV Storage Access Framework issues by using standard app external files dir.
 */
class BackupManager(
    private val database: AppDatabase,
    private val appPreferences: AppPreferences
) {
    /**
     * Dumps all data to a single JSON string and writes to the provided file.
     * Note: In a real app, you would use Gson or Moshi to serialize lists of entities.
     * For this modular architecture proof, this is the stub that orchestrates it.
     */
    suspend fun exportBackup(destinationFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            // val metas = database.channelMetaDao().getAllChannelMeta().first()
            val prefsJson = appPreferences.exportToJson()
            
            // Construct a master JSON object
            val backupJson = """
                {
                    "settings": $prefsJson
                }
            """.trimIndent()
            
            FileWriter(destinationFile).use { writer ->
                writer.write(backupJson)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
