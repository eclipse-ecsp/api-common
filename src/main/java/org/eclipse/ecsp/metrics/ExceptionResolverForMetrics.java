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
import io.prometheus.client.Counter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Exception resolver that reports error rate in the name errors. <br/>
 * Other metrics
 * are captured in MetricsFilter
 *
 * <p>Metrics are reported with the following labels
 * <ul>
 * <li>service - name of the micro-service</li>
 * <li>api - request uri</li>
 * <li>method - http method</li>
 * <li>node - node name</li>
 * </ul>
 *
 * <p>To enable this resolver, set metrics.enabled to true.
 *
 * @author ssasidharan
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty("metrics.enabled")
public class ExceptionResolverForMetrics implements HandlerExceptionResolver {
    private static final String URI_UNDEFINED = "undefined";
    
    private static final IgniteLogger LOGGER =
        IgniteLoggerFactory.getLogger(ExceptionResolverForMetrics.class);
    
    @Value("${node.name:undefined}")
    private String nodeName;
    
    private Counter errorCounter;
    
    /**
     * creates an error counter in the metrics.
     */
    public ExceptionResolverForMetrics() {
        LOGGER.info("Initializing");
        errorCounter = Counter.build("errors", "Error counter")
            .labelNames("api", "method", "node", "statusCode", "class")
            .register(CollectorRegistry.defaultRegistry);
    }
    
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object handler, Exception ex) {
        if (ex != null) {
            LOGGER.trace("Updating errors counter");
            Object uri = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            String method = request.getMethod();
            errorCounter.labels(uri != null ? (String) uri : URI_UNDEFINED, method, nodeName,
                    String.valueOf(response.getStatus()),
                    ex.getClass().getSimpleName())
                .inc();
        }
        // return null so that default resolvers are processed for actual
        // exception handling
        return null;
    }

    /**
     * This method is a getter for nodename.
     *
     * @return String
     */
    
    public String getNodeName() {
        return nodeName;
    }

    /**
     * This method is a setter for nodename.
     *
     * @param nodeName : String
     */
    
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
}