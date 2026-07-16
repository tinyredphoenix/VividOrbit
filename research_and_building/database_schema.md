# VividOrbit — Database Schema (Room)

## Why a separate DB?

The core AOSP app stores channel data in Android's `TvProvider` (a system-level ContentProvider). Writing custom names, numbers, or groups to TvProvider columns would be overwritten by channel rescans. A parallel Room DB is the safe approach.

## Tables

### channel_meta
Maps core TvProvider channel ID to user-customizable metadata.

```sql
CREATE TABLE channel_meta (
    channel_id   INTEGER PRIMARY KEY,  -- matches TvContract.Channels._ID
    custom_name  TEXT,                  -- null = use broadcast display name
    custom_number TEXT,                 -- user-assigned channel number
    logo_path    TEXT,                  -- local file path to custom logo
    logo_source  INTEGER NOT NULL DEFAULT 0,  -- 0=auto, 1=manual, 2=default
    is_favorite  INTEGER NOT NULL DEFAULT 0,
    created_at   INTEGER NOT NULL,      -- epoch millis
    updated_at   INTEGER NOT NULL
);
```

### channel_groups
User-defined channel groups.

```sql
CREATE TABLE channel_groups (
    group_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    name           TEXT NOT NULL,
    sort_order     INTEGER NOT NULL DEFAULT 0,
    numbering_mode INTEGER NOT NULL DEFAULT 0,  -- 0=global, 1=group-specific
    created_at     INTEGER NOT NULL
);
```

### group_channels
Many-to-many join: channels within groups.

```sql
CREATE TABLE group_channels (
    group_id     INTEGER NOT NULL,
    channel_id   INTEGER NOT NULL,
    group_number TEXT,                   -- group-specific numbering (null = use global)
    sort_order   INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (group_id, channel_id),
    FOREIGN KEY (group_id) REFERENCES channel_groups(group_id) ON DELETE CASCADE
);
```

### watch_history
Recently watched channels (keep last 50).

```sql
CREATE TABLE watch_history (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    channel_id   INTEGER NOT NULL,
    watched_at   INTEGER NOT NULL,       -- epoch millis
    UNIQUE(channel_id)                   -- only latest entry per channel
);
```

### launcher_channels
Channels pushed to Android TV home screen (max 10).

```sql
CREATE TABLE launcher_channels (
    channel_id   INTEGER PRIMARY KEY,
    position     INTEGER NOT NULL,       -- 0-9
    added_at     INTEGER NOT NULL
);
```

### app_settings
Key-value settings store.

```sql
CREATE TABLE app_settings (
    key          TEXT PRIMARY KEY,
    value        TEXT NOT NULL
);
```

### logo_cache
Cached channel logo PNGs.

```sql
CREATE TABLE logo_cache (
    channel_id   INTEGER PRIMARY KEY,
    logo_blob    BLOB,                   -- PNG bytes
    fetched_at   INTEGER NOT NULL
);
```

## Room DAOs

### ChannelMetaDao
```kotlin
@Dao
interface ChannelMetaDao {
    @Query("SELECT * FROM channel_meta ORDER BY custom_number ASC")
    fun getAll(): Flow<List<ChannelMeta>>

    @Query("SELECT * FROM channel_meta WHERE channel_id = :channelId")
    suspend fun get(channelId: Long): ChannelMeta?

    @Query("SELECT * FROM channel_meta WHERE is_favorite = 1 ORDER BY custom_number ASC")
    fun getFavorites(): Flow<List<ChannelMeta>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(meta: ChannelMeta)

    @Query("UPDATE channel_meta SET custom_number = :number, updated_at = :now WHERE channel_id = :channelId")
    suspend fun setCustomNumber(channelId: Long, number: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE channel_meta SET custom_name = :name, updated_at = :now WHERE channel_id = :channelId")
    suspend fun setCustomName(channelId: Long, name: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE channel_meta SET is_favorite = :fav, updated_at = :now WHERE channel_id = :channelId")
    suspend fun setFavorite(channelId: Long, fav: Boolean, now: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM channel_meta WHERE custom_number = :number AND channel_id != :excludeId")
    suspend fun countByCustomNumber(number: String, excludeId: Long): Int
}
```

