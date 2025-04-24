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

package org.eclipse.ecsp.test.controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.eclipse.ecsp.test.domains.DemoEventRequest;
import org.eclipse.ecsp.test.domains.DemoEventResponse;
import org.eclipse.ecsp.test.services.DemoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.Collections;
import java.util.List;

/**
 * test controller to test api metrics.
 *
 * @author abhishekkumar
 */
@RestController
public class DemoEventController {
    private DemoService service = new DemoService();
    
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(value = "/v1.0/demo",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public DemoEventResponse createDemoEvent(@Valid @RequestBody DemoEventRequest project,
                                             HttpServletResponse response) {
        return service.createDemoEventRequest();
    }
    
    @GetMapping(value = "/v1.0/demo/{mustBeLong}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> fetch(@PathVariable("mustBeLong") long mustBeLong) {
        return Collections.emptyList();
    }
}

