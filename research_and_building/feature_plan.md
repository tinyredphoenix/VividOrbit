# VividOrbit — Feature Plan

## 1. Channel Management

### 1.1 Auto-load Channels from Hardware DTV Broadcast
- **Status:** Core already handles this via `BaseTunerSetupActivity` → `ScanFragment` → `ChannelDataManager` (tuner).
- **On app start:** Check `ChannelDataManager.getChannelCount()`. If 0, launch setup wizard.
- **After scan:** Populate `channel_meta` table with default entries.
- **Default numbering:** Use virtual major/minor from `TunerChannel`.

### 1.2 Unique Channel Numbering
- **Source:** `channel_meta.custom_number` (fallback: `Channel.getDisplayNumber()`)
- **Edit:** Dialog with number input. Validate uniqueness against all other numbers.
- **Duplicate prevention:** `ChannelNumberingManager.isDuplicate()` checks custom + broadcast numbers.
- **Bi-directional:** Number → channel + channel → number.

### 1.3 Manual Channel Rename
- **Source:** `channel_meta.custom_name` (fallback: `Channel.getDisplayName()`)
- **Edit:** Dialog with text input, max 50 chars.
- **Display:** Custom name everywhere (channel list, EPG, banner, settings).

### 1.4 Logo Handling (3-tier)
1. **Broadcast logo:** `Channel.getLogoUri()` → `Channel.loadBitmap()` from TvProvider.
2. **Locally stored:** `channel_meta.logo_path` → user-assigned image file.
3. **App default:** Placeholder drawable.
- **Cache:** `logo_cache` table stores fetched PNGs.
- **User assignment:** File picker → copy to app storage → store path.

### 1.5 Favorites
- **Toggle:** `channel_meta.is_favorite` (boolean).
- **UI:** Star icon on channel row. Long-press toggles.
- **Quick Favorites Row:** Top of channel list, horizontal scroll, 5-10 channels.
- **Query:** `SELECT * FROM channel_meta WHERE is_favorite = 1 ORDER BY custom_number`.

### 1.6 Custom Groups
- **Create:** Settings → Manage Groups → New Group (name input).
- **Add channels:** Multi-select checkbox list → "Add to Group".
- **Group numbering:** Toggle per group. Stores in `group_channels.group_number`.
- **Display:** Tab bar in channel list (All | Group1 | Group2 | ...).

### 1.7 Watch History
- **Record:** On channel tune, upsert `watch_history` with timestamp.
- **Keep:** Last 50 entries.
- **UI:** "Recent" tab/section. Shows "Watched 5 min ago".
- **Click:** Tune to channel.

### 1.8 Last Channel Recall
- **Store:** `lastChannelId` in SharedPreferences on each channel change.
- **Trigger:** Dedicated remote button or UI button.
- **Action:** `ChannelTuner.moveToChannel(lastChannelId)`.

### 1.9 Search
- **Scope:** Channel number + channel name only.
- **Index:** In-memory map: channelId → (number, displayName, customName).
- **UI:** Search bar at top of channel list. Filter as you type.

---

## 2. Launcher Integration

- **Goal:** Push up to 10 channels as Android TV home screen shortcuts.
- **Selection:** Grid of channels in Settings → select up to 10.
- **Storage:** `launcher_channels` table.
- **Implementation:** Use `TvContractCompat.buildChannelUri()` to create entries visible to launcher.
- **Update:** On save, update TvProvider entries for selected channels.

---

## 3. EPG (Electronic Program Guide)

### 3.1 Now/Next
- **Source:** `ProgramDataManager.getCurrentProgram(channelId)`.
- **Display:** In channel list rows + playback channel banner.
- **Fields:** Program title, start/end time, description.

### 3.2 EPG Grid
- **Source:** `ProgramDataManager.getPrograms(channelId, startTime)`.
- **Vertical:** Channels (scrollable). Horizontal: Time (2-hour window).
- **Cell:** Program title + short description.
- **Current time indicator:** Vertical line.
- **Click cell:** Tune to channel.
- **Prefetch:** `ProgramDataManager.setPrefetchTimeRange()`.

---

## 4. Playback

### 4.1 Live Channel Playback
- **Widget:** Android `TvView` (uses core's TvInputService).
- **Tune:** `TvView.tune(inputId, TvContract.buildChannelUri(channelId))`.
- **Navigation:** Channel up/down via `ChannelTuner`.
- **Fast zapping:** Core handles PID filter reuse for same-frequency channels.

### 4.2 Audio Track Switching
- **List:** `TvView.getTracks(TvTrackInfo.TYPE_AUDIO)`.
- **Select:** `TvView.selectTrack(TYPE_AUDIO, trackId)`.
- **Persist:** `TvSettings.setMultiAudioId()`.

### 4.3 Always Full Screen
- `TvView` layout: `match_parent` × `match_parent`.
- No aspect ratio controls.

### 4.4 Channel Banner Overlay
- **Content:** Logo + number + name + current program title + progress bar.
- **Behavior:** Auto-hide after 5s. Show on channel change or Info button.
- **Animation:** Fade in/out.

---

## 5. Settings

### 5.1 Channel Management
- Grouping, numbering, renaming — reuses channel management UI.

### 5.2 Launcher Channels (max 10)
- Settings → Home Screen Channels → grid selector.

### 5.3 Default Channel
- On app boot, auto-tune to selected channel.
- Store: `PREF_DEFAULT_CHANNEL_ID` in SharedPreferences.

### 5.4 Open App on Device Boot
- `BootCompletedReceiver` → launch main activity.
- Toggle in settings.

### 5.5 Parental PIN
- 4-digit PIN. App lock + channel lock.
- Core already has PIN infrastructure via `TvSettings`.

### 5.6 Sleep Timer
- Options: Off / 15 / 30 / 45 / 60 / 90 / 120 min.
- `CountDownTimer` on playback start. On expire: stop playback + toast.

### 5.7 Import/Export Settings
- JSON format: channel_meta, groups, favorites, launcher channels, settings.
- File picker for import/export (`ActivityResultContracts`).

### 5.8 UI Options
- Accent color (8-10 presets).
- Font size (small/normal/large).
- Theme (light/dark/system).

### 5.9 Signal Quality Indicator
- **Source:** `DvbTunerHal.getSignalStrength()` (0-100).
- **Display:** 4-bar icon in channel banner + channel list.
- **Poll:** Every 5 seconds during playback.

---

## 6. Excluded Features
- Timeshift / recording
- Multi-view / PiP
- Manual scan
- Teletext / MHEG
- Aspect ratio controls
- Reminders
- Internet streaming
- Cloud EPG
- Google account sign-in
- Analytics / tracking
- Recommendations