/********************************************************************************
 * Copyright (c) 2023-24 Harman International
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * <p>SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

package com.example.test;

import org.eclipse.ecsp.threadlocal.PlatformThreadLocal;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * rest template example.
 */
@RestController
public class ExampleRestController {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ExampleRestController.class);
    private static final String PLATFORM_ID = "platform-id";

    /**
     * Example GET endpoint.
     *
     * @return a simple greeting message
     */
    @GetMapping("/example")
    public String exampleEndpoint() {
        return "Hello, World!";
    }

    /**
     * Example POST endpoint.
     *
     * @param requestBody the request body
     * @return a message indicating the received data
     */
    @PostMapping("/example")
    public String examplePostEndpoint(@RequestBody String requestBody) {
        return "Received: " + requestBody;
    }

    /**
     * Example PUT endpoint.
     *
     * @param requestBody the request body
     * @return a message indicating the updated data
     */
    @PutMapping("/example")
    public String examplePutEndpoint(@RequestBody String requestBody) {
        return "Updated: " + requestBody;
    }

    /**
     * Example DELETE endpoint.
     *
     * @return a message indicating the deletion
     */
    @DeleteMapping("/example")
    public String exampleDeleteEndpoint() {
        return "Deleted";
    }

    @GetMapping("/test/platform-id")
    public String testEndpoint(@RequestHeader(name = PLATFORM_ID, required = false) String platformId) {
        LOGGER.debug("Received platform ID: {}", platformId);
        return PlatformThreadLocal.getPlatformId() != null ? platformId : "No Platform ID found";
    }
}
