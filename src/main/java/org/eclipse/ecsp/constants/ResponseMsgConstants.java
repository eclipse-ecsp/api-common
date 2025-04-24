/********************************************************************************
 * Copyright (c) 2023-24 Harman International
 * 
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 * 
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and\
 * limitations under the License.
 * 
 * <p>SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.ecsp.constants;

/**
 * Contains constants used to send API response.
 *
 * @author abhishekkumar
 */
public class ResponseMsgConstants {
    
    private ResponseMsgConstants() {
    }
    

    /**
     * INVALID_PARAM_VALUE.
     */
    public static final String INVALID_PARAM_VALUE = "Invalid parameter value : ";

    /**
     * INVALID_PAYLOAD_MSG.
     */
    public static final String INVALID_PAYLOAD_MSG = "Received invalid payload";

    /**
     * INVALID_ACTION_MSG.
     */
    public static final String INVALID_ACTION_MSG = "Received invalid action";

    /**
     * INVALID_DEVICE_ID_MSG.
     */
    public static final String INVALID_DEVICE_ID_MSG = "Received invalid device id in URI";

    /**
     * INVALID_EVENT_ID.
     */
    public static final String INVALID_EVENT_ID = "Invalid Event ID";

    /**
     * COMMAND_FAIL.
     */
    public static final String COMMAND_FAIL = "Failed to receive Command";
}