### ChannelGroupDao
```kotlin
@Dao
interface ChannelGroupDao {
    @Query("SELECT * FROM channel_groups ORDER BY sort_order ASC")
    fun getAll(): Flow<List<ChannelGroup>>

    @Insert
    suspend fun insert(group: ChannelGroup): Long

    @Update
    suspend fun update(group: ChannelGroup)

    @Delete
    suspend fun delete(group: ChannelGroup)

    @Transaction
    @Query("SELECT * FROM channel_groups WHERE group_id = :groupId")
    fun getWithChannels(groupId: Long): Flow<GroupWithChannels?>
}
```

### GroupChannelDao
```kotlin
@Dao
interface GroupChannelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: GroupChannel)

    @Delete
    suspend fun delete(entry: GroupChannel)

    @Query("DELETE FROM group_channels WHERE group_id = :groupId")
    suspend fun deleteByGroup(groupId: Long)

    @Query("SELECT channel_id FROM group_channels WHERE group_id = :groupId ORDER BY sort_order ASC")
    suspend fun getChannelIds(groupId: Long): List<Long>
}
```

### WatchHistoryDao
```kotlin
@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY watched_at DESC LIMIT 50")
    fun getAll(): Flow<List<WatchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: WatchHistory)

    @Query("DELETE FROM watch_history WHERE id NOT IN (SELECT id FROM watch_history ORDER BY watched_at DESC LIMIT 50)")
    suspend fun trimToLimit()
}
```

### LauncherChannelDao
```kotlin
@Dao
interface LauncherChannelDao {
    @Query("SELECT * FROM launcher_channels ORDER BY position ASC LIMIT 10")
    fun getAll(): Flow<List<LauncherChannel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(channel: LauncherChannel)

    @Query("DELETE FROM launcher_channels WHERE channel_id = :channelId")
    suspend fun delete(channelId: Long)

    @Query("SELECT COUNT(*) FROM launcher_channels")
    suspend fun count(): Int
}
```

## Entity Classes

```kotlin
@Entity(tableName = "channel_meta")
data class ChannelMeta(
    @PrimaryKey val channel_id: Long,
    val custom_name: String? = null,
    val custom_number: String? = null,
    val logo_path: String? = null,
    val logo_source: Int = 0,
    val is_favorite: Boolean = false,
    val created_at: Long = System.currentTimeMillis(),
    val updated_at: Long = System.currentTimeMillis()
)

@Entity(tableName = "channel_groups")
data class ChannelGroup(
    @PrimaryKey(autoGenerate = true) val group_id: Long = 0,
    val name: String,
    val sort_order: Int = 0,
    val numbering_mode: Int = 0,
    val created_at: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "group_channels",
    primaryKeys = ["group_id", "channel_id"],
    foreignKeys = [ForeignKey(
        entity = ChannelGroup::class,
        parentColumns = ["group_id"],
        childColumns = ["group_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class GroupChannel(
    val group_id: Long,
    val channel_id: Long,
    val group_number: String? = null,
    val sort_order: Int = 0
)

@Entity(tableName = "watch_history")
data class WatchHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channel_id: Long,
    val watched_at: Long = System.currentTimeMillis()
)

@Entity(tableName = "launcher_channels")
data class LauncherChannel(
    @PrimaryKey val channel_id: Long,
    val position: Int,
    val added_at: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "logo_cache")
data class LogoCache(
    @PrimaryKey val channel_id: Long,
    val logo_blob: ByteArray? = null,
    val fetched_at: Long = System.currentTimeMillis()
)
```

## Relations

```kotlin
data class GroupWithChannels(
    @Embedded val group: ChannelGroup,
    @Relation(
        parentColumn = "group_id",
        entityColumn = "channel_id",
        associateBy = Junction(GroupChannel::class)
    )
    val channels: List<ChannelMeta>
)
```

## Database Class

```kotlin
@Database(
    entities = [
        ChannelMeta::class,
        ChannelGroup::class,
        GroupChannel::class,
        WatchHistory::class,
        LauncherChannel::class,
        AppSetting::class,
        LogoCache::class
    ],
    version = 1,
    exportSchema = true
)
abstract class VividOrbitDatabase : RoomDatabase() {
    abstract fun channelMetaDao(): ChannelMetaDao
    abstract fun channelGroupDao(): ChannelGroupDao
    abstract fun groupChannelDao(): GroupChannelDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun launcherChannelDao(): LauncherChannelDao
}
```