# VividOrbit — Architecture

## Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    CUSTOM UI LAYER (future)                     │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────────┐  │
│  │  Channel  │ │   EPG    │ │ Playback │ │    Settings      │  │
│  │  Screen   │ │  Screen  │ │  Screen  │ │    Screens       │  │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────────┬─────────┘  │
│       │             │            │                 │            │
│  ┌────┴─────────────┴────────────┴─────────────────┴────────┐  │
│  │              ViewModels / Repositories (Kotlin)           │  │
│  └────────────┬──────────────────────────────┬──────────────┘  │
│               │                              │                  │
│  ┌────────────┴──────────┐    ┌──────────────┴──────────────┐  │
│  │  Custom Room DB       │    │  Core AOSP Java APIs        │  │
│  │  - channel_meta       │    │  - ChannelDataManager       │  │
│  │  - groups              │    │  - ProgramDataManager       │  │
│  │  - favorites           │    │  - ChannelTuner             │  │
│  │  - watch_history       │    │  - TvInputManagerHelper     │  │
│  │  - launcher_channels   │    │  - TvProvider (channels,    │  │
│  │  - logo_cache          │    │    programs)                │  │
│  └────────────────────────┘    └─────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────┴───────────────────────────────────┐
│                    CORE APP (offline, no internet)              │
│  ┌──────────────────┐  ┌────────────────┐  ┌────────────────┐  │
│  │  TvInputService   │  │  Tuner HAL     │  │  JNI / DVB     │  │
│  │  (LiveTvTunerTv   │──│  (DvbTunerHal) │──│  (DvbManager)  │  │
│  │   InputService)   │  │                │  │                │  │
│  └──────────────────┘  └────────────────┘  └────────────────┘  │
│  ┌──────────────────┐  ┌────────────────────────────────────┐  │
│  │  In-band EPG     │  │  Channel Scan                       │  │
│  │  (EIT from TS)   │  │  (Tuner ChannelDataManager)         │  │
│  └──────────────────┘  └────────────────────────────────────┘  │
│                                                                  │
│  DEPENDENCIES: DVB kernel drivers only. No internet.            │
└─────────────────────────────────────────────────────────────────┘
```

## Key Principles

1. **100% offline** — No internet permission. No cloud EPG. No analytics. No location services.
2. **Core handles hardware** — TvInputService, DVB HAL, JNI, channel scan, EIT-based EPG.
3. **Custom UI is separate** — Will be built as a new `app/` module depending on `core-app/`.
4. **Custom DB is parallel** — Never writes to TvProvider for custom metadata (avoids rescan overwrites).
5. **No crashes** — Every nullable return from core APIs is null-checked. Every error path is handled.

## Module Structure

```
VividOrbit/
├── core-app/                    # Stripped AOSP LiveTV (existing)
│   ├── src/                     # ChannelDataManager, ProgramDataManager, etc.
│   ├── tuner/                   # TvInputService, TunerHal, setup UI
│   ├── common/                  # Shared utilities
│   └── jni/                     # DVB native code (C++)
│
├── app/                         # FUTURE: Custom UI application
│   ├── src/main/java/com/vividorbit/
│   │   ├── VividOrbitApplication.kt
│   │   ├── ui/
│   │   │   ├── channels/        # ChannelListScreen, ChannelEditDialog
│   │   │   ├── epg/             # EpgScreen, EpgGrid
│   │   │   ├── playback/        # PlaybackActivity, ChannelBannerView
│   │   │   ├── settings/        # SettingsScreen
│   │   │   └── common/          # Shared UI components
│   │   ├── data/
│   │   │   ├── db/              # Room database, DAOs, entities
│   │   │   ├── repository/      # ChannelRepository, EpgRepository
│   │   │   └── model/           # Domain models
│   │   └── di/                  # Hilt modules
│   └── build.gradle
│
└── research_and_building/       # Documentation (this folder)
    ├── architecture.md
    ├── feature_plan.md
    ├── database_schema.md
    ├── strip_plan.md
    └── integration_guide.md
```

## Integration Points (UI → Core)

| UI Need | Core API | Type |
|---------|----------|------|
| Channel list | `ChannelDataManager.getChannelList()` | Java |
| Current program | `ProgramDataManager.getCurrentProgram(channelId)` | Java |
| EPG grid data | `ProgramDataManager.getPrograms(channelId, startTime)` | Java |
| Tune channel | `TvView.tune(inputId, channelUri)` | Android SDK |
| Channel up/down | `ChannelTuner.moveToAdjacentBrowsableChannel(up)` | Java |
| Signal strength | `DvbTunerHal.getSignalStrength()` | Java |
| Audio track switch | `TvView.selectTrack(TYPE_AUDIO, trackId)` | Android SDK |
| Channel scan | `BaseTunerSetupActivity` | Android Activity |
| Boot auto-start | `BootCompletedReceiver` | Android Broadcast |

## Dependency Direction

```
app/ ──depends on──> core-app/
```

Single APK containing both the TvInputService and the custom UI.