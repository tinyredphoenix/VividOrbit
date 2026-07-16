# UI Architecture & Framework Decisions (Phase 8)

This document captures the final decisions regarding the UI implementation for the Custom DTH App, superseding any prior assumptions.

## 1. UI Framework
**Decision: Jetpack Compose for TV (Foundation Only)**

We intentionally chose a highly customized TV interface with fixed center-pivot scrolling, custom DPAD focus behavior, layered overlays, and a custom EPG.
*   **Use `androidx.tv:tv-foundation`** for focus and DPAD primitives only.
*   **Do not use TV Material components or styling.** (`tv-material` brings opinions we don't want).
*   The interface implements its own focus styling, animations, scrolling behavior, and layouts.

## 2. EPG Grid Implementation
**Decision: Custom Compose synchronized layout, split into distinct files.**

Do not try to force the guide into RecyclerView-style abstractions. The EPG should be built as a custom synchronized layout where:
*   Timeline scrolls horizontally.
*   Program grid scrolls horizontally with the timeline.
*   Channel column remains horizontally fixed.
*   Channel column scrolls vertically with the program grid.
*   Focus behavior follows the center-pivot model.

**No monolithic files.** The Guide must be split into: `GuideTimeline.kt`, `GuideChannelColumn.kt`, `GuideProgramGrid.kt`, `GuideFocusEngine.kt`, and `GuideCoordinator.kt` to keep files under 300-500 lines.

## 3. Theme & Styling
Use a restrained palette matching the specifications.

**Colors:**
| Role | Color (Hex) |
| :--- | :--- |
| Primary Background | `#15181D` |
| Secondary Surface | `#1C2026` |
| Elevated Surface | `#252A31` |
| Divider | `#313842` |
| Primary Text | `#FFFFFF` |
| Secondary Text | `#AEB5BF` |
| Accent Blue | `#2E8BFF` |
| Focused Surface | `#E5E7EB` |
| Focused Text | `#111111` |
| Disabled Text | `#6D737D` |

**Constants (Never hardcode in UI):**
*   **Spacing:** `4.dp`, `8.dp`, `12.dp`, `16.dp`, `20.dp`, `24.dp`, `32.dp`
*   **Radius:** `6.dp`, `8.dp`, `10.dp`
*   **Animation Durations:** `150ms`, `180ms`
*   **Typography:** `Display`, `Title`, `Body`, `Caption`

## 4. State Architecture
**Rule: Do not allow UI composables to own state.**
Compose becomes almost stateless. Use dedicated state classes like `GuideUiState`, `PlaybackUiState`, `SettingsUiState`, `NavigationUiState`.

## 5. Focus & Animation Management
*   **`FocusAnimator.kt`**: Centralize focus animations. Do not scatter `animateFloatAsState` across the codebase. One single implementation ensures consistency (e.g., exactly a `1.02f` pop).
*   **`TvFocusManager.kt`**: Central responsibility for remembering focus, restoring focus, moving focus, DPAD interception, transitions, and state persistence. Every screen uses it.

## 6. Overlay Management
**Rule: One Stack, One Controller.**
Instead of scattered overlays, use `OverlayManager.kt` to handle all flows: Flow 1, Flow 1A, Flow 2, Flow 3, Playback OSD, Numeric Entry, and Settings.

## 7. Implementation Safeguards & Performance Rules

**Feature Flags (`UiFeatureFlags.kt`)**
Instead of `if (DEBUG)`, centralize feature toggles:
*   Animations Enabled
*   Experimental Guide
*   Debug Focus Overlay
*   Show Layout Bounds
*   Center Pivot Debug

**UI Inspector Rule**
*   Debug builds must expose UI inspection information.
*   Use Layout Inspector and Compose recomposition tools during development.
*   Performance optimizations must remain compatible with debugging.

**No Premature Optimization**
*   Optimize only after measuring.
*   Do not introduce unnecessary complexity for hypothetical performance issues.
*   Prioritize correctness, maintainability, and smooth user experience first.
*   Profile before optimizing. Compose performs extremely well if used correctly; don't over-engineer out of fear of recomposition.

**Strict Performance Rules**
*   Never use nested LazyColumns inside LazyColumns.
*   Keep recompositions localized.
*   Stable models only.
*   Remember expensive calculations.
*   Avoid creating lambdas during recomposition.
*   Prefer immutable UI state.
*   Use `derivedStateOf` where appropriate.
*   Never recreate overlays.
*   Never recreate guide cells unnecessarily.
*   Preserve scroll states across navigation.
*   Preserve focus across overlays.
*   Optimize for smooth 60 FPS DPAD navigation rather than minimizing code.

## 8. Reference Screenshots
Treat the Markdown specifications as the primary implementation source. The screenshots should only be used to validate spacing, proportions, panel widths, typography hierarchy, focus appearance, and animation feel. Whenever the specification and screenshots differ, the specification takes precedence.
