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

package org.eclipse.ecsp.configurations;

import org.eclipse.ecsp.metrics.MetricsFilter;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring configuration class contains cors Registry,api processing metrics filter configuration.
 *
 * @author abhishekkumar
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(WebConfig.class);
    
    @Value("${cors.origin.allow:*}")
    private String corsOriginAllow;
    
    @Value("${metrics.enabled}")
    private boolean metricsEnabled;
    
    @Value("${node.name:undefined}")
    private String nodeName;
    
    @Value("${service.name:undefined}")
    private String serviceName;
    
    @Value("#{'${processing.duration.buckets:0.05,0.1,0.2,0.3,0.4,0.7,1,2.5,5,10}'.split(',')}")
    private double[] apiProcessingDurationBuckets;

    /**
     * create {@link MethodValidationPostProcessor}.
     *
     * @return instance of {@link MethodValidationPostProcessor}
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
    
    /**
     * add cors origin to {@link CorsRegistry}.
     *
     * @return {@link WebMvcConfigurer} which contains cors mapping
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping(corsOriginAllow.trim());
            }
        };
    }
    
    /**
     * creates bean which register metrics filter which collects api request processing durations.
     *
     * @return FilterRegistrationBean of {@link MetricsFilter}
     */
    @Bean
    public FilterRegistrationBean<MetricsFilter> metricsFilter() {
        LOGGER.info("Registering metrics filter if metrics is enabled {}", metricsEnabled);
        MetricsFilter filter = new MetricsFilter();
        filter.setApiProcessingDurationBuckets(apiProcessingDurationBuckets);
        filter.setNodeName(nodeName);
        filter.setMetricsEnabled(metricsEnabled);
        FilterRegistrationBean<MetricsFilter> mfrb = new FilterRegistrationBean<>(filter);
        mfrb.setEnabled(metricsEnabled);
        mfrb.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return mfrb;
    }

    /**
     * is metric enabled.
     *
     * @return metric enabled
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
     * This method is a getter for serviceName.
     *
     * @return String
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * This method is a setter for serviceName.
     *
     * @param serviceName : String
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * This method is a getter for api processing duration buckets.
     *
     * @return double
     */
    public double[] getApiProcessingDurationBuckets() {
        return apiProcessingDurationBuckets;
    }

    /**
     * This method is a setter for api processing duration buckets.
     *
     * @param apiProcessingDurationBuckets : double
     */
    public void setApiProcessingDurationBuckets(double[] apiProcessingDurationBuckets) {
        this.apiProcessingDurationBuckets = apiProcessingDurationBuckets;
    }
}