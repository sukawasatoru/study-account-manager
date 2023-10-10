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

package com.example.study.accountmanager.authenticator

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import com.example.study.accountmanager.contract.AccountContract
import java.lang.ref.WeakReference
import java.time.Clock
import kotlin.random.Random
import kotlin.random.nextUInt

class LoginActivity : ComponentActivity() {
    private val backCb by lazy { BackKeyCallback(this) }

    private var res: AccountAuthenticatorResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        log("[LoginActivity][onCreate] (${savedInstanceState != null})")

        super.onCreate(savedInstanceState)


        @Suppress("DEPRECATION")
        res = intent.getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)

        // refer AccountAuthenticatorActivity.
        res?.onRequestContinued() ?: run {
            log("[LoginActivity][onCreate] res == null")
        }

        // - AccountManager.getAuthToken(Account, String, Bundle?, Activity, AccountManagerCallback?, Handler?) for foreground app.
        // - AccountManager.getAuthToken(Account, String, Bundle?, boolean, AccountManagerCallback?, Handler?) for background app.
        //     - if `notifyAuthFailure = false`, caller app has responsibility to launch activity some point using `AccountManager.KEY_INTENT` intent.

        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    Column {
                        ProvideTextStyle(MaterialTheme.typography.h1) {
                            Text("Login")
                        }
                        Button(::login) { Text("Login") }
                        Button(::cancel) { Text("Cancel") }
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, backCb)
    }

    override fun onDestroy() {
        log("[LoginActivity][onDestroy]")

        super.onDestroy()
    }

    override fun onStart() {
        log("[LoginActivity][onStart]")

        super.onStart()
    }

    override fun onStop() {
        log("[LoginActivity][onStop]")

        super.onStop()
    }

    override fun onResume() {
        log("[LoginActivity][onResume]")

        super.onResume()
    }

    override fun onPause() {
        log("[LoginActivity][onPause]")

        super.onPause()
    }

    override fun onRestart() {
        log("[LoginActivity][onRestart]")

        super.onRestart()
    }

    private fun login() {
        log("[LoginActivity][login]")
        val am = AccountManager.get(this)
        val name = "name-${Random.nextUInt()}"
        val password = "password"

        val (accessToken, refreshToken, expiresIn) = ServerUtil.authenticate(
            Clock.systemUTC(),
            name,
            password,
        ).getOrElse {
            log("[LoginActivity][login] failed to login. please retry")
            return
        }

        val account = Account(name, AccountContract.ACCOUNT_TYPE)
        am.addAccountExplicitly(
            account,
            null,
            Bundle().apply {
                // for AccountManager.getUserData().
                putString(
                    AccountContract.INTERNAL_KEY_USER_DATA_REFRESH_TOKEN,
                    refreshToken.value,
                )
            },
        )

        // don't invoke `setAuthToken()` because this authenticator set
        // `android:customTokens="true"` and return `KEY_CUSTOM_TOKEN_EXPIRY`.
        //
        // need to invoke setAuthToken() and `invalidateAuthToken()` if set
        // android:customTokens="false"` or undefined.
        @Suppress("ConstantConditionIf")
        if (false) {
            am.setAuthToken(
                account,
                AccountContract.AUTHTOKEN_TYPE,
                accessToken.value,
            )
        }

        val intent = Intent()
            .putExtra(AccountManager.KEY_ACCOUNT_NAME, account.name)
            .putExtra(AccountManager.KEY_ACCOUNT_TYPE, account.type)
            // dont need KEY_AUTHTOKEN.
            // .putExtra(AccountManager.KEY_AUTHTOKEN, accessToken.value)
            .putExtra(
                AbstractAccountAuthenticator.KEY_CUSTOM_TOKEN_EXPIRY,
                expiresIn.toEpochMilli(),
            )

        res?.onResult(intent.extras)

        setResult(RESULT_OK, intent)
        finish()

        // help gc.
        res = null
    }

    private fun cancel() {
        log("[LoginActivity][cancel]")

        res?.onError(AccountManager.ERROR_CODE_CANCELED, "cancelled")

        setResult(RESULT_CANCELED)
        finish()

        // help gc.
        res = null
    }

    private class BackKeyCallback(activity: LoginActivity) : OnBackPressedCallback(true) {
        private val activity = WeakReference(activity)

        override fun handleOnBackPressed() {
            activity.get()?.cancel()
        }
    }
}
