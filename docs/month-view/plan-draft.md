# Month view — draft plan (v0, to be challenged)

## What iOS does (/tmp/ios-calendar/CalendarFeatures/CalendarView/MonthView)
- `MonthView`: observes `calendarManager.observeDaySlices` over 3 month grids (prev/current/next), throttled 500 ms,
  keeps a cache of ±2 months of computed layouts. `MonthEventsObserver` is marked `FIXME: This logic should move KMP later`.
- `MonthGrid`: weeks (7 dates each) covering a month for the locale's first weekday.
- `MonthWeekLayout` (pure, "meant to move to KMP"): turns per-day slices of a week row into `MonthEventSegment`
  (event, startColumn, span, lane). Multi-day segments first (by start column, then longest), then single-day in KMP order.
  Greedy lowest free lane. Out-of-month days show NO events (`isDisplayed = grid.isInMonth`).
  Height-dependent part: `visibleSegments(laneCount)` / `hiddenEventCounts(laneCount)` — last lane reserved for "+N"
  on overflowing columns.
- `MonthGridMetrics`: rows fill the height (portrait) or are square + vertical scroll (landscape). laneCount from row height.
- UI: weekday header, `CalendarPeriodPager` horizontal paged months, week rows = day cells (number, today circle + tint,
  out-of-month dimmed 0.45, separators) + events layer (custom Layout by column/span/lane) + "+N" label.
- Tap day → switch to Day mode on that date. Tap event → event detail. Top header: no collapsed calendar, expanded = month picker.
- Optional week-number column (user setting).

## What Android already has
- `VisibleEventsManager` (app-scoped): ONE `observeDaySlices` window around the visible date (loaded -28..+42 d,
  re-windowed when needed -14..+21 d isn't covered). `DayEventsLoader` maps it for day views; Planning maps it too.
- `MonthScreen` + `MonthViewModel` exist as placeholders (fake LazyColumn), nav entry `CalendarView.Month` wired.
- `DayPager` pattern: HorizontalPager over a date range, `selectedDate: () -> LocalDate`, `onVisibleDateChanged`,
  jump handling via `ApplyJumpRequests(visibleDayState)`.
- `WeekNumbering.weeksBetween(start, end)` → `YearWeek`s (grid weeks for free), `rememberToday()`, `DayCircle`/`DateState`,
  `HorizontalMonthSelector` (month picker), `ExpandableCalendarDefaults.None`, `EventColorsUi`, `OccurrenceEventSummaryUi`.
- KMP gives only `Map<LocalDate, List<EventDaySlice>>` (slice: event, date, displayStart/End, position index/count).

## Proposed design

### 1. Component: new module `:CalendarComponents:Month` (flavor-aware plugin; api Foundation, impl Resources)
Pure UI + height-dependent logic only. Input type is the "ideal KMP shape" so KMP can later feed it directly:
- `model/MonthWeek.kt` — `MonthWeek(firstDay: LocalDate, segments: List<MonthEventSegment>)`,
  `MonthEventSegment(event: EventSummaryUi.Normal, startColumn: Int, span: Int, lane: Int)`.
- `layout/MonthWeekOverflow.kt` — pure: `visibleSegments(laneCount)` + `hiddenCounts(laneCount)` (UI concern, stays forever). Unit-tested.
- `MonthPager.kt` — public, mirrors `DayPager`: `MonthPager(monthRange, selectedDate, weeksOf: (YearMonth) -> List<MonthWeek>?,
  weekNumbering, onVisibleDateChanged, onDayClick, onEventClick, contentPadding)`.
- `component/MonthPage.kt` — weeks of one month (rows fill height; min row height + vertical scroll when too small).
- `component/MonthWeekdayHeader.kt`, `component/MonthDayCell.kt` (number, today, out-of-month dim),
  `component/MonthEventsLayer.kt` (custom `Layout` placing chips by column/span/lane), `component/MonthEventChip.kt`,
  `component/MonthOverflowLabel.kt` ("+N"). One feature = one file; row composes them.

### 2. App: temporary "fake KMP" seam (delete when KMP serves month weeks)
- `utils/MonthWeekPackingExt.kt` — `TODO[kmpMonth]`: pure port of iOS `MonthWeekLayout` init:
  `Map<LocalDate, List<EventDaySlice>>.toMonthWeeks(month, weekNumbering, timeZone): List<MonthWeek>`.
  Groups by `occurrenceId`, lane-packs. Unit test in app/src/test.
- `manager/MonthEventsLoader.kt` — app-scoped, mirrors `DayEventsLoader`: `VisibleEventsManager.loadedEvents` →
  `MonthUiState` (`weeksOf(month)`, null while not loaded / outside the loaded window).
  Swap-out later = replace the body of this loader with a mapping of KMP's month API, delete the packing file.
- `VisibleEventsManager`: make windows month-aligned so the month grid ±1 month is always loaded
  (needed = prev month grid start .. next month grid end; loaded = one more month each side). Covers day/week/planning too.

### 3. Screen
- `MonthScreen`/`MonthViewModel`: replace placeholder. VM exposes `uiState` from `MonthEventsLoader` + `isLoadingEvents`.
- Top bar: `CalendarTopAppBar` + `ExpandableCalendar(collapsedContent = None, …)` — or just month title v1 (question).
- Day tap → `visibleDayState.updateVisibleDate(day)` + navigate to `CalendarView.Day` (save as selected view).
- Event tap → `EventDetail(occurrenceId)`. Swipe → visible date = first day of the month (unless already in it).

### 4. Skipped (YAGNI)
- Week-number column (no setting on Android yet; `TODO[weekNumbering]` already everywhere).
- Throttle (StateFlow + mapLatest already coalesce), ±2 month layout cache (loader output is already a map).
- Landscape "square" mode beyond min row height + scroll.

### 5. Docs/CI
- AGENTS.md: new module row, dependency graph row; app/AGENTS.md: MonthEventsLoader, packing seam.
- Strings ("+N", content descriptions) in `CalendarComponents/Resources`. No new deps → no LICENSES change. fdroid: nothing flavor-specific.
