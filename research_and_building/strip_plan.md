# VividOrbit — Strip Plan

## Goal

Remove ALL internet-dependent code from the AOSP LiveTV core. The app is a DTH satellite tuner that works 100% offline. EPG comes from in-band DVB EIT tables, not the cloud.

## Files to DELETE

### Internet connectivity
- `src/com/android/tv/util/NetworkUtils.java`
- `common/src/com/android/tv/common/util/NetworkTrafficTags.java`

### Account / sign-in
- `src/com/android/tv/util/account/AccountHelper.java`
- `src/com/android/tv/util/account/AccountHelperImpl.java`

### Cloud EPG
- `src/com/android/tv/data/epg/EpgInputWhiteList.java`
- `src/com/android/tv/data/epg/EpgFetchService.java`
- `src/com/android/tv/data/epg/EpgFetcherImpl.java` (replace with no-op)
- `src/com/android/tv/data/epg/EpgFetcher.java` (replace with no-op)

### Location services
- `common/src/com/android/tv/common/util/LocationUtils.java`
- `common/src/com/android/tv/common/util/PostalCodeUtils.java`
- `tuner/src/com/android/tv/tuner/setup/LocationFragment.java`
- `tuner/src/com/android/tv/tuner/setup/PostalCodeFragment.java`

### Network tuner (HDHomeRun — LAN-based, not DTH)
- `tuner/src/com/android/tv/tuner/hdhomerun/` (entire directory)
- `tuner/SampleNetworkTuner/` (entire directory)

### Cloud EPG lineup
- `tuner/src/com/android/tv/tuner/setup/LineupFragment.java`

### Unused
- `src/com/android/tv/data/Lineup.java` (cloud EPG lineups)
- `src/com/android/tv/data/ChannelLogoFetcher.java` (internet logo fetcher)
- `src/com/android/tv/data/PreviewDataManager.java` (channel previews / recommendations)
- `src/com/android/tv/data/PreviewProgramContent.java` (preview content)
- `src/com/android/tv/data/WatchedHistoryManager.java` (replaced by custom Room DB)
- `src/com/android/tv/data/OnCurrentProgramUpdatedListener.java` (replaced by Room)
- `src/com/android/tv/util/OnboardingUtils.java` (onboarding wizard)
- `src/com/android/tv/util/GtvUtils.java` (Google TV specific)
- `src/com/android/tv/features/PartnerFeatures.java` (partner customization)
- `src/com/android/tv/features/TvFeatures.java` (feature flags for cloud/internet features)
- `src/com/android/tv/util/Partner.java` (partner package discovery)
- `src/com/android/tv/util/SetupUtils.java` (setup helper, references cloud EPG components)
- `src/com/android/tv/data/GenreItems.java` (genre labels from resources)
- `src/com/android/tv/data/DisplayMode.java` (display mode, UI feature)
- `src/com/android/tv/data/TvInputNewComparator.java` (input sorting)
- `src/com/android/tv/ChannelChanger.java` (channel change helper, unused)
- `src/com/android/tv/Starter.java` (app starter, unused)
- `src/com/android/tv/perf/` (entire directory — performance monitoring)
- `src/com/android/tv/util/CompositeComparator.java` (unused)
- `src/com/android/tv/util/TimeShiftUtils.java` (timeshift, excluded)
- `src/com/android/tv/util/TvProviderUtils.java` (extra columns, unused)
- `src/com/android/tv/util/ViewCache.java` (UI caching, unused)
- `src/com/android/tv/util/AsyncDbTask.java` (DB utility, keep if needed)
- `src/com/android/tv/util/MainThreadExecutor.java` (unused)
- `src/com/android/tv/util/ToastUtils.java` (UI, keep for future)
- `src/com/android/tv/ui/api/TunableTvViewPlayingApi.java` (UI API, unused)
- `src/com/android/tv/util/images/` (image loading, keep minimal)
- `src/com/android/tv/util/CaptionSettings.java` (caption settings, keep)
- `src/com/android/tv/util/MultiLongSparseArray.java` (unused)

## Files to MODIFY

### TvApplication.java
- Remove: `mEpgFetcher.startRoutineService()` call
- Remove: Analytics imports and usage
- Remove: PerformanceMonitor references
- Remove: Partner/Onboarding initialization

### LiveTvModule.java
- Remove: `AccountHelper` binding
- Remove: `EpgFetcher` binding
- Remove: `EpgReader` binding
- Remove: `PerformanceMonitor` binding
- Remove: `PartnerFeatures` binding
- Remove: `TvFeatures` binding

### AndroidManifest_common.xml
- Remove: `ACCESS_COARSE_LOCATION` permission
- Remove: `ACCESS_NETWORK_STATE` permission
- Remove: `INTERNET` permission
- Remove: `EpgFetchService` service declaration

