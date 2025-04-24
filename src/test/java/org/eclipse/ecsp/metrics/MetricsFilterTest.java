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
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Metrics filter test cases for API request processing duration.
 *
 * @author abhishekkumar
 */
public class MetricsFilterTest {
    

    /**
     * INT_2.
     */
    public static final int INT_2 = 2;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;
    private MetricsFilter filter;
    
    /**
     * setting up the registry and configuring metrics filter.
     */
    @Before
    public void setup() {
        CollectorRegistry.defaultRegistry.clear();
        request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getRequestURI()).thenReturn("/index.html");
        Mockito.when(request.getMethod()).thenReturn("GET");
        response = Mockito.mock(HttpServletResponse.class);
        chain = Mockito.mock(FilterChain.class);
        filter = new MetricsFilter();
    }
    
    @Test
    public void testDisabled() throws IOException, ServletException {
        filter.setMetricsEnabled(false);
        filter.doFilter(request, response, chain);
        // when disabled, the chain.doFilter() should be invoked
        Mockito.verify(chain, Mockito.times(1)).doFilter(request, response);
        // but registry should be empty
        new PrometheusMetricsRegistryAssertion().assertEmpty();
    }
    
    @Test
    public void testEnabled() throws IOException, ServletException {
        filter.setMetricsEnabled(true);
        filter.setApiProcessingDurationBuckets(new double[] {1, INT_2});
        filter.setNodeName("localhost");
        filter.init(Mockito.mock(FilterConfig.class));
        filter.doFilter(request, response, chain);
        Mockito.verify(chain, Mockito.times(1)).doFilter(request, response);
        new PrometheusMetricsRegistryAssertion().assertCaptured();
    }
    
    @Test
    public void testEnabledWithDetails() throws IOException, ServletException {
        filter.setMetricsEnabled(true);
        filter.setApiProcessingDurationBuckets(new double[] {1, INT_2});
        filter.setNodeName("localhost");
        filter.init(Mockito.mock(FilterConfig.class));
        filter.doFilter(request, response, chain);
        Mockito.verify(chain, Mockito.times(1)).doFilter(request, response);
        new PrometheusMetricsRegistryAssertion().assertCaptured();
        
        assertEquals(1, filter.getApiProcessingDurationBuckets()[0]);
        assertEquals(INT_2, filter.getApiProcessingDurationBuckets()[1]);
        
        assertEquals("localhost", filter.getNodeName());
        
        assertTrue(filter.isMetricsEnabled());
        
        filter.destroy();
    }
}