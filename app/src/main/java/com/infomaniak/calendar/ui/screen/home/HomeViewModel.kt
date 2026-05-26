package com.infomaniak.calendar.ui.screen.home

import android.content.Context
import androidx.lifecycle.ViewModel
import com.infomaniak.calendar.di.ViewModelKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey(HomeViewModel::class)
class HomeViewModel(appContext: Context) : ViewModel() {
    val greetings: String = "Greetings from package: ${appContext.packageName}"
}
