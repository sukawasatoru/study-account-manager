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

import TrimMemoryLevel
import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.accounts.NetworkErrorException
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import com.example.study.accountmanager.common.model.RefreshToken
import com.example.study.accountmanager.contract.AccountContract
import java.time.Clock

/**
 * - [https://developer.android.com/training/id-auth/custom_auth]
 * - [https://developer.android.com/training/sync-adapters/creating-authenticator]
 * - [https://cs.android.com/android/platform/superproject/main/+/main:development/samples/SampleSyncAdapter/]
 * - [https://cs.android.com/android/platform/superproject/+/main:cts/tests/tests/accounts/src/android/accounts/cts/AccountManagerTest.java]
 * - [https://cs.android.com/android/platform/superproject/+/main:cts/tests/tests/accounts/src/android/accounts/cts/MockAccountAuthenticator.java]
 * - [http://www.jssec.org/report/securecoding.html]
 */
class AuthenticatorService : Service() {
    private lateinit var authenticator: AccountAuthenticator

    override fun onCreate() {
        log("[AuthenticatorService][onCreate]")

        super.onCreate()

        authenticator = AccountAuthenticator(application, Clock.systemUTC())
    }

    override fun onDestroy() {
        log("[AuthenticatorService][onDestroy]")

        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("[AuthenticatorService][onStartCommand]")

        super.onStartCommand(intent, flags, startId)

        stopSelf(startId)

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        log("[AuthenticatorService][onBind]")

        return authenticator.iBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        log("[AuthenticatorService][onUnbind]")

        return false
    }

    override fun onTrimMemory(level: Int) {
        log("[AuthenticatorService][onTrimMemory] level: ${TrimMemoryLevel.from(level)}")
    }

    class AccountAuthenticator(
        private val context: Context,
        private val clock: Clock,
    ) : AbstractAccountAuthenticator(context) {
        override fun editProperties(
            response: AccountAuthenticatorResponse?,
            accountType: String?,
        ): Bundle? {
            log("[AccountAuthenticator][editProperties]")
            // TODO:
            throw UnsupportedOperationException()
        }

        @Throws(NetworkErrorException::class)
        override fun addAccount(
            response: AccountAuthenticatorResponse,
            accountType: String,
            authTokenType: String?,
            requiredFeatures: Array<out String>?,
            options: Bundle,
        ): Bundle {
            require(accountType == AccountContract.ACCOUNT_TYPE)

            log("[AccountAuthenticator][addAccount]")
            // this method invoked from client's AccountManager.addAccount().

            log(
                "[AccountAuthenticator][addAccount]" +
                        " packageName: ${options.getString(AccountManager.KEY_ANDROID_PACKAGE_NAME)}," +
                        " pid: ${options.getInt(AccountManager.KEY_CALLER_PID)}," +
                        " uid: ${options.getInt(AccountManager.KEY_CALLER_UID)}"
            )

            if (authTokenType != null && authTokenType != AccountContract.AUTHTOKEN_TYPE) {
                log("[AccountAuthenticator][addAccount] unsupported auth type")

                return Bundle().apply {
                    putInt(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_BAD_ARGUMENTS)
                    putString(AccountManager.KEY_ERROR_MESSAGE, "unsupported 'authTokenType'")
                }
            }

            // after returns Bundle, client's will AccountManager launch LoginActivity.
            return Bundle().apply {
                // use KEY_INTENT to launch LoginActivity.
                //
                // LoginActivity returns Bundle contains the following values:
                // - AccountManager.KEY_ACCOUNT_NAME
                // - AccountManager.KEY_ACCOUNT_TYPE
                // - AbstractAccountAuthenticator.KEY_CUSTOM_TOKEN_EXPIRY
                //     - if set `android:customTokens="true"`
                //
                // LoginActivity will be invoke the following API:
                // - am.addAccountExplicitly()
                // - am.setAuthToken()
                //     - if set `android:customTokens="false"` or undefined

                putParcelable(
                    AccountManager.KEY_INTENT,
                    Intent(context, LoginActivity::class.java)
                        .putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response),
                )
            }
        }

