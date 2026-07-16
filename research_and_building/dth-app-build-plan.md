# Custom DTH Channel App — Build Plan (Airtel Xstream, Hardware Tuner)

## Scope Boundaries — Read This First (source of truth)

This section exists to remove all ambiguity. If anything elsewhere in this
document conflicts with this section, this section wins.

### We copy ZERO code from AOSP LiveTv. Ever.
- No `.java`/`.kt` file, class, or resource from `packages/apps/TV` is copied,
  pasted, subclassed, imported, or linked into this project, at any point.
- Nothing from `tuner/` (AOSP's own software/USB DVB tuner implementation) is
  used — irrelevant here, since the Xstream box already has a working vendor
  `TvInputService` for its hardware tuner.
- Nothing from `common/analytics`, `onboarding/`, `menu/` (leanback UI), or any
  Dagger/Guava plumbing is used.
- The app never declares `privileged: true`, never requests
  `com.android.providers.tv.permission.ALL_EPG_DATA`, never ships as a system app.

### We copy code directly where it's genuinely practical, reference-only where it isn't
This isn't a blanket rule either way — it's decided per file, based on actually
inspecting that file's dependency graph (done below for the files that matter most).
Apache-2.0 permits direct copying with attribution; the only question is whether a
file's coupling to other AOSP-internal classes is shallow enough to be worth it.

| File | Verdict | Why |
|---|---|---|
| `src/com/android/tv/ChannelTuner.java` | **COPY near-verbatim** | ~250 lines of the actual channel-navigation logic (current channel tracking, next/prev browsable channel, nearest-browsable fallback, change listeners). Depends on only three AOSP-internal types (`ChannelDataManager`, `Channel` interface, `TvInputManagerHelper`) — all things you're building in the data layer anyway. If your repository classes expose matching method names, this file drops in with only import-path edits, no logic rewrite. This is your actual "battle-proven playback router." |
| `src/com/android/tv/TvActivity.java` | N/A — trivial | Just a 4-line launcher-redirect stub, not playback logic. Nothing to copy or reference. |
| `src/com/android/tv/MainActivity.java` | **Reference-only, extract a fragment** | ~50+ imports wired directly into DVR, `TimeShiftManager`, the full `TvOverlayManager` menu/side-panel system, parental controls, analytics `Tracker`, `MediaSessionWrapper`, onboarding, PIP handling, a Dagger-style singleton graph. The actual `TvView.tune()` call sequence is maybe 20 lines inside a 900+ line file. Copying the file means dragging in excluded features (DVR, timeshift) or performing surgery — at that point you're writing new code informed by reading old code, not copying. Read `startTv()`/`tune()` for the sequencing pattern, write your own short version. |
| `src/com/android/tv/data/ChannelDataManager.java`, `ProgramDataManager.java`, `WatchedHistoryManager.java`, `data/ChannelNumber.java` | **To be assessed the same way** — inspect actual imports before deciding copy vs. reference, don't assume | Not yet inspected line-by-line as of this plan's writing. Before Phase 1/4/5, pull each file's real import list the same way `ChannelTuner.java` was checked above, and update this table with a real verdict instead of a guess. |

The pattern going forward: **check the file, don't assume.** Shallow-dependency files
get copied. Deep-dependency files get read for the pattern, then rewritten short and
clean against just the public API.

### Explicitly NOT built, NOT included — do not implement, do not add libraries for these
| Excluded | Why |
|---|---|
| ExoPlayer / any custom media player | Playback is `TvView.tune()` only — the vendor's `TvInputService` does all tuning/demux/decrypt/decode internally. Never touch a stream or a decoder. |
| M3U / M3U8 playlist parsing | Not applicable — this is a hardware DTH tuner, not IPTV. No playlists exist anywhere in this data flow. |
| PVR / Recording | Excluded per original spec |
| Timeshift | Excluded per original spec |
| Multi-view | Excluded per original spec |
| Manual channel scan | Excluded — app only reads channels the vendor input already populated into `TvProvider`; app never scans |
| Teletext / MHEG | Excluded per original spec |
| Aspect ratio controls | Excluded per original spec (always full screen, fit to screen) |
| Reminders | Excluded per original spec |
| Leanback UI framework | Not used for the data/logic layer at all; only reconsider if wanted for the UI phase (Phase 8), and even then it's optional, not required |

### UI sequencing — explicit
No UI is designed, styled, or built until Phases 0–7 (data layer, playback,
channel management, EPG, app-owned features like history/PIN/sleep timer, launcher
integration) are complete and verified through the plain-text throwaway harness
described in Phase 6. Phase 8 ("Real UI") is the only phase where visual design
work happens, and it starts only after everything above it is done and tested on
the actual Xstream box.

---

## 0. Ground truth this plan is based on

- Confirmed working: Airtel Xstream box exposes its DTH tuner as a registered
  `TvInputService` to the system `TvInputManager`. Proven because both AOSP LiveTv
  and MochiTIF (a plain sideloaded third-party APK, not system-signed) play channels
  on it. This means: **no privileged install, no vendor HAL access, no
  `ALL_EPG_DATA` permission needed.** You are a normal TIF client app.
- Reference codebase: `platform/packages/apps/TV` (AOSP "LiveTv" / "Live Channels"),
  Apache-2.0 licensed. Repo: `https://android.googlesource.com/platform/packages/apps/TV`
  Browsable mirror for reading single files fast: `https://cs.android.com/android/platform/superproject/+/master:packages/apps/TV/`
- We are **not forking this app**. Nothing from it is compiled into, imported by,
  or depended on by your project — zero lines of its code end up in your APK. It's
  `priv-app`-only (declares `privileged: true`, needs system permissions), pulls in
  Guava/Dagger/leanback/analytics, and its data classes are tightly coupled to its
  own DI graph, so it wouldn't even compile standalone inside a normal app if you
  tried. It is used purely as **read-only reference material** — the same role a
  textbook or a second documentation tab plays. You read a specific file to see
  *how they solved a problem* (e.g. how they query `Programs` with a time-window
  selection), then write your own small function against the same public
  `android.media.tv.*` / `android.media.tv.TvContract` APIs it itself is built on.

Two ways to read the reference files, pick whichever's easier, neither becomes
part of your project:

- **Browser, zero local footprint:** open each file path below directly on
  `cs.android.com/android/platform/superproject/+/master:packages/apps/TV/<path>`
  — nothing touches your disk.
- **Local read-only checkout, if you prefer `grep`/your editor over a browser:**
  ```bash
  # a separate folder, NOT inside your app project — for reading only, never referenced by any build.gradle
  git clone --depth 1 --branch android-live-tv \
    https://android.googlesource.com/platform/packages/apps/TV aosp-livetv-ref
  ```

---

## 1. Reference file map — what to study from where

| Your feature | AOSP file to read (path under `packages/apps/TV/`) | What to take from it |
|---|---|---|
| Channel data model | `src/com/android/tv/data/Channel.java`, `ChannelImpl.java` | How they wrap `TvContract.Channels` cursor columns into an immutable object — field list is your Room entity's source-of-truth field list |
| Loading channels from TvProvider, caching, change listening | `src/com/android/tv/data/ChannelDataManager.java` | Pattern for `ContentObserver` on `Channels.CONTENT_URI`, async cursor load, in-memory map keyed by channel ID, update-on-change |
| Channel number parsing/sorting/comparison | `src/com/android/tv/data/ChannelNumber.java` | Major/minor number parsing (e.g. "5-1") and comparator logic — useful even though you're overriding numbers yourself, for sorting broadcast-provided numbers before you assign your own |
| EPG data loading | `src/com/android/tv/data/ProgramDataManager.java` | Query pattern against `TvContract.Programs.CONTENT_URI` with time-window `selection`, prefetch-ahead strategy, per-channel program cache |
| EPG grid UI logic (not visuals) | `src/com/android/tv/guide/ProgramManager.java`, `ProgramGrid.java`, `GuideUtils.java` | Data-side logic only: how they bucket programs into a channel×time grid model. Ignore the View/Adapter classes — you'll build your own UI |
| Watch history | `src/com/android/tv/data/WatchedHistoryManager.java` | Exact pattern for a local (their case: SQLite-backed) watch-log table: channel id, start time, duration — maps almost directly onto your Room entity |
| Core tuning / fast zap / TvView lifecycle | `src/com/android/tv/ChannelTuner.java`, `TvActivity.java` | How they drive `TvView.tune()`, handle `TvInputCallback` states, and switch channels without tearing down the view — this is your `PlaybackManager` blueprint |
| Audio track handling | `src/com/android/tv/TvOptionsManager.java` | `TvView.getTracks()` / `selectTrack()` usage pattern |
| Search by channel | `src/com/android/tv/search/TvProviderSearch.java` | Confirms channel search is just a `ContentResolver` query with a `LIKE` selection on display name/number — nothing exotic |
| App bootstrap / input list handling | `src/com/android/tv/TvApplication.java`, `MainActivity.java` | How they enumerate `TvInputManager.getTvInputList()` and pick relevant inputs — you'll do the same, filtered to the Xstream input |

**Do not bother with:** `tuner/` module (software DVB tuner — irrelevant, you have a
working vendor input already), `common/analytics`, `onboarding/`, `menu/` (leanback
UI), `TimeShiftManager.java` (timeshift excluded per your spec), anything under
`data/epg/EpgReader.java` (that's for *external* EPG sourcing when a tuner input
doesn't supply its own program data — only touch this later if you discover Xstream's
input doesn't populate `Programs` table; see Phase 1 validation step).

**Launcher-shortcut feature is a different API family entirely**, not in the LiveTv
app repo — it's `androidx.tvprovider:tvprovider` (the Home Screen Channels /
"preview channels" Jetpack library), the same mechanism Hotstar/Zee5/JioTV use.
Reference: `developer.android.com/training/tv/discovery/recommendations-channel`.
Key classes: `Channel.Builder` (`TvContractCompat.Channels.TYPE_PREVIEW`),
`PreviewProgram.Builder` with `.setLive(true)` (confirmed API —
`BasePreviewProgram.Builder.setLive(boolean)`), `.setIntentUri(...)` pointing back
into your app with the target DTH channel id so selecting it from the home screen
launches straight into that channel.

---

## 2. The one architectural decision that shapes everything

**TvProvider rows for channels you don't own are effectively read-only to you.**
The system reserves per-input row ownership so only the owning input's package (or
a privileged system app) can write back to `Channels`/`Programs` rows. As a normal
third-party app you get `android.permission.READ_TV_LISTINGS` (read-only, no special
install). That means:

