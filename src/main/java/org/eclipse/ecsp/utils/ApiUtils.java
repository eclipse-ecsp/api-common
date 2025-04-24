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

package org.eclipse.ecsp.utils;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.constants.Constants;
import org.eclipse.ecsp.entities.UserContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * {@link ApiUtils} contains utility methods to APIs.
 *
 * @author abhishekkumar
 */
@Component
public class ApiUtils {
    
    @Value("${vehicle.owner.role:VO}")
    private String vehicleOwnerRole;
    
    /**
     * This method is used to prepare {@link HttpHeaders} object with provided values.
     *
     * @param clientRequestId    values for header ClientRequestId in {@link HttpHeaders}
     * @param sessionId          values for header SessionId in {@link HttpHeaders}
     * @param platformResponseId values for header PlatformResponseId in {@link HttpHeaders}
     * @return {@link HttpHeaders}
     */
    public static HttpHeaders getHeaders(String clientRequestId, String sessionId,
                                         String platformResponseId) {
        HttpHeaders responseHeaders = new HttpHeaders();
        if (StringUtils.isNoneBlank(clientRequestId)) {
            responseHeaders.set(Constants.HTTP_HEADER_CLIENT_REQUEST_ID, clientRequestId);
        }
        responseHeaders.set(Constants.HTTP_HEADER_SESSION_ID, sessionId);
        responseHeaders.set(Constants.HTTP_HEADER_PLATFORM_RESPONSE_ID, platformResponseId);
        return responseHeaders;
    }
    
    /**
     * This method is used to create list of {@link UserContext}.<br/>
     * with provided userId and role defined in vehicle.owner.role property
     *
     * @param userId user id to be added in UserContext
     * @return list of {@link UserContext}
     */
    public List<UserContext> getUserContext(String userId) {
        UserContext userContext = new UserContext();
        userContext.setUserId(userId);
        userContext.setRole(vehicleOwnerRole);
        return List.of(userContext);
    }
}