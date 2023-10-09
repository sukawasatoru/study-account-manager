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

import com.example.study.accountmanager.common.model.AccessToken
import com.example.study.accountmanager.common.model.RefreshToken
import java.time.Clock
import java.time.Duration
import java.time.Instant
import kotlin.random.Random
import kotlin.random.nextUInt

object ServerUtil {
    fun authenticate(
        clock: Clock,
        userName: String,
        password: String,
    ): Result<Triple<AccessToken, RefreshToken, Instant>> {
        return Result.success(
            Triple(
                AccessToken("access-token-${Random.nextUInt()}"),
                RefreshToken("refresh-token-${Random.nextUInt()}"),
                clock.instant().plus(Duration.ofMinutes(1))
            )
        )
    }

    fun refreshAccessToken(
        clock: Clock,
        refreshToken: RefreshToken,
    ): Result<Pair<AccessToken, Instant>> {
        return if (refreshToken.value.startsWith("refresh-token-")) {
            Result.success(
                AccessToken("access-token-${Random.nextUInt()}") to
                        clock.instant().plus(Duration.ofMinutes(1))
            )
        } else {
            Result.failure(Exception("expired"))
        }
    }
}
