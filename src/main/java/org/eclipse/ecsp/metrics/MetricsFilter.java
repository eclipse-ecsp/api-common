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


import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import java.io.IOException;

/**
 * Servlet filter that reports metrics. The following metrics are reported
 *
 * <ul>
 * <li>api_requests_total - Counter for requests</li>
 * <li>api_processing_duration_seconds - Histogram for API processing duration
 * in seconds</li>
 * <li>api_inprogress_requests - Gauge for number of requests being served at
 * this instant</li>
 * </ul>
 *
 * <p>The reason to use a filter is so that Spring overhead gets included as well
 *
 * <p>All of these metrics are reported with the following labels
 * <ul>
 * <li>service - name of the micro-service</li>
 * <li>api - request uri</li>
 * <li>method - http method</li>
 * <li>node - node name</li>
 * </ul>
 *
 * <p>To enable this filter, set metrics.enabled to true.
 *
 * <p>This filter does not support init config. Admittedly ugly. But it is expected
 * to be configured via java config.
 *
 * @author ssasidharan
 */
public class MetricsFilter implements Filter {
    
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(MetricsFilter.class);
    
    private boolean metricsEnabled;
    
    private String nodeName;
    
    private double[] apiProcessingDurationBuckets;
    
    private Counter requestsCounter;
    private Histogram latencyHisto;
    private Gauge inProgressRequests;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        if (metricsEnabled) {
            String method = ((HttpServletRequest) request).getMethod();
            inProgressRequests.labels(method, nodeName).inc();
            requestsCounter.labels(method, nodeName).inc();
            try (Histogram.Timer t = latencyHisto.labels(method, nodeName).startTimer()) {
                chain.doFilter(request, response);
            }
            inProgressRequests.labels(method, nodeName).dec();
        } else {
            chain.doFilter(request, response);
        }
    }
    
    @Override
    public void init(FilterConfig fc) throws ServletException {
        LOGGER.info("metricsEnabled: {}", metricsEnabled);
        LOGGER.info("apiProcessingDurationBuckets: {}", apiProcessingDurationBuckets);
        if (metricsEnabled) {
            String[] labelNames = new String[] {"method", "node"};
            requestsCounter = Counter.build().name("api_requests_total").help("Counter for api requests")
                .labelNames(labelNames)
                .register();
            latencyHisto = Histogram.build().name("api_request_processing_duration_seconds")
                .help("API request processing duration in seconds (incl Spring)")
                .buckets(apiProcessingDurationBuckets)
                .labelNames(labelNames)
                .register();
            inProgressRequests = Gauge.build().name("api_inprogress_requests")
                .help("Number of requests being served at this instant")
                .labelNames(labelNames)
                .register();
        }
    }

    /**
     * This method is a getter for nodeName.
     *
     * @return String
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * This method is a setter for nodeName.
     *
     * @param nodeName : String
     */
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * This method is a getter for api processing duration buckets.
     *
     * @return double
     */
    public double[] getApiProcessingDurationBuckets() {
        return apiProcessingDurationBuckets.clone();
    }

    /**
     * This method is a setter for api processing histogramBuckets.
     *
     * @param histogramBuckets : double
     */
    public void setApiProcessingDurationBuckets(double[] histogramBuckets) {
        this.apiProcessingDurationBuckets = histogramBuckets.clone();
    }

    /**
     * This method is a getter for metricsEnabled.
     *
     * @return boolean
     */
    public boolean isMetricsEnabled() {
        return metricsEnabled;
    }

    /**
     * This method is a setter for metricsEnabled.
     *
     * @param metricsEnabled : boolean
     */
    public void setMetricsEnabled(boolean metricsEnabled) {
        this.metricsEnabled = metricsEnabled;
    }
}