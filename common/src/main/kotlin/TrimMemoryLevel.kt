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

import android.content.ComponentCallbacks2

enum class TrimMemoryLevel {
    Complete,
    Moderate,
    Background,
    UiHidden,
    RunningCritical,
    RunningLow,
    RunningModerate;

    companion object {
        fun from(level: Int): TrimMemoryLevel? {
            return entries.find { it.value == level }
        }
    }

    val value
        get() = when (this) {
            Complete -> ComponentCallbacks2.TRIM_MEMORY_COMPLETE
            Moderate -> ComponentCallbacks2.TRIM_MEMORY_MODERATE
            Background -> ComponentCallbacks2.TRIM_MEMORY_BACKGROUND
            UiHidden -> ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN
            RunningCritical -> ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL
            RunningLow -> ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW
            RunningModerate -> ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE
        }

    override fun toString() = when (this) {
        Complete -> "TRIM_MEMORY_COMPLETE"
        Moderate -> "TRIM_MEMORY_MODERATE"
        Background -> "TRIM_MEMORY_BACKGROUND"
        UiHidden -> "TRIM_MEMORY_UI_HIDDEN"
        RunningCritical -> "TRIM_MEMORY_RUNNING_CRITICAL"
        RunningLow -> "TRIM_MEMORY_RUNNING_LOW"
        RunningModerate -> "TRIM_MEMORY_RUNNING_MODERATE"
    }
}
