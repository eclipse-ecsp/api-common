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

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.CollectorRegistry;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.junit.Assert;
import java.util.Enumeration;

/**
 * testing all available metrics sample in prometheus.
 *
 * @author abhishekkumar
 */
public class PrometheusMetricsRegistryAssertion {
    
    private static final IgniteLogger LOGGER =
        IgniteLoggerFactory.getLogger(PrometheusMetricsRegistryAssertion.class);

    /**
     * INT_2.
     */
    public static final int INT_2 = 2;

    /**
     * INT_3.
     */
    public static final int INT_3 = 3;

    /**
     * INT_4.
     */
    public static final int INT_4 = 4;
    
    public void assertCaptured() {
        assertCaptured("localhost");
    }
    
    /**
     * assert samples which contains provided node name.
     *
     * @param nodeName to check the metrics sample
     */
    public void assertCaptured(String nodeName) {
        // check the prometheus default registry
        Enumeration<MetricFamilySamples> allSamples =
            CollectorRegistry.defaultRegistry.metricFamilySamples();
        boolean foundTotal = false;
        boolean foundHistogram = false;
        while (allSamples.hasMoreElements()) {
            MetricFamilySamples samples = allSamples.nextElement();
            LOGGER.info("Metric name {}", samples.name);
            if ("api_requests".equals(samples.name)
                && "api_requests_total".equals(samples.samples.get(0).name)) {
                Sample sample = samples.samples.get(0);
                Assert.assertEquals(1.0, sample.value, 0.0D);
                Assert.assertEquals(nodeName, sample.labelValues.get(1));
                Assert.assertEquals("GET", sample.labelValues.get(0));
                foundTotal = true;
            } else if (samples.name.equals("api_request_processing_duration_seconds")) {
                Sample sample = samples.samples.get(0);
                Assert.assertEquals(1.0, sample.value, 0.0D);
                Assert.assertEquals(nodeName, sample.labelValues.get(1));
                Assert.assertEquals("GET", sample.labelValues.get(0));
                foundHistogram = true;
            }
        }
        Assert.assertTrue(foundTotal);
        Assert.assertTrue(foundHistogram);
        
    }
    
    /**
     * test empty metrics samples.
     */
    public void assertEmpty() {
        Enumeration<MetricFamilySamples> allSamples =
            CollectorRegistry.defaultRegistry.metricFamilySamples();
        int count = 0;
        while (allSamples.hasMoreElements()) {
            count++;
            MetricFamilySamples samples = allSamples.nextElement();
            LOGGER.info("Metric name {}", samples.name);
            if (samples.name.equals("service_health_metric")
                || samples.name.equals("diagnostic_metric")
                || samples.name.equals("error_count")) {
                count--;
            }
        }
        Assert.assertEquals(0, count);
    }
    
    /**
     * assert error metrics samples.
     *
     * @param nodeName to check the metrics sample
     */
    public void assertError(String nodeName) {
        // check the prometheus default registry
        Enumeration<MetricFamilySamples> allSamples =
            CollectorRegistry.defaultRegistry.metricFamilySamples();
        boolean foundError = false;
        while (allSamples.hasMoreElements()) {
            MetricFamilySamples samples = allSamples.nextElement();
            if (samples.name.equals("errors")) {
                for (Sample sample : samples.samples) {
                    if (sample.labelValues.get(0).equals("undefined") && "errors_total".equals(sample.name)) {
                        Assert.assertEquals(1.0, sample.value, 0.0D);
                        Assert.assertEquals(nodeName, sample.labelValues.get(INT_2));
                        Assert.assertEquals("GET", sample.labelValues.get(1));
                        Assert.assertEquals("500", sample.labelValues.get(INT_3));
                        Assert.assertEquals("IllegalStateException", sample.labelValues.get(INT_4));
                        foundError = true;
                    }
                }
            }
        }
        Assert.assertTrue(foundError);
    }
    
    public void assertError() {
        assertError("localhost");
    }
}