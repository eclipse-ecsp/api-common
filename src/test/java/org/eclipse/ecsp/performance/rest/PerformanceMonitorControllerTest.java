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

package org.eclipse.ecsp.performance.rest;

import com.codahale.metrics.Counter;
import com.codahale.metrics.DefaultSettableGauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.prometheus.client.CollectorRegistry;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.eclipse.ecsp.metrics.PrometheusTestUtil;
import org.eclipse.ecsp.testutils.EmbeddedRedisServer;
import org.eclipse.ecsp.testutils.MongoServer;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.SortedMap;
import java.util.TreeMap;
import static org.mockito.Mockito.when;

/**
 * Test cases for fetching metrics using GET /metrics api.
 *
 * @author abhishekkumar
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@DirtiesContext
public class PerformanceMonitorControllerTest {

    
    static {
        // Workaround to avoid duplicate metrics registration in case of Spring
        // Boot dev-tools restarts
        CollectorRegistry.defaultRegistry.clear();
        new PrometheusTestUtil().clearMetrics();

    }
    
    @MockitoBean
    private KafkaService kafkaService;

    /**
     * MONGO_SERVER.
     */
    @ClassRule
    public static final MongoServer MONGO_SERVER = new MongoServer();

    @ClassRule
    public static final EmbeddedRedisServer REDIS_SERVER = new EmbeddedRedisServer();

    @Autowired
    private TestRestTemplate restTemplate;
    
    @MockitoBean
    private MetricRegistry metricRegistry;
    
    @Test
    public void testJamonMetrics() {
        when(metricRegistry.getTimers()).thenReturn(prepareMetricRegistryTimer());
        HttpEntity<String> entity = new HttpEntity<String>(new HttpHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            "/v1/jamon-metrics?api=all",
            HttpMethod.GET, entity, String.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
    }
    
    @Test
    //@Ignore
    public void testMetricsReset() {
        when(metricRegistry.getTimers()).thenReturn(prepareMetricRegistryTimer());
        when(metricRegistry.getGauges()).thenReturn(prepareMetricRegistryGauge());
        when(metricRegistry.getCounters()).thenReturn(prepareMetricRegistryCounter());
        
        HttpEntity<String> entity = new HttpEntity<String>(new HttpHeaders());
        
        ResponseEntity<String> response = restTemplate.exchange(
            "/v1/metrics/reset",
            HttpMethod.GET, entity, String.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals("success", response.getBody());
    }
    
    private SortedMap prepareMetricRegistryTimer() {
        SortedMap<String, Timer> timers = new TreeMap<>();
        Timer timer = new Timer();
        timers.put("api=testApt,method=testMethod", timer);
        return timers;
    }
    
    private SortedMap prepareMetricRegistryGauge() {
        SortedMap<String, Metric> gauge = new TreeMap<>();
        gauge.put("api=testApt,method=testMethod", new DefaultSettableGauge());
        return gauge;
    }
    
    private SortedMap prepareMetricRegistryCounter() {
        SortedMap<String, Counter> counter = new TreeMap<>();
        counter.put("api=testApt,method=testMethod", new Counter());
        return counter;
    }
}
