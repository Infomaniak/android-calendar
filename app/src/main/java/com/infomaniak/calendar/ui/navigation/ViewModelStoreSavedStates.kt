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
package com.infomaniak.calendar.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.SavedState
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.read
import androidx.savedstate.savedState

@Composable
fun rememberViewModelStoreSavedStates(): ViewModelStoreSavedStates {
    return rememberSaveable(saver = ViewModelStoreSavedStates.Saver) { ViewModelStoreSavedStates() }
}

/**
 * The saved state of each entry's ViewModelStore, kept until the entry is popped rather than while it is composed: a child entry
 * writes to its parent's ViewModels while the parent is not composed, and may be the first to create them after a process death.
 */
class ViewModelStoreSavedStates private constructor(private val restoredStates: MutableMap<String, SavedState>) {
    private val owners = mutableMapOf<String, StoreSavedStateRegistryOwner>()

    constructor() : this(mutableMapOf())

    fun ownerOf(contentKey: Any): SavedStateRegistryOwner {
        val key = contentKey.toString()
        return owners.getOrPut(key) { StoreSavedStateRegistryOwner(restoredStates.remove(key)) }
    }

    fun remove(contentKey: Any) {
        val key = contentKey.toString()
        owners.remove(key)
        restoredStates.remove(key)
    }

    private fun save(): SavedState = savedState {
        restoredStates.forEach { (key, state) -> putSavedState(key, state) }
        owners.forEach { (key, owner) -> putSavedState(key, owner.save()) }
    }

    private class StoreSavedStateRegistryOwner(restoredState: SavedState?) : SavedStateRegistryOwner {
        private val controller = SavedStateRegistryController.create(this)

        override val lifecycle: Lifecycle
            field = LifecycleRegistry.createUnsafe(this)

        override val savedStateRegistry: SavedStateRegistry
            get() = controller.savedStateRegistry

        init {
            controller.performRestore(restoredState)
        }

        fun save(): SavedState = savedState().also(controller::performSave)
    }

    companion object {
        val Saver = Saver<ViewModelStoreSavedStates, SavedState>(
            save = { it.save() },
            restore = { saved ->
                val states = saved.read { toMap().keys.associateWith { getSavedState(it) } }
                ViewModelStoreSavedStates(states.toMutableMap())
            },
        )
    }
}
