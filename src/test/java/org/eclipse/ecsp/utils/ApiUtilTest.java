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

import org.eclipse.ecsp.constants.Constants;
import org.eclipse.ecsp.entities.UserContext;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import java.util.List;

/**
 * {@link ApiUtilTest} contains test cases for {@link ApiUtils}.
 *
 * @author abhishekkumar
 */
public class ApiUtilTest {
    
    private ApiUtils apiUtils = new ApiUtils();
    
    @Test
    public void getHeadersTest() {
        HttpHeaders response = apiUtils.getHeaders("clientRequestId", "sessionId", "platformResponseId");
        Assert.assertEquals("clientRequestId",
            response.get(Constants.HTTP_HEADER_CLIENT_REQUEST_ID).get(0));
        Assert.assertEquals("sessionId", response.get(Constants.HTTP_HEADER_SESSION_ID).get(0));
        Assert.assertEquals("platformResponseId",
            response.get(Constants.HTTP_HEADER_PLATFORM_RESPONSE_ID).get(0));
    }
    
    @Test
    public void getUserContextTest() {
        List<UserContext> list = apiUtils.getUserContext("userId");
        Assert.assertNotNull(list);
    }
    
    @Test
    public void getHeadersWithoutClientRequestIdTest() {
        HttpHeaders response = apiUtils.getHeaders("", "sessionId", "platformResponseId");
        Assert.assertNull(response.get(Constants.HTTP_HEADER_CLIENT_REQUEST_ID));
        Assert.assertEquals("sessionId", response.get(Constants.HTTP_HEADER_SESSION_ID).get(0));
        Assert.assertEquals("platformResponseId",
            response.get(Constants.HTTP_HEADER_PLATFORM_RESPONSE_ID).get(0));
    }
    
}
