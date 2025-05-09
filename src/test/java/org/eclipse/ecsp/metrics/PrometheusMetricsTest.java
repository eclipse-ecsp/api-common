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

package org.eclipse.ecsp.metrics;

import io.prometheus.client.CollectorRegistry;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * testing prometheus metrics endpoint.
 *
 * @author abhishekkumar
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = "classpath:/application-base.properties")
@DirtiesContext
public class PrometheusMetricsTest extends CommonTestBase {
    static {
        // Workaround to avoid duplicate metrics registration in case of Spring
        // Boot dev-tools restarts
        CollectorRegistry.defaultRegistry.clear();
        new PrometheusTestUtil().clearMetrics();
    }
    
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    
    @Test
    public void testMetrics() throws Exception {

        mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
        String response = mockMvc.perform(get("/metrics"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        System.out.println("Response: " + response);
        mockMvc.perform(get("/metrics"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string(containsString("# TYPE rest_processing_duration_seconds histogram")));

    }
}
