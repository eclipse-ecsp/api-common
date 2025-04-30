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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import io.prometheus.client.Histogram;
import io.prometheus.client.hotspot.DefaultExports;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import java.util.List;

/**
 * {@link PerformanceMonitorAspect} and histogram of the api request with the processing duration.
 *
 * @author abhishekkumar
 */
@Aspect
@Component
public class PerformanceMonitorAspect {
    
    private static final IgniteLogger LOGGER =
        IgniteLoggerFactory.getLogger(PerformanceMonitorAspect.class);

    /**
     * Initialize {@link PerformanceMonitorAspect}.
     */
    public PerformanceMonitorAspect() {
        LOGGER.info("Instantiated");
    }

    private MetricRegistry registry;

    private HttpServletRequest request;

    @Value("${performance.pointcut.expression:execution(* org.eclipse.ecsp..*.*(..))}")
    private String pointcutExpression;

    @Value("${performance.monitoring.enabled}")
    private boolean legacyPerformanceMonitoringEnabled;
    @Value("${metrics.enabled}")
    private boolean newAgeMetricsEnabled;
    private Histogram latencyHisto;
    @Value("#{'${processing.duration.buckets:0.05,0.1,0.2,0.3,0.4,0.7,1,2.5,5,10}'.split(',')}")
    private double[] apiProcessingDurationBuckets;
    @Value("${node.name:undefined}")
    private String nodeName;
    
    private final ThreadLocal<String> currentApi = new ThreadLocal<>();

    /**
     * Constructor for {@link PerformanceMonitorAspect}.
     *
     * @param registry MetricRegistry
     * @param request HttpServletRequest
     */
    @Autowired
    public PerformanceMonitorAspect(MetricRegistry registry, HttpServletRequest request) {
        this.registry = registry;
        this.request = request;
    }

    /**
     * register and export histogram of the api request with the processing duration.
     */
    @PostConstruct
    public void init() {
        LOGGER.info("metrics enabled: {}", newAgeMetricsEnabled);
        if (newAgeMetricsEnabled) {
            LOGGER.info("Initializing default exports for prometheus");
            DefaultExports.initialize();
            LOGGER.info("apiProcessingDurationBuckets: {}", List.of(apiProcessingDurationBuckets));
            String[] labelNames = new String[] {"api", "method", "node"};
            latencyHisto = Histogram.build().name("rest_processing_duration_seconds")
                .help("REST api processing duration in seconds (excl Spring)")
                .buckets(apiProcessingDurationBuckets)
                .labelNames(labelNames)
                .register();
        }
    }
    
    /**
     * adding api metrics to the api request duration histogram.<br/>
     * checks if the class has {@link RestController} annotation and<br/>
     * and for legacy api signature checks for name of the class with <b>Controller</b><br/>
     * it calculate the time taken to complete the execution.
     *
     * @param jp ProceedingJoinPoint which contains class signature, method signature and arg etc.
     * @return object which is return by actual method call
     * @throws Throwable if any error occurs
     */
    @SuppressWarnings("unchecked")

    @Around("#{pointcutExpression}")
    public Object monitor(ProceedingJoinPoint jp) throws Throwable {
        if (newAgeMetricsEnabled
            && jp.getSignature().getDeclaringType().isAnnotationPresent(RestController.class)) {
            // Get the templated URL
            String api = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            if (api == null) {
                LOGGER.warn("Templated URL empty for request url (anonymized) {}",
                    anonymizeUrl(request.getRequestURL().toString()));
                api = "unknown";
            }
            try (io.prometheus.client.Histogram.Timer t = latencyHisto.labels(api, request.getMethod(),
                nodeName).startTimer()) {
                return jp.proceed();
            }
        } else if (legacyPerformanceMonitoringEnabled) {
            boolean apiLayer = false;
            if (jp.getSignature().getDeclaringTypeName().endsWith("Controller")) {
                // this is the Controller class
                currentApi.set(jp.getSignature().toLongString());
                apiLayer = true;
            }
            String api = currentApi.get();
            if (api == null) {
                return jp.proceed();
            } else {
                Timer t = registry.timer("api=" + api + ",method=" + jp.toLongString());
                Context c = t.time();
                try {
                    return jp.proceed();
                } finally {
                    c.stop();
                    if (apiLayer) {
                        currentApi.remove();
                    }
                }
            }
        } else {
            return jp.proceed();
        }
    }
    
    private String anonymizeUrl(String requestUrl) {
        return requestUrl.replaceFirst("/users/[a-zA-Z0-9]*/", "/users/{uid}/")
            .replaceFirst("/vehicles/[a-zA-Z0-9]*/",
                "/vehicles/{vid}/");
    }
}
