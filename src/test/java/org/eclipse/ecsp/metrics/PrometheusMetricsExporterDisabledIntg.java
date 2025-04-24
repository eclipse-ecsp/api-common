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
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * for testing prometheus metrics disable scenario.
 *
 * @author abhishekkumar
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:/application-base-metrics-disabled.properties")
public class PrometheusMetricsExporterDisabledIntg extends CommonTestBase {
    private static final IgniteLogger LOGGER =
        IgniteLoggerFactory.getLogger(PrometheusMetricsExporterDisabledIntg.class);

    /**
     * EXPECTED.
     */
    public static final int EXPECTED = 404;
    
    static {
        // Workaround to avoid duplicate metrics registration in case of Spring
        // Boot dev-tools restarts
        CollectorRegistry.defaultRegistry.clear();
    }
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void testMetricsDisabled() {
        new PrometheusTestUtil().clearMetrics();
        // make a call to a non-existent index.html
        restTemplate.getForObject("http://localhost:" + port + "/index.html", String.class);
        // we are expecting registry to be empty
        new PrometheusMetricsRegistryAssertion().assertEmpty();
        // when we scrape metrics we should get 404
        int statusCode =
            restTemplate.getForEntity("http://localhost:" + port + "/metrics", String.class)
                .getStatusCode().value();
        Assert.assertEquals(EXPECTED, statusCode);
    }
}