/*
 * Copyright 2023 sukawasatoru
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.study.accountmanager.client

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Parcelable
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.study.accountmanager.common.model.AccessToken
import com.example.study.accountmanager.contract.AccountContract
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import suspendRunCatching

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        private const val KEY_STATE_DATETIME = "datetime"
        private const val KEY_STATE_ACCOUNT_NAME = "account-name"
    }

    private val vm by viewModels<MainViewModel>()

    private var persistAcrossReboots = false

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        log("[MainActivity][onCreate] acrossReboots(${savedInstanceState != null}, ${persistentState != null})")

        persistAcrossReboots = true

        super.onCreate(savedInstanceState, persistentState)

        // after onCreate(Bundle).

        if (savedInstanceState == null && persistentState != null) {
            // only acrossReboots
            log("[MainActivity][onCreate] acrossReboots restore")

            val datetime = persistentState.getString(KEY_STATE_DATETIME)
            log("[MainActivity][onCreate] acrossReboots restore datetime: $datetime")

            val name = persistentState.getString(KEY_STATE_ACCOUNT_NAME) ?: run {
                vm.restored()
                return
            }

            vm.restoreActivity(name)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        log("[MainActivity][onCreate] (${savedInstanceState != null})")

        super.onCreate(savedInstanceState)

        if (!persistAcrossReboots) {
            vm.restored()
        }

        setContent {
            val accounts by vm.accounts.collectAsState()
            val activeAccount by vm.activeAccount.collectAsState()
            val restoring by vm.restoring.collectAsState()
            val accessToken by vm.activeAccessToken.collectAsState()

            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    MainView(
                        accounts = accounts,
                        activeAccount = activeAccount,
                        accessToken = accessToken,
                        restoring = restoring,
                        addAccount = { vm.addAccount(this) },
                        getAccounts = { vm.getAccounts() },
                        getAuthToken = { vm.getAuthToken() },
                        setActiveAccount = { vm.setActiveAccount(it) },
                        invalidate = { vm.invalidate() },
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        log("[MainActivity][onDestroy]")

        super.onDestroy()
    }

    override fun onStart() {
        log("[MainActivity][onStart]")

        super.onStart()
    }

    override fun onStop() {
        log("[MainActivity][onStop]")

        super.onStop()
    }

    override fun onRestoreInstanceState(
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?,
    ) {
        log("[MainActivity][onRestoreInstanceState] acrossReboots(${savedInstanceState != null}, ${persistentState != null})")

        super.onRestoreInstanceState(savedInstanceState, persistentState)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        log("[MainActivity][onSaveInstanceState] acrossReboots")

        super.onSaveInstanceState(outState, outPersistentState)

        outPersistentState.apply {
            putString(KEY_STATE_DATETIME, Instant.now().toString())

            vm.activeAccount.value?.let {
                putString(KEY_STATE_ACCOUNT_NAME, it.name)
            }
        }
    }

    override fun onResume() {
        log("[MainActivity][onResume]")

        super.onResume()
    }

    override fun onPause() {
        log("[MainActivity][onPause]")

        super.onPause()
    }

    override fun onRestart() {
        log("[MainActivity][onRestart]")

        super.onRestart()
    }
}

@Composable
fun MainView(
    accounts: List<Account>,
    activeAccount: Account?,
    accessToken: AccessToken?,
    restoring: Boolean,
    addAccount: () -> Unit,
    getAccounts: () -> Unit,
    getAuthToken: () -> Unit,
    setActiveAccount: (Account) -> Unit,
    invalidate: () -> Unit,
) {
    Column {
        ProvideTextStyle(MaterialTheme.typography.h1) {
            Text("Client")
        }

        if (restoring) {
            return@Column
        }

        Button(addAccount) { Text("Add account") }
        Button(getAccounts) { Text("Get accounts") }

        ProvideTextStyle(MaterialTheme.typography.subtitle1) {
            Text("Accounts:")
        }
        LazyColumn(
            Modifier
                .fillMaxWidth()
                .height(200.dp)
                .border(1.dp, Color.Black)
        ) {
            items(accounts, { it.name }) { account ->
                ProvideTextStyle(MaterialTheme.typography.button) {
                    val active = account == activeAccount
                    val color = if (active) {
                        Color.White
                    } else {
                        Color.Unspecified
                    }
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clickable {
                                setActiveAccount(account)
                            }
                            .background(
                                if (active) {
                                    MaterialTheme.colors.primarySurface
                                } else {
                                    MaterialTheme.colors.background
                                }
                            )
                    ) {
                        Row {
                            Text(text = "Name: ", color = color)
                            Text(text = account.name, color = color)
                        }
                        Row {
                            Text(text = "Type: ", color = color)
                            Text(text = account.type, color = color)
                        }
                    }
                }
            }
        }

        Button(
            onClick = getAuthToken,
            enabled = activeAccount != null,
        ) {
            Text("Get auth token")
        }
        Button(
            onClick = invalidate,
            enabled = activeAccount != null && accessToken != null,
        ) {
            Text("Invalidate")
        }
    }
}

/**
 * - https://cs.android.com/android/platform/superproject/+/main:cts/tests/tests/accounts/src/android/accounts/cts/AccountManagerTest.java
 */
