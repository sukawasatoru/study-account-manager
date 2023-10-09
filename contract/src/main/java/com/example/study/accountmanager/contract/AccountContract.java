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

package com.example.study.accountmanager.contract;

public class AccountContract {
    /**
     * This value must be same as:
     * - app/src/main/res/authenticator.xml's android:accountType.
     */
    public static final String ACCOUNT_TYPE = "com.example.study.accountmanager";

    public static final String AUTHTOKEN_TYPE = "com.example.study.accountmanager";

    public static final String INTERNAL_KEY_USER_DATA_REFRESH_TOKEN = "refresh-token";
}
