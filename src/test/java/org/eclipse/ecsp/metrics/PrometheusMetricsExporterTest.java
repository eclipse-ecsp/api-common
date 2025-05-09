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
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Test prometheus metrics api.
 *
 * @author abhishekkumar
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:/application-base.properties")
@DirtiesContext
public class PrometheusMetricsExporterTest extends CommonTestBase {
    private static final IgniteLogger LOGGER =
        IgniteLoggerFactory.getLogger(PrometheusMetricsExporterTest.class);
    public static final int TIMEOUT = 10;

    static {
        // Workaround to avoid duplicate metrics registration in case of Spring
        // Boot dev-tools restarts
        CollectorRegistry.defaultRegistry.clear();
    }

    @LocalServerPort
    private int port;
    
    @Autowired
    private Environment env;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void testMetricsCaptureAndExport() throws InterruptedException {
        new PrometheusTestUtil().clearMetrics();
        // make a call to a non-existent index.html
        restTemplate.getForObject("http://localhost:" + port + "/index.html", String.class);
        String nodeName = env.getProperty("node.name");
        Assert.assertNotNull(nodeName);
        LOGGER.info("Node name {}", nodeName);
        CountDownLatch latch = new CountDownLatch(1);
        if (!latch.await(TIMEOUT, TimeUnit.MILLISECONDS)) {
            LOGGER.error("Latch timed out");
        }
        // now scrape the metrics time and validate the same in the scraped text
        String metrics =
            restTemplate.getForObject("http://localhost:" + port + "/metrics", String.class);
        Assert.assertNotNull(metrics);

        Assert.assertTrue(
            metrics.contains(
                "api_requests_total{method=\"GET\",node=\"" + nodeName + "\",} 2.0"));
        Assert.assertTrue(metrics.contains(
            "api_request_processing_duration_seconds_count{method=\"GET\",node=\"" + nodeName
                + "\",} 1.0"));
        // now scrape the metrics second time and validate the same in the scraped text
        String metricsSecond =
                restTemplate.getForObject("http://localhost:" + port + "/metrics", String.class);
        Assert.assertNotNull(metrics);
        // node=localhost as defined in
        // src/test/resources/application-base.properties
        // 2.0 because 1 hit for index.html and 1 for /metrics
        Assert.assertTrue(
                metricsSecond.contains(
                        "api_requests_total{method=\"GET\",node=\"" + nodeName + "\",} 3.0"));
        Assert.assertTrue(metricsSecond.contains(
                "api_request_processing_duration_seconds_count{method=\"GET\",node=\"" + nodeName
                        + "\",} 2.0"));
    }
}