@HiltViewModel
class MainViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    private val _restoring = MutableStateFlow(true)
    val restoring: StateFlow<Boolean> = _restoring

    private val _accounts = MutableStateFlow<List<Account>>(listOf())
    val accounts: StateFlow<List<Account>> = _accounts

    private val accessTokens = MutableStateFlow(HashMap<Account, AccessToken>())

    private val _activeAccount = MutableStateFlow<Account?>(null)
    val activeAccount: StateFlow<Account?> = _activeAccount

    val activeAccessToken: StateFlow<AccessToken?> =
        accessTokens.combine(activeAccount) { tokens, activeAccount ->
            if (activeAccount == null) {
                null
            } else {
                tokens[activeAccount]
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        log("[MainViewModel][init]")
    }

    override fun onCleared() {
        log("[MainViewModel][onCleared]")
    }

    fun addAccount(activity: Activity) {
        log("[MainViewModel][addAccount]")

        viewModelScope.launch {
            val am = AccountManager.get(getApplication())

            // called `AuthenticatorService.addAccount()` and launched LoginActivity.
            val fut = am.addAccount(
                AccountContract.ACCOUNT_TYPE,
                AccountContract.AUTHTOKEN_TYPE,
                null,
                null,
                activity,
                null,
                null,
            )

            val result = withContext(Dispatchers.IO) {
                suspendRunCatching {
                    fut.result
                }.getOrElse {
                    log("[MainViewModel][addAccount] failed to add account: $it")

                    Bundle()
                }
            }

            // closed LoginActivity.

            val name = result.getString(AccountManager.KEY_ACCOUNT_NAME)
            val type = result.getString(AccountManager.KEY_ACCOUNT_TYPE)
            // KEY_AUTHTOKEN is null always.
            val accessToken = result.getString(AccountManager.KEY_AUTHTOKEN)
            log("[MainViewModel][addAccount] ret name: $name, type: $type, accessToken: $accessToken")
        }
    }

    fun setActiveAccount(account: Account) {
        log("[MainViewModel][setActiveAccount]")

        _activeAccount.value = account
    }

    fun getAccounts() {
        log("[MainViewModel][getAccounts]")

        // can call from UI thread.
        val am = AccountManager.get(getApplication())
        val accounts = am.getAccountsByType(AccountContract.ACCOUNT_TYPE)

        if (accounts.isNotEmpty()) {
            val builder = StringBuilder("[MainViewModel][getAccounts]\n")
            for (account in accounts) {
                builder.append("  ")
                    .append(account)
                    .appendLine()
            }

            log(builder.toString())
        }

        _accounts.value = accounts.toList()

        activeAccount.value?.let { account ->
            val found = accounts.any { it == account }
            if (!found) {
                log("[MainViewModel][getAccounts] unset activeAccount")
                _activeAccount.value = null
            }
        }

        HashMap(accessTokens.value).let { newTokens ->
            var removed = false
            val iter = newTokens.iterator()
            for (entry in iter) {
                if (!accounts.any { it == entry.key }) {
                    log(
                        "[MainViewModel][getAccounts] remove accessToken from" +
                                " ViewModel's cache: ${entry.key.name}"
                    )
                    removed = true
                    iter.remove()
                }
            }
            if (removed) {
                log("[MainViewModel][getAccounts] update ViewModel's accessToken cache")
                accessTokens.value = newTokens
            }
        }
    }

    fun getAuthToken() {
        log("[MainViewModel][getAuthToken]")

        val account = activeAccount.value ?: run {
            log("[MainViewModel][getAuthToken] activeAccount is null")
            return
        }

        val am = AccountManager.get(getApplication())

        // - `android:customTokens="true"` service
        //     - called `AuthenticatorService.getAccounts()` if token expired,
        //       or return immediately if token live.
        // - `android:customTokens="false` service
        //     - called `AuthenticatorService.getAccounts()` always
        //
        // - AccountManager.getAuthToken(Account, String, Bundle?, Activity, AccountManagerCallback?, Handler?) for foreground app.
        // - AccountManager.getAuthToken(Account, String, Bundle?, boolean, AccountManagerCallback?, Handler?) for background app.
        //     - if `notifyAuthFailure = false`, caller app has responsibility to launch activity some point using `AccountManager.KEY_INTENT` intent.
        //
        // can call from UI thread.
        val fut = am.getAuthToken(
            account,
            AccountContract.ACCOUNT_TYPE,
            null,
            false,
            null,
            null,
        )

        viewModelScope.launch {
            val extra = withContext(Dispatchers.IO) {
                suspendRunCatching {
                    fut.result
                }
            }.getOrElse {
                log("[MainViewModel][getAuthToken] failed to get auth token: $it")
                return@launch
            }

            if (extra.containsKey(AccountManager.KEY_INTENT)) {
                log("[MainViewModel][getAuthToken] need to input user credentials")
                return@launch
            }

            val name = extra.getString(AccountManager.KEY_ACCOUNT_NAME)
            val type = extra.getString(AccountManager.KEY_ACCOUNT_TYPE)
            val token = extra.getString(AccountManager.KEY_AUTHTOKEN)
            log("[MainViewModel][getAuthToken] name: $name, type: $type, token: $token")

            accessTokens.value = HashMap(accessTokens.value).let { newTokens ->
                if (token == null) {
                    newTokens.remove(account)
                } else {
                    newTokens[account] = AccessToken(token)
                }
                newTokens
            }
        }
    }

    fun invalidate() {
        log("[MainViewModel][invalidate]")

        val account = activeAccount.value ?: run {
            log("[MainViewModel][invalidate] need to select account")
            return
        }

        val accessToken = activeAccessToken.value ?: run {
            log("[MainViewModel][invalidate] need to retrieve AccessToken")
            return
        }

        // can call from UI thread.
        val am = AccountManager.get(getApplication())
        am.invalidateAuthToken(account.type, accessToken.value)
    }

    fun restoreActivity(activeAccountName: String) {
        log("[MainViewModel][restoreActivity]")

        viewModelScope.launch {
            _restoring.value = true
            try {
                val prevList = accounts.value
                getAccounts()

                // compare reference for wait loading.
                accounts.filter { it !== prevList }
                    .first()

                val account = accounts.value.find { it.name == activeAccountName }
                if (account != null) {
                    _activeAccount.value = account
                }
            } finally {
                log("[MainViewModel][restoreActivity] restored")
                _restoring.value = false
            }
        }
    }

    fun restored() {
        log("[MainViewModel][restored]")

        _restoring.value = false
    }

    private fun <T : Parcelable> getParcelable(bundle: Bundle, key: String, clazz: Class<T>): T? {
        return if (android.os.Build.VERSION_CODES.TIRAMISU <= android.os.Build.VERSION.SDK_INT) {
            bundle.getParcelable(key, clazz)
        } else {
            @Suppress("DEPRECATION")
            bundle.getParcelable(key)
        }
    }
}
