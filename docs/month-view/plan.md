# Month view — final plan (after 3-way challenge)

Challengers: YAGNI minimalist, KMP-boundary architect, Compose perf/UX skeptic. Draft: plan-draft.md.

## Principles
- Components never see KMP; KMP never sees UI types. One app adapter in between (like `EventDaySliceExt`).
- The temporary "fake KMP" lives in ONE app package and is deleted when KMP ships a month API.
- Month's data loading is Month's own: removing the Month view must not change what Day/Week/Planning load.
- A feature with behaviour = its own file + a connector line. One-line labels are not features.

## 1. `:CalendarComponents:Month` (new module, one module per calendar view like Day/Planning)
Flavor-aware plugin; `api` Foundation, `implementation` Event + Resources. No KMP, no app types.

| File | Responsibility |
|---|---|
| `model/MonthWeekUi.kt` | `MonthWeekUi(firstDay, segments)`, `MonthSegmentUi(key, event: EventSummaryUi.Normal, startColumn, span, lane)` — height-independent |
| `MonthPager.kt` | Public entry. `HorizontalPager` over months of `NAVIGABLE_DATES`; follows `selectedDate`, reports ONE committed month (keeps the day if still in that month, else 1st), suppresses reports during programmatic jumps (DayPager's `travellingTo` idea, minus its initial-emit report) |
| `component/MonthPage.kt` | Weekday header + week rows of a month (`WeekNumbering.weeksBetween`). Rows fill height; below a font-scaled minimum row height → vertical scroll. Bottom content padding for the floating toolbar |
| `component/MonthWeekRow.kt` | Composes day cells + events layer; computes `laneCount` from scaled typography |
| `component/MonthDayCell.kt` | Day number (DayCircle), click → `onDayClick(date)`, one a11y node: full date + event count |
| `feature/TodayHighlight.kt` | `Modifier.todayHighlight(isToday)` (tint + circle state) |
| `feature/OutsideMonthDimming.kt` | `Modifier.outsideMonthDimming(isInMonth)` |
| `component/MonthEventsLayer.kt` | Custom `Layout` placing chips by logical column/span/lane with `placeRelative` (RTL) |
| `component/MonthEventChip.kt` | Title-only chip over `EventItemCard` colors/status (not full `EventItem`) |
| `feature/MonthOverflow.kt` | Pure `visibleSegments/hiddenCounts(laneCount)` computed together across spans + its "+N" label (tap → day). Removing it = layer shows `lane < laneCount` |

Tests (module `src/test`): overflow with 0/1 lanes, exact fit, a spanning bar crossing an overflowing column.

## 2. App — `ui/screen/month/` (everything Month-specific stays here)
| File | Responsibility | On KMP month API |
|---|---|---|
| `MonthUiState.kt` | `pages: Map<YearMonth, List<MonthWeekUi>>`, `weeksOf(month)`; absent = loading, present+empty = loaded-empty | keep |
| `MonthEventsLoader.kt` | Plain `@Inject` class (not app-scoped): `observe(months: Flow<ClosedRange<YearMonth>>, weekNumbering, timeZone): Flow<MonthUiState>`. Own `observeDaySlices` over grid of current ±1 month, re-windowed only when outside; publishes a page only when its grid range is covered | replace body with KMP call + mapper |
| `LocalMonthWeekPacking.kt` | `// TODO[kmpMonth]` pure port of iOS `MonthWeekLayout`: clip to `displayEventDays` BEFORE packing, group consecutive slices by `event.occurrenceId`, multi-day (by clipped span) first, then KMP per-day order, greedy lowest lane, stable keys. Runs on Default, cooperative cancellation | delete |
| `MonthViewModel.kt` | months flow from `VisibleDateManager` (yearMonth, distinct) → loader → `stateIn` | keep |
| `MonthScreen.kt` | Replace placeholder: `CalendarTopAppBar` whose calendar slot toggles `HorizontalMonthSelector` (iOS: collapsed none / expanded month picker), `ApplyJumpRequests`, `MonthPager` | keep |

Test: `app/src/test/.../LocalMonthWeekPackingTest.kt` — contiguous runs, week/month clipping, ordering, midnight-exclusive end, DST day. Doubles as the contract fixture for the future KMP API.

## 3. Connectors
- `MainNavHost`: `MonthScreen(goToEventDetail, goToDay)`; `goToDay` reuses the existing view-switch operation (replace root + `onCalendarViewSelected`) after updating the visible date.
- `settings.gradle.kts` include, app `build.gradle.kts` dependency.
- Strings ("+N" content description plural, a11y) in `CalendarComponents/Resources`. No new deps (LICENSES untouched), nothing fdroid-specific.
- AGENTS.md: module table + dependency graph; app/AGENTS.md: month package and the `TODO[kmpMonth]` seam.

## 4. Proposed future KMP API (for the KMP team)
`observeMonthWeeks(month, firstDayOfWeek, timeZone, displayEventDays): Flow<Map<LocalDate /*week start*/, EventWeekLayout>>`,
`EventWeekLayout.segments = Event + startColumn + span + lane`. Same weeks can differ between adjacent pages → keyed per page.

## 5. Deliberately skipped
- Week-number column — no setting yet (`TODO[weekNumbering]`); add with the setting.
- Throttle / ±2-month layout cache — add if traces show burst work.
- iOS landscape "square" mode — min row height + scroll covers it.

## 6. Pre-existing gap surfaced (not Month-specific)
`SyncEventsManager.loadCurrentMonths()` only runs on user change (MainViewModel.kt:87-100): navigating far away shows
only already-synced events in every view. Month makes it more visible. Separate ticket.

## Delivery order (small PRs)
1. Packing + its test (app, pure) — reviewable alone.
2. Month module: model + overflow + layer/chip + page/row + pager with previews.
3. Screen/VM/loader + nav connectors + header selector + docs.
