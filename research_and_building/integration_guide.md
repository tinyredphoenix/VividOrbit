# VividOrbit — UI Integration Guide

## How the custom UI will connect to the core

### 1. Module Setup

The custom UI will be a new `app/` module that depends on `core-app/`:

```gradle
// app/build.gradle
dependencies {
    implementation project(':core-app')
    implementation 'androidx.room:room-runtime:2.6.1'
    implementation 'androidx.room:room-ktx:2.6.1'
    kapt 'androidx.room:room-compiler:2.6.1'
    implementation 'com.google.dagger:hilt-android:2.51'
    kapt 'com.google.dagger:hilt-compiler:2.51'
    // Compose for TV
    implementation 'androidx.tv:tv-material:1.0.0'
}
```

```gradle
// settings.gradle
include ':core-app'
include ':app'
```

### 2. Application Class

```kotlin
@HiltAndroidApp
class VividOrbitApplication : Application() {
    // Core's LiveTvApplication is the manifest application class.
    // We extend it or replace it.
    // The core's TvInputService is registered in core-app's manifest.
}
```

### 3. Key Integration Patterns

#### 3.1 Getting the Channel List

```kotlin
@Singleton
class ChannelRepository @Inject constructor(
    private val channelDataManager: ChannelDataManager,  // from core
    private val channelMetaDao: ChannelMetaDao,           // custom Room DB
    private val programDataManager: ProgramDataManager    // from core
) {
    fun getChannelList(): Flow<List<ChannelWithMeta>> {
        // Combine core channel data with custom metadata
        return channelMetaDao.getAll().map { metas ->
            val metaMap = metas.associateBy { it.channel_id }
            channelDataManager.channelList.map { channel ->
                val meta = metaMap[channel.id]
                ChannelWithMeta(
                    channel = channel,
                    customName = meta?.custom_name,
                    customNumber = meta?.custom_number ?: channel.displayNumber,
                    isFavorite = meta?.is_favorite ?: false,
                    currentProgram = getCurrentProgramSafe(channel.id)
                )
            }
        }
    }

    private fun getCurrentProgramSafe(channelId: Long): Program? {
        return try {
            programDataManager.getCurrentProgram(channelId)
        } catch (e: Exception) {
            null  // Never crash
        }
    }
}

data class ChannelWithMeta(
    val channel: Channel,
    val customName: String?,
    val customNumber: String,
    val isFavorite: Boolean,
    val currentProgram: Program?
)
```

#### 3.2 Tuning to a Channel

```kotlin
class PlaybackViewModel @Inject constructor(
    private val channelTuner: ChannelTuner,
    private val tvInputManagerHelper: TvInputManagerHelper
) : ViewModel() {

    fun tuneToChannel(channelId: Long) {
        val channel = channelTuner.getChannel(channelId) ?: run {
            Log.w(TAG, "Channel not found: $channelId")
            return  // Never crash
        }
        val inputId = channel.inputId
        val channelUri = TvContract.buildChannelUri(channelId)
        // TvView handles the actual tuning
        _tuneEvent.value = TuneEvent(inputId, channelUri)
    }
}
```

#### 3.3 EPG Data

```kotlin
class EpgRepository @Inject constructor(
    private val programDataManager: ProgramDataManager
) {
    fun getPrograms(channelId: Long, startTimeMs: Long, endTimeMs: Long): List<Program> {
        return try {
            programDataManager.getPrograms(channelId, startTimeMs)
                ?.filter { it.startTimeUtcMillis in startTimeMs..endTimeMs }
                ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get programs", e)
            emptyList()
        }
    }
}
```

#### 3.4 Signal Strength

```kotlin
class SignalStrengthManager @Inject constructor() {
    private var tunerHal: DvbTunerHal? = null

    fun getSignalStrength(): Int {
        return try {
            tunerHal?.getSignalStrength() ?: TvInputConstantCompat.SIGNAL_STRENGTH_NOT_USED
        } catch (e: Exception) {
            TvInputConstantCompat.SIGNAL_STRENGTH_ERROR
        }
    }
}
```

#### 3.5 Channel Navigation

```kotlin
fun channelUp() {
    try {
        channelTuner.moveToAdjacentBrowsableChannel(true)
    } catch (e: Exception) {
        Log.e(TAG, "Channel up failed", e)
    }
}

fun channelDown() {
    try {
        channelTuner.moveToAdjacentBrowsableChannel(false)
    } catch (e: Exception) {
        Log.e(TAG, "Channel down failed", e)
    }
}
```

#### 3.6 Watch History Recording

```kotlin
// In PlaybackViewModel, when a channel is successfully tuned:
fun onChannelTuned(channelId: Long) {
    viewModelScope.launch {
        try {
            watchHistoryDao.upsert(WatchHistory(channel_id = channelId))
            watchHistoryDao.trimToLimit()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to record watch history", e)
        }
    }
}
```

### 4. Error Handling Checklist

Every integration point must follow this pattern:

```kotlin
fun safeOperation(): Result<T> {
    return try {
        val result = coreApi.someMethod()
        if (result == null) {
            Result.failure(NotFoundException("Resource not found"))
        } else {
            Result.success(result)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Operation failed", e)
        Result.failure(e)
    }
}
```

**No exceptions should propagate to the UI thread uncaught.**

### 5. Manifest

The custom UI adds its own activities to the merged manifest:

```xml
<!-- app/src/main/AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application>
        <!-- Main launcher activity -->
        <activity android:name=".ui.MainActivity"
            android:exported="true"
            android:launchMode="singleTask">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
                <category android:name="android.intent.category.LEANBACK_LAUNCHER"/>
            </intent-filter>
        </activity>

        <!-- Playback activity -->
        <activity android:name=".ui.playback.PlaybackActivity"
            android:exported="false"
            android:launchMode="singleTask"
            android:screenOrientation="landscape"/>

        <!-- Settings activity -->
        <activity android:name=".ui.settings.SettingsActivity"
            android:exported="false"/>
    </application>
</manifest>
```

### 6. Testing Without Hardware

During development, use `FileTunerHal` from the tuner tests to simulate a tuner:

```kotlin
// In debug builds, inject a file-based tuner instead of DvbTunerHal
@Module
@InstallIn(SingletonComponent::class)
object DebugTunerModule {
    @Provides
    fun provideTunerHal(): TunerHal {
        return FileTunerHal(context, testTsFile)
    }
}
```

### 7. Key Files to Read in Core

| File | Purpose |
|------|---------|
| `ChannelDataManager.java` | Channel CRUD, listeners |
| `ProgramDataManager.java` | EPG data, prefetch, current program |
| `ChannelTuner.java` | Channel navigation, current channel |
| `TvInputManagerHelper.java` | Input discovery, labels |
| `TvSettings.java` | Settings persistence |
| `TunerHal.java` | Hardware tuning, signal strength |
| `BaseTunerTvInputService.java` | TvInputService lifecycle |
| `TunerSessionWorker.java` | Playback engine |
| `EpgFetchHelper.java` | EPG data → TvProvider |

### 8. Building

```bash
# Build the entire project
cd VividOrbit
./gradlew :app:assembleDebug

# Core app builds as android_library
# App module depends on it
# Single APK output
```