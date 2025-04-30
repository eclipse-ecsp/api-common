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
import io.prometheus.client.exporter.common.TextFormat;
import org.apache.commons.io.output.StringBuilderWriter;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes metrics to prometheus for scraping at /metrics path. This controller
 * is enabled only if metrics.enabled is true
 *
 * @author ssasidharan
 */
@RestController
@ConditionalOnProperty("metrics.enabled")
public class PrometheusMetricsExporter {
    
    private static final IgniteLogger LOGGER =
        IgniteLoggerFactory.getLogger(PrometheusMetricsExporter.class);
    private static final String EMPTY_STR = "";

    /**
     * CAPACITY.
     */
    public static final int CAPACITY = 1024;
    
    /**
     * API to fetch prometheus metrics.
     *
     * @return prometheus metrics
     */
    @GetMapping(path = "/metrics", produces = TextFormat.CONTENT_TYPE_004)
    public String get() {
        LOGGER.debug("Fetching prometheus metrics");
        StringBuilderWriter writer = new StringBuilderWriter(CAPACITY);
        try (writer) {
            TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples());
            writer.flush();
        } catch (Exception e) {
            LOGGER.warn("Could not flush out metrics, sending empty response", e);
            return EMPTY_STR;
        }
        LOGGER.debug("Prometheus metrics export complete");
        return writer.getBuilder().toString();
    }
}