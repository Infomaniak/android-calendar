/*
 * Infomaniak Calendar - Android
 * Copyright (C) 2026 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.calendar.utils

import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/*
 * `map` and `combine` turn a StateFlow into a plain Flow, whose first value is only produced after a coroutine dispatch, and
 * `stateIn` needs an initial value that can already be stale. These derived StateFlows instead compute their `value` on demand
 * from their sources, so they're always as ready as their sources are, without any scope or initial value.
 */

/**
 * A map operation on state flows that returns a new state flow with its values always computed at each call so there's no need
 * for a default initial state. The first value is always up to date.
 */
fun <T, R> StateFlow<T>.mapState(transform: (T) -> R): StateFlow<R> {
    return DerivedStateFlow(getValue = { transform(value) }, updates = map(transform))
}

/**
 * A combine operation on state flows that returns a new state flow with its values always computed at each call so there's no
 * need for a default initial state. The first value is always up to date.
 */
fun <T1, T2, R> combineStates(flow1: StateFlow<T1>, flow2: StateFlow<T2>, transform: (T1, T2) -> R): StateFlow<R> {
    return DerivedStateFlow(getValue = { transform(flow1.value, flow2.value) }, updates = combine(flow1, flow2, transform))
}

/**
 * Acts as a view that derives the value computation at each call based on the input instructions.
 */
@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
private class DerivedStateFlow<T>(private val getValue: () -> T, private val updates: Flow<T>) : StateFlow<T> {
    override val value: T get() = getValue()
    override val replayCache: List<T> get() = listOf(value)

    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        updates.distinctUntilChanged().collect(collector)
        awaitCancellation()
    }
}
