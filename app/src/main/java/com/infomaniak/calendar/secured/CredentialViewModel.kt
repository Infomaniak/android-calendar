package com.infomaniak.calendar.secured

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infomaniak.calendar.di.ViewModelKey
import com.infomaniak.calendar.utils.AccountUtils
import com.infomaniak.multiplatform_calendar.core.domain.model.account.AccountId
import com.infomaniak.multiplatform_calendar.core.managers.AccountManager
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class CredentialViewModel(
    private val accountManager: AccountManager,
    private val calendarManager: CalendarManager,
    private val securedDavCredentialsRepository: SecuredDavCredentialsRepository,
    private val accountUtils: AccountUtils,
) : ViewModel() {

    /**
     * Tracks user IDs that have already been initialized during this session,
     * preventing redundant init and sync calls when the user list changes.
     */
    private val initializedUserIds = mutableSetOf<Long>()

    init {
        loadDavCredential()
    }

    private fun loadDavCredential() {
        viewModelScope.launch {
            accountUtils.users.collect { userList ->
                val currentUserIds = userList.map { it.id.toLong() }.toSet()
                initializedUserIds.retainAll(currentUserIds)

                userList
                    .filter { user -> user.id.toLong() !in initializedUserIds }
                    .forEach { user ->
                        Log.e("nicolas", "loadDavCredential - user: ${user}")
                        val userId = user.id.toLong()
                        val credentials = securedDavCredentialsRepository.get(userId)
                        val accountId = AccountId(userId)

                        accountManager.initAccount(accountId, credentials)
                        calendarManager.syncCalendars(accountId)

                        initializedUserIds.add(userId)
                    }
            }
        }
    }
}