### BaseTunerSetupActivity.java
- Remove: Location permission request
- Remove: PostalCodeFragment references
- Remove: LocationFragment references
- Remove: LineupFragment references
- Simplify: Show only Welcome → ConnectionType → Scan → ScanResult

### BitmapUtils.java (in common module)
- Remove: HTTP URL download path
- Keep: Content URI (`content://`, `file://`, `android.resource://`) support

### DefaultCloudEpgFlags.java
- Set `compiled()` to return `false`
- Set `thirdPartyEpgInputs()` to return empty list

### EpgFetcherImpl.java → replace with NoOpEpgFetcher.java
```java
public class NoOpEpgFetcher implements EpgFetcher {
    @Override public void startRoutineService() {}
    @Override public void fetchImmediatelyIfNeeded() {}
    @Override public void fetchImmediately() {}
    @Override public void onChannelScanStarted() {}
    @Override public void onChannelScanFinished() {}
    @Override public void stopFetchingJob() {}
    @Override public boolean executeFetchTaskIfPossible(JobService s, JobParameters p) { return false; }
}
```

### EpgFetcher.java
- Keep the interface.
- Add `fetchImmediately()` and `executeFetchTaskIfPossible()` methods if not already present.

### ChannelDataManager.java (core app)
- Remove: `ChannelLogoFetcher` references
- Remove: `PreviewDataManager` references
- Remove: `WatchedHistoryManager` references

### ProgramDataManager.java
- Remove: `PreviewDataManager` references
- Keep: Core program querying functionality (needed for EPG)

### TvInputManagerHelper.java
- Remove: Partner input references
- Remove: OnboardingUtils references
- Keep: Core input management

### Utils.java
- Remove: `getLastWatchedChannelId()` etc. (replaced by Room)
- Remove: Recording-related methods
- Remove: `isInternalTvInput()` (network check)
- Keep: Video definition strings, audio channel strings, time formatting

## Files to KEEP (essential for offline DTH)

### Hardware / Tuner
- `jni/DvbManager.cpp`, `jni/DvbManager.h`
- `jni/tunertvinput_jni.cpp`, `jni/tunertvinput_jni.h`
- `tuner/src/com/android/tv/tuner/TunerHal.java`
- `tuner/src/com/android/tv/tuner/dvb/DvbTunerHal.java`
- `tuner/src/com/android/tv/tuner/dvb/DvbTunerHalFactory.java`
- `tuner/src/com/android/tv/tuner/dvb/DvbDeviceAccessor.java`
- `tuner/src/com/android/tv/tuner/api/Tuner.java`
- `tuner/src/com/android/tv/tuner/api/TunerFactory.java`
- `tuner/src/com/android/tv/tuner/api/ScanChannel.java`
- `tuner/src/com/android/tv/tuner/api/ChannelScanListener.java`

### TvInputService / Playback
- `tuner/src/com/android/tv/tuner/tvinput/BaseTunerTvInputService.java`
- `tuner/src/com/android/tv/tuner/tvinput/TunerSession.java`
- `tuner/src/com/android/tv/tuner/tvinput/TunerSessionExoV2.java`
- `tuner/src/com/android/tv/tuner/tvinput/TunerSessionWorker.java`
- `tuner/src/com/android/tv/tuner/tvinput/TunerSessionWorkerExoV2.java`
- `tuner/src/com/android/tv/tuner/tvinput/TunerSessionOverlay.java`
- `tuner/src/com/android/tv/tuner/tvinput/factory/TunerSessionFactory.java`
- `tuner/src/com/android/tv/tuner/tvinput/factory/TunerRecordingSessionFactory.java`
- `tuner/src/com/android/tv/tuner/livetuner/LiveTvTunerTvInputService.java`

### Channel Data (tuner)
- `tuner/src/com/android/tv/tuner/tvinput/datamanager/ChannelDataManager.java`
- `tuner/src/com/android/tv/tuner/data/TunerChannel.java`
- `tuner/src/com/android/tv/tuner/data/PsipData.java`
- `tuner/src/com/android/tv/tuner/data/PsipData.java` (EitItem, VctItem, etc.)
- `tuner/src/com/android/tv/tuner/data/SectionParser.java`
- `tuner/src/com/android/tv/tuner/data/Cea708Data.java`
- `tuner/src/com/android/tv/tuner/data/Cea708Parser.java`

### Channel Data (core)
- `src/com/android/tv/data/ChannelDataManager.java`
- `src/com/android/tv/data/ChannelImpl.java`
- `src/com/android/tv/data/ChannelNumber.java`
- `src/com/android/tv/data/api/Channel.java`
- `src/com/android/tv/data/api/Program.java`
- `src/com/android/tv/data/api/BaseProgram.java`
- `src/com/android/tv/data/BaseProgramImpl.java`
- `src/com/android/tv/data/ProgramDataManager.java`
- `src/com/android/tv/data/ProgramImpl.java`
- `src/com/android/tv/data/ParcelableList.java`
- `src/com/android/tv/data/StreamInfo.java`
- `src/com/android/tv/data/InternalDataUtils.java`

