# Phase 0 Empirical Findings

This document records the findings from the initial Phase 0 validation runs on the physical Airtel Xstream Android TV box.

## 1. Tuner Identification
- **Identified Input ID**: `com.droidlogic.dtvkit.inputsource/.DtvkitTvInput/HW19`
- **Label**: `DTVKit2`
- **Context**: DTVKit is the industry-standard DVB middleware running on Amlogic hardware. The presence of this specific tuner confirms we are interacting directly with the hardware DTH tuner, not an IP stream.

## 2. Channels Table
- **Total Channels Found**: 701
- **Permission requirement**: Third-party apps absolutely MUST request the `android.permission.READ_TV_LISTINGS` permission at runtime (or have it granted via ADB). Without this runtime grant, the `TvContract.Channels` query returns 0 rows.
- **Stable Key Selection**: The `Channels` table is richly populated with `original_network_id`, `service_id`, and `transport_stream_id`. We can use this DVB identity triplet as the stable primary key in our local Room database rather than the system's `_id` which might change upon a re-scan.
- **Logos**: The `logo` column contains blob data, confirming broadcast logos are available directly from the tuner.

## 3. Programs Table (EPG)
- **EPG Availability**: Natively available! A sample query against channel 715 successfully returned 87 program rows.
- **Conclusion**: The hardware tuner automatically scrapes EPG data from the satellite and populates `TvContract.Programs`. We do NOT need to implement our own external EPG scraper/fetcher.

## Artifacts
The raw `phase_zero_logs.txt` and the `phase_zero_screenshot.png` from the test run have been preserved in this folder alongside this summary.
