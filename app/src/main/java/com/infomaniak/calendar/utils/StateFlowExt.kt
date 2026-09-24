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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/*
 * `map` and `combine` turn a StateFlow into a plain Flow, whose first value is only produced after a coroutine dispatch. These
 * extensions recreate standard behaviors but automate the first state initialization based on input state flow's values.
 */

/**
 * A map operation on state flows that returns a new state flow with its initial value based on the initial values of its source
 * state flows.
 */
fun <T, R> StateFlow<T>.mapState(scope: CoroutineScope, started: SharingStarted, transform: (T) -> R): StateFlow<R> {
    return map(transform)
        .stateIn(scope, started, transform(value))
}

/**
 * A combine operation on state flows that returns a new state flow with its initial value based on the initial values of its
 * source state flows.
 */
fun <T1, T2, R> combineStates(
    flow1: StateFlow<T1>,
    flow2: StateFlow<T2>,
    scope: CoroutineScope,
    started: SharingStarted,
    transform: (T1, T2) -> R,
): StateFlow<R> {
    return combine(flow1, flow2, transform)
        .stateIn(scope, started, transform(flow1.value, flow2.value))
}