- Your custom numbering, renaming, favorites, groups, and any other user edits are
  **never written into TvProvider.** They live entirely in your own local database,
  keyed against the broadcaster's channel identity.
- Key by something stable, not the raw `Channels._ID` alone — if the Xstream input
  ever re-scans, row IDs can be reassigned. Use a composite/stable key: input's
  `COLUMN_SERVICE_ID` + `COLUMN_ORIGINAL_NETWORK_ID` + `COLUMN_TRANSPORT_STREAM_ID`
  if populated (standard DVB service identity triplet), falling back to
  `COLUMN_DISPLAY_NAME` if those are null. **Validate in Phase 1 which of these
  columns Xstream's input actually populates** — this determines your key strategy
  and is the single biggest unknown in this whole plan.
- This also means: your app is a **pure overlay/read layer**. It never scans, never
  writes to `Channels`, never touches `Programs` for writing. Much simpler, and
  matches your "no manual scan" exclusion anyway.

---

## 3. Module / package structure

Recommended as separate Gradle modules so the data/logic layer is fully buildable
and testable before any UI exists — matches your "functions first, UI later" plan.

```
app/                          (thin shell — temporary test harness only, see Phase 6)
core-tif/                     (wraps android.media.tv.* — no app logic)
  TifInputRepository.kt         - enumerate/find the Xstream TvInputInfo
  TvProviderChannelSource.kt    - read Channels table for that input
  TvProviderEpgSource.kt        - read Programs table, now/next + grid window
data/                          (Room DB — all app-owned state)
  entities: ChannelMeta, ChannelGroup, ChannelGroupMember, WatchHistoryEntry,
            FavoriteFlag(part of ChannelMeta), AppSettings
  dao: ChannelMetaDao, GroupDao, HistoryDao, SettingsDao
  ChannelRepository.kt          - merges TvProvider live data + local ChannelMeta
playback/
  PlaybackManager.kt            - wraps a single TvView instance, tune()/zap logic,
                                   TvInputCallback -> state flow, audio track API
launcher/
  HomeScreenChannelSync.kt      - androidx.tvprovider push of up to 10 channels
boot/
  BootReceiver.kt                - RECEIVE_BOOT_COMPLETED -> optionally launch app
                                    on last/default channel
settings-io/
  BackupExport.kt / BackupImport.kt  - serialize Room DB + prefs to JSON
```