### EPG
- `src/com/android/tv/data/epg/EpgFetchHelper.java` (writes to local TvProvider)
- `src/com/android/tv/data/epg/StubEpgReader.java` (no-op implementation)

### Tuner Input Controller
- `src/com/android/tv/tunerinputcontroller/BuiltInTunerManager.java`
- `src/com/android/tv/tunerinputcontroller/HasBuiltInTunerManager.java`
- `src/com/android/tv/tunerinputcontroller/TunerInputController.java`

### ExoPlayer (playback)
- `tuner/src/com/android/tv/tuner/exoplayer2/` (all files)
- `tuner/src/com/google/android/exoplayer/` (MediaFormatUtil, MediaSoftwareCodecUtil)

### Setup (simplified)
- `tuner/src/com/android/tv/tuner/setup/BaseTunerSetupActivity.java` (modified)
- `tuner/src/com/android/tv/tuner/setup/WelcomeFragment.java`
- `tuner/src/com/android/tv/tuner/setup/ConnectionTypeFragment.java`
- `tuner/src/com/android/tv/tuner/setup/ScanFragment.java`
- `tuner/src/com/android/tv/tuner/setup/ScanResultFragment.java`
- `tuner/src/com/android/tv/tuner/setup/ChannelScanFileParser.java`

### Utilities (keep)
- `src/com/android/tv/util/Utils.java` (modified)
- `src/com/android/tv/util/TvSettings.java`
- `src/com/android/tv/util/TvTrackInfoUtils.java`
- `src/com/android/tv/util/TvInputManagerHelper.java` (modified)
- `src/com/android/tv/util/TvUriMatcher.java`
- `src/com/android/tv/util/RecurringRunner.java`
- `tuner/src/com/android/tv/tuner/util/ConvertUtils.java`
- `tuner/src/com/android/tv/tuner/util/Ints.java`
- `tuner/src/com/android/tv/tuner/util/ByteArrayBuffer.java`
- `tuner/src/com/android/tv/tuner/util/StatusTextUtils.java`
- `tuner/src/com/android/tv/tuner/util/GlobalSettingsUtils.java`

### DI / App
- `src/com/android/tv/TvApplication.java` (modified)
- `src/com/android/tv/TvSingletons.java` (modified)
- `src/com/android/tv/app/LiveTvApplication.java` (modified)
- `src/com/android/tv/app/LiveTvApplicationComponent.java`
- `src/com/android/tv/app/LiveTvModule.java` (modified)
- `src/com/android/tv/modules/TvApplicationModule.java`
- `src/com/android/tv/modules/TvSingletonsModule.java`

### Source / TS Streaming
- `tuner/src/com/android/tv/tuner/source/` (all files)
- `tuner/src/com/android/tv/tuner/ts/` (EventDetector, TsParser)

### Prefs / Layout
- `tuner/src/com/android/tv/tuner/prefs/TunerPreferences.java`
- `tuner/src/com/android/tv/tuner/layout/ScaledLayout.java`
- `tuner/src/com/android/tv/tuner/cc/` (CaptionLayout, Cea708 renderers)

### Singletons
- `tuner/src/com/android/tv/tuner/singletons/TunerSingletons.java`
- `tuner/src/com/android/tv/tuner/modules/TunerModule.java`
- `tuner/src/com/android/tv/tuner/modules/TunerSingletonsModule.java`

## Files to HANDLE GRACEFULLY (null safety)

All retained files must be audited for:
1. **Null returns** — Every method returning a nullable type must have a null check at the call site.
2. **Try/catch** — All hardware I/O (DVB ioctl, file operations) must be try/caught.
3. **Default values** — When a channel or program is not found, return a sensible default, not null.
4. **No crashes** — Never throw uncaught exceptions. Log and recover.

### Key error handling patterns:
```java
// Before (crash-prone)
Channel channel = channelDataManager.getChannel(channelId);
String name = channel.getDisplayName(); // NPE if channel is null

// After (safe)
Channel channel = channelDataManager.getChannel(channelId);
String name = (channel != null) ? channel.getDisplayName() : "Unknown";

// Hardware I/O
try {
    boolean tuned = tunerHal.tune(freq, modulation, null);
    if (!tuned) {
        Log.w(TAG, "Tune failed for frequency " + freq);
        notifyTuneFailed();
    }
} catch (Exception e) {
    Log.e(TAG, "Tune error", e);
    notifyTuneFailed();
}
```