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
import android.app.Application
import android.util.Log

class App : Application() {
    override fun onCreate() {
        log("[App][onCreate]")

        super.onCreate()
    }

    override fun onTrimMemory(level: Int) {
        log("[App][onTrimMemory] ${TrimMemoryLevel.from(level)}")

        super.onTrimMemory(level)
    }
}

fun log(msg: String) {
    Log.i("StudyAccountManager", msg)
}
