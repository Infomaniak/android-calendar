package com.infomaniak.calendar.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infomaniak.calendar.di.ViewModelKey
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Event
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.SortedMap
import kotlin.time.Instant

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey(HomeViewModel::class)
class HomeViewModel(calendarManager: CalendarManager) : ViewModel() {
    val events: StateFlow<SortedMap<YearMonth, SortedMap<Int, List<Event>>>> = calendarManager
        .observeEvents(Instant.DISTANT_PAST, Instant.DISTANT_FUTURE)
        .map(List<Event>::groupByMonthAndDay)
        .stateIn(scope = viewModelScope, started = SharingStarted.Lazily, initialValue = sortedMapOf())
}