---

## 4. Manifest / permissions needed

```xml
<uses-permission android:name="android.permission.READ_TV_LISTINGS" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<!-- normal app, NOT priv-app, NOT system-signed -->
```

Gradle deps:
```
implementation "androidx.tvprovider:tvprovider:1.1.0"   // launcher channels
implementation "androidx.leanback:leanback:1.0.0"        // optional, later for UI only
implementation "androidx.room:room-runtime:...", "androidx.room:room-ktx:..."
```
No ExoPlayer, no media3, no M3U parser, no PVR libs — confirmed not needed since
playback is entirely `TvView.tune(inputId, channelUri)`.

---

## 5. Phased build order (functions fully before UI)

Each phase should be verifiable via `Log.d`/`adb logcat` and a **bare-bones single
Activity with a `TvView` + a plain `TextView`/`ListView` dump** — not real UI, just
enough to visually confirm state (see Phase 6 note). Don't build real UI until
Phase 6.

### Phase 0 — Environment validation (do this first, before writing app logic)
- Write a throwaway Activity that calls `TvInputManager.getTvInputList()` and logs
  every `TvInputInfo` (id, type, label). Confirm you can identify the Xstream DTH
  input programmatically (by type, or by label matching) without hardcoding a
  package name you don't yet know.
- Query `TvContract.Channels.CONTENT_URI` filtered `WHERE input_id = ?` for that
  input, log all rows + all populated columns (dump full `Cursor` column names).
  **This tells you exactly which columns Xstream actually populates** —
  `COLUMN_CHANNEL_LOGO`, service/network/transport IDs, `COLUMN_DISPLAY_NUMBER`, etc.
  Do not assume; confirm empirically.