        @Throws(NetworkErrorException::class)
        override fun confirmCredentials(
            response: AccountAuthenticatorResponse,
            account: Account,
            options: Bundle?,
        ): Bundle? {
            log("[AccountAuthenticator][confirmCredentials]")
            // sudo mode.

            // TODO:
            throw UnsupportedOperationException()
        }

        @Throws(NetworkErrorException::class)
        override fun getAuthToken(
            response: AccountAuthenticatorResponse,
            account: Account,
            authTokenType: String,
            options: Bundle,
        ): Bundle? {
            log("[AccountAuthenticator][getAuthToken]")
            // this method invoked from client's AccountManager.getAuthToken()

            log(
                "[AccountAuthenticator][getAuthToken]" +
                        " packageName: ${options.getString(AccountManager.KEY_ANDROID_PACKAGE_NAME)}," +
                        " pid: ${options.getLong(AccountManager.KEY_CALLER_PID)}," +
                        " uid: ${options.getLong(AccountManager.KEY_CALLER_UID)}"
            )

            if (authTokenType != AccountContract.AUTHTOKEN_TYPE) {
                return Bundle().apply {
                    putInt(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_BAD_ARGUMENTS)
                    putString(AccountManager.KEY_ERROR_MESSAGE, "unsupported type of auth token")
                }
            }

            val am = AccountManager.get(context)
            // don't invoke `peekAuthToken()` because this authenticator set
            // `android:customTokens="true"` and return `KEY_CUSTOM_TOKEN_EXPIRY`.
            // don't cause IPC when token live, but IPC cause if invoked `setAuthToken()` always.
            //
            // need to invoke `peekAuthToken()` if set android:customTokens="false"` or undefined.
            @Suppress("ConstantConditionIf")
            if (false) {
                am.peekAuthToken(account, authTokenType)?.let { accessToken ->
                    log("[AccountAuthenticator][getAuthToken] use cached token")
                    return Bundle().apply {
                        putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
                        putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
                        putString(AccountManager.KEY_AUTHTOKEN, accessToken)
                    }
                }
            }

            if (!am.getAccountsByType(AccountContract.ACCOUNT_TYPE).any { it == account }) {
                log("[AccountAuthenticator][getAuthToken] account not exists")

                return Bundle().apply {
                    putInt(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_BAD_REQUEST)
                    putString(AccountManager.KEY_ERROR_MESSAGE, "account not exists")
                }
            }

            log("[AccountAuthenticator][getAuthToken] retrieve new token")

            val refreshToken = am.getUserData(
                account,
                AccountContract.INTERNAL_KEY_USER_DATA_REFRESH_TOKEN,
            )?.let { RefreshToken(it) } ?: return Bundle().apply {
                log("[AccountAuthenticator][getAuthToken] RefreshToken is null. try login")

                // if client invoked `AccountManager.getAuthToken(Account, String, Bundle?, Activity, AccountManagerCallback?, Handler?)`,
                // login activity will launched.
                // if client invoked `AccountManager.getAuthToken(Account, String, Bundle?, false, AccountManagerCallback?, Handler?)`,
                // client should launch login activity using KEY_INTENT's intent.
                putParcelable(
                    AccountManager.KEY_INTENT,
                    Intent(context, LoginActivity::class.java)
                        .putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response),
                )
            }

            val (newAccessToken, expiresIn) = ServerUtil.refreshAccessToken(clock, refreshToken)
                .getOrElse {
                    log("[AccountAuthenticator][getAuthToken] RefreshToken expired. try login")
                    return Bundle().apply {
                        putParcelable(
                            AccountManager.KEY_INTENT,
                            Intent(context, LoginActivity::class.java)
                                .putExtra(
                                    AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
                                    response,
                                ),
                        )
                    }
                }

            // don't invoke `setAuthToken()` because this authenticator set
            // `android:customTokens="true"` and return `KEY_CUSTOM_TOKEN_EXPIRY`.
            // don't cause IPC when token live, but IPC cause if invoked `setAuthToken()` always.
            //
            // need to invoke setAuthToken() and `invalidateAuthToken()` if set
            // android:customTokens="false"` or undefined for `peekAuthToken()`.
            @Suppress("ConstantConditionIf")
            if (false) {
                am.setAuthToken(account, AccountContract.AUTHTOKEN_TYPE, newAccessToken.value)
            }