- Query `TvContract.Programs.CONTENT_URI` for one channel over a wide time window.
  Confirm EPG data actually exists there (LiveTv showing a guide implies yes, but
  verify programmatically — some OEM inputs populate channels but feed their own
  EPG UI from a different source).
- Confirm `TvView.tune()` from your unsigned app succeeds without a DRM/signing
  restriction (implied working, since MochiTIF proved it — but confirm in your
  own package too, since some restrictions can be per-package-name allowlists,
  though unlikely given TIF's design).

### Phase 1 — Core data layer: channel + input discovery
- `TifInputRepository`: find and hold the target `TvInputInfo` / `inputId`.
- `TvProviderChannelSource`: `ContentResolver` query + `ContentObserver` on
  `Channels.CONTENT_URI` (pattern from `ChannelDataManager.java`), expose as a
  Flow/LiveData of raw channel rows (`TvContractCompat.Channels` fields).
- Room `ChannelMeta` entity: `stableKey (unique)`, `assignedNumber (unique, nullable
  until user assigns default)`, `customName (nullable)`, `isFavorite (bool)`,
  `localLogoPath (nullable)`.
- `ChannelRepository`: joins live `TvProvider` rows with `ChannelMeta` by stable key,
  auto-creates a `ChannelMeta` row (with broadcast-provided number/name as default)
  the first time a new channel is seen — this is your "auto-load channels on app
  start" requirement, satisfied structurally here.
- Acceptance test: dump repository output to logcat; every channel has exactly one
  merged record, no duplicates, no crashes on repeated app restarts.

### Phase 2 — Playback
- `PlaybackManager` wrapping one `TvView`: `tune(channel)`, exposes tuning state
  (`onTuned`, `onVideoAvailable`, `onError`) as a state flow your future UI observes.
  Pattern from `ChannelTuner.java` / `TvActivity.java` — specifically how they avoid
  re-creating the `TvView`/session on every zap (just call `tune()` again on the
  same session).
- Fast zap = call `tune()` again immediately; no manual session teardown.
- Audio track switching: `TvView.getTracks(TYPE_AUDIO)` + `selectTrack()`, exposed
  as a simple list + selector method.
- Acceptance test: from the Phase-0 throwaway harness, cycle through all channels
  from `ChannelRepository` via keypress, confirm picture comes up each time,
  confirm audio track list is non-empty on at least one channel.

### Phase 3 — Channel management features
- Unique numbering: enforce via Room `UNIQUE` index on `assignedNumber`; write a
  `renumber(channelId, newNumber)` repository method that swaps numbers atomically
  if the target number is taken (bi-directional swap, per your spec) rather than
  rejecting.
- Rename: trivial `ChannelMeta.customName` update.
- Logo handling — 3-tier fallback function `resolveLogo(channel)`:
  1. `localLogoPath` if you've cached one,
  2. else `TvContract.buildChannelLogoUri(channelId)` (broadcast-provided, if
     Phase 0 confirmed this column is populated),
  3. else a bundled default drawable.
  Cache broadcast logos to local storage on first resolve (simple `ContentResolver.
  openInputStream` -> file write) so you're not re-hitting TvProvider's logo blob
  repeatedly.
- Favorites: `ChannelMeta.isFavorite` toggle + repository filter method.
- Custom Groups: `ChannelGroup(id, name)` + `ChannelGroupMember(groupId, channelId,
  groupSpecificNumber nullable)` join table — this directly supports "group-specific
  numbering" since the number lives on the membership row, not the channel.
- Search: repository method `searchChannels(query)` — `WHERE customName LIKE ? OR
  assignedNumber = ?`, purely a Room query, no special logic needed (confirmed
  trivial by `TvProviderSearch.java` precedent, just applied to your own table
  instead of theirs).
- Acceptance test: unit tests on the Room DAOs (no Android framework needed for
  most of this — plain JVM Room tests) covering duplicate-number swap, group
  membership with override numbers, search matching.

### Phase 4 — EPG
- `TvProviderEpgSource`: query `Programs.CONTENT_URI` for a given channel + time
  range (`start <= now AND end >= now` for Now, next row after that for Next).
- Grid data model: for the guide screen, batch-query all visible channels' programs
  over e.g. a 3-hour rolling window, bucket by channel — logic pattern from
  `ProgramManager.java`, reimplemented against your own `Channel`/`ChannelMeta`
  types instead of theirs.
- Acceptance test: log Now/Next for every channel; confirm grid-window query returns
  a sane number of rows in reasonable time (this table can be large — make sure
  your selection is properly time-bounded and indexed).

### Phase 5 — App-owned features (all pure local logic, no TIF/UI dependency)
- Watch History: `WatchHistoryEntry(channelId, watchedAtEpochMillis, durationMillis
  nullable)` — insert on tune, update duration on next tune/stop. Direct structural
  match to `WatchedHistoryManager.java`.
- Last Channel Recall: single `lastChannelId` field in a settings table/DataStore,
  updated on every successful tune.
- Default channel on boot / open app on boot: `BootReceiver` (needs
  `RECEIVE_BOOT_COMPLETED`) reads `defaultChannelId` setting; if "open on boot" is
  enabled, launches your main Activity which auto-tunes to it.
- Parental PIN: simple local gate — hashed PIN in settings, a `requirePin(action)`
  wrapper you call before switching to a channel flagged locked in your own
  `ChannelMeta` (this is your own app-level lock flag, not the system's rating
  lock — keeps scope small per your feature list).
- Sleep Timer: `CountDownTimer` or scheduled `Handler.postDelayed` from the active
  playback screen; stop playback / finish activity on expiry. No manifest changes
  needed.
- Import/Export: serialize all Room tables + settings to a single JSON file. On
  Android TV, standard Storage Access Framework file pickers are unreliable/ugly
  on remote-only navigation — simplest reliable approach: write to
  `getExternalFilesDir(null)/backup.json` and let import read the same fixed path
  (document this clearly in your own future UI: "insert USB drive, backup saved to
  X" or similar), rather than fighting SAF on a remote-control-only device.
- Acceptance test: kill+restart app, confirm last channel / default channel /
  history all persisted correctly; export then wipe DB then import, confirm full
  state restored.

### Phase 6 — Minimal test harness (not real UI — throwaway scaffold only)
Since you explicitly don't want to build real UI yet: keep a single debug Activity
with a `TvView` + a plain scrollable text list (no styling, no leanback) bound
directly to `ChannelRepository`, driven by D-pad up/down + OK to tune. This exists
purely so Phases 1–5 are verifiable end-to-end on the actual box before any design
work starts. Delete or gut this Activity once real UI work begins.

### Phase 7 — Launcher integration (androidx.tvprovider)
- On first run / from future settings, let user pick up to 10 channels.
- For each: `Channel.Builder().setType(TYPE_PREVIEW)...setAppLinkIntentUri(...)`
  once per selected channel (each becomes its own home-screen row, matching how
  Hotstar/Zee5 do individual live tiles) — confirm during implementation whether
  you want one channel-row-per-DTH-channel (matches their pattern) or one channel-row
  containing 10 preview-programs (also valid, fewer home-screen rows). Given you
  said "similar to Hotstar/Zee5," their pattern is one row per pinned item, so favor
  the first approach.
- Attach one `PreviewProgram` per channel with `.setLive(true)`,
  `.setIntentUri(deep-link-uri-with-channel-stable-key)`, poster/logo from your
  resolved logo.
- Handle the resulting deep-link `Intent` in your main Activity to auto-tune to
  that channel on launch from the home screen row.
- Acceptance test: pin a channel, back out to Android TV home screen, confirm the
  tile appears and launches straight into that channel.

### Phase 8 — Real UI
Only after 0–7 are solid. Not detailed here per your instruction — revisit once
functions are done; your channel banner/overlay (logo+number+name+current program,
auto-hide/Info-toggle) is purely a UI layer reading state already exposed by
`PlaybackManager` + `ChannelRepository` + `TvProviderEpgSource`, so no new data-layer
work should be needed at that point.

---

## 6. Open items to resolve empirically (can't be answered from docs alone)

1. Which DVB identity columns (service/network/transport ID) does Xstream's input
   actually populate — determines your channel stable-key strategy. **Resolve in
   Phase 0.**
2. Does `Programs` table get populated by Xstream's input at all, or does its own
   app source EPG a different way while `LiveTv`'s guide happens to still show
   something cached/stale? **Resolve in Phase 0** — if Programs is genuinely empty,
   your whole Phase 4 needs a fallback EPG source (this would be a significant
   scope change, flag it immediately if found).
3. Whether `COLUMN_CHANNEL_LOGO` is populated (broadcast logo) or you'll be relying
   on tier 3 (bundled default) for most channels initially. **Resolve in Phase 0.**

Everything else in this plan follows directly from public, documented `android.
media.tv` / `androidx.tvprovider` APIs and is not device-specific.