            return Bundle().apply {
                putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
                putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
                putString(AccountManager.KEY_AUTHTOKEN, newAccessToken.value)
                putLong(KEY_CUSTOM_TOKEN_EXPIRY, expiresIn.toEpochMilli())
            }
        }

        override fun getAuthTokenLabel(authTokenType: String): String? {
            log("[AccountAuthenticator][getAuthTokenLabel]")

            // return null because this authenticator supports 1 types.
            // ref. https://cs.android.com/android/platform/superproject/main/+/main:development/samples/SampleSyncAdapter/src/com/example/android/samplesync/authenticator/Authenticator.java;l=129
            return null
        }

        @Throws(NetworkErrorException::class)
        override fun updateCredentials(
            response: AccountAuthenticatorResponse,
            account: Account,
            authTokenType: String?,
            options: Bundle?,
        ): Bundle? {
            log("[AccountAuthenticator][updateCredentials]")
            // e.g. update RefreshToken.

            // TODO:
            throw UnsupportedOperationException()
        }

        @Throws(NetworkErrorException::class)
        override fun hasFeatures(
            response: AccountAuthenticatorResponse,
            account: Account,
            features: Array<out String>,
        ): Bundle? {
            log("[AccountAuthenticator][hasFeatures]")

            // e.g. an `account`'s token have read/write permissions.

            return if (features.isEmpty()) {
                Bundle().apply {
                    putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true)
                }
            } else {
                Bundle().apply {
                    // no app specific features.
                    putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)
                }
            }
        }

        @Throws(NetworkErrorException::class)
        override fun getAccountRemovalAllowed(
            response: AccountAuthenticatorResponse,
            account: Account,
        ): Bundle? {
            log("[AccountAuthenticator][getAccountRemovalAllowed]")
            return super.getAccountRemovalAllowed(response, account)
        }

        @Throws(NetworkErrorException::class)
        override fun getAccountCredentialsForCloning(
            response: AccountAuthenticatorResponse,
            account: Account,
        ): Bundle? {
            log("[AccountAuthenticator][getAccountCredentialsForCloning]")
            return super.getAccountCredentialsForCloning(response, account)
        }

        @Throws(NetworkErrorException::class)
        override fun addAccountFromCredentials(
            response: AccountAuthenticatorResponse,
            account: Account,
            accountCredentials: Bundle?,
        ): Bundle? {
            log("[AccountAuthenticator][addAccountFromCredentials]")
            return super.addAccountFromCredentials(response, account, accountCredentials)
        }

        @Throws(NetworkErrorException::class)
        override fun startAddAccountSession(
            response: AccountAuthenticatorResponse,
            accountType: String,
            authTokenType: String?,
            requiredFeatures: Array<out String>?,
            options: Bundle?,
        ): Bundle? {
            log("[AccountAuthenticator][startAddAccountSession]")
            return super.startAddAccountSession(
                response, accountType, authTokenType, requiredFeatures, options
            )
        }

        @Throws(NetworkErrorException::class)
        override fun startUpdateCredentialsSession(
            response: AccountAuthenticatorResponse,
            account: Account,
            authTokenType: String?,
            options: Bundle?,
        ): Bundle? {
            log("[AccountAuthenticator][startUpdateCredentialsSession]")
            return super.startUpdateCredentialsSession(response, account, authTokenType, options)
        }

        @Throws(NetworkErrorException::class)
        override fun finishSession(
            response: AccountAuthenticatorResponse,
            accountType: String,
            sessionBundle: Bundle?,
        ): Bundle? {
            log("[AccountAuthenticator][finishSession]")
            return super.finishSession(response, accountType, sessionBundle)
        }

        @Throws(NetworkErrorException::class)
        override fun isCredentialsUpdateSuggested(
            response: AccountAuthenticatorResponse,
            account: Account,
            statusToken: String?,
        ): Bundle? {
            log("[AccountAuthenticator][isCredentialsUpdateSuggested]")
            return super.isCredentialsUpdateSuggested(response, account, statusToken)
        }
    }
}
