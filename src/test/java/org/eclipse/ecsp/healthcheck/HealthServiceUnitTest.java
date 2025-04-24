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

package org.eclipse.ecsp.healthcheck;

import io.prometheus.client.CollectorRegistry;
import org.eclipse.ecsp.nosqldao.spring.config.IgniteDAOMongoConfigWithProps;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.eclipse.ecsp.validations.HealthValidation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertTrue;

/**
 * {@link HealthServiceUnitTest} contains test cases for health service monitors.
 *
 * @author abhishekkumar
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@TestPropertySource("classpath:/application-base-health-monitor.properties")
public class HealthServiceUnitTest extends CommonTestBase {
    
    static {
        // Workaround to avoid duplicate metrics registration in case of Spring
        // Boot dev-tools restarts
        CollectorRegistry.defaultRegistry.clear();
    }
    
    @Mock
    private HealthService healthService;
    
    @Mock
    private IgniteDAOMongoConfigWithProps igniteDaoMongoConfigWithProps;
    
    @Mock
    private HealthValidation healthValidation;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testHealthCheck() {
        
        List<HealthMonitor> failedMonitors = new ArrayList<HealthMonitor>();
        
        Mockito.doCallRealMethod().when(healthValidation).setHealthService(Mockito.any());
        Mockito.when(healthValidation.doInitialHealthValdiation()).thenCallRealMethod();
        Mockito.doNothing().when(healthValidation).startPeriodicHealthValdiation();
        
        Mockito.when(healthService.triggerInitialCheck()).thenReturn(failedMonitors);
        Mockito.when(igniteDaoMongoConfigWithProps.monitorName()).thenReturn("MONGO_HEALTH_MONITOR");
        
        healthValidation.setHealthService(healthService);
        
        List<HealthMonitor> healthMonitorList = healthValidation.doInitialHealthValdiation();
        assertTrue(healthMonitorList.isEmpty());
        
        failedMonitors.add(igniteDaoMongoConfigWithProps);
        Mockito.when(healthService.triggerInitialCheck()).thenReturn(failedMonitors);
        Mockito.when(igniteDaoMongoConfigWithProps.monitorName())
            .thenReturn("NOT_MONGO_HEALTH_MONITOR");
        
        healthMonitorList = healthValidation.doInitialHealthValdiation();
        assertTrue(!healthMonitorList.isEmpty());
        
        assertTrue(healthMonitorList.get(0).monitorName().equals("NOT_MONGO_HEALTH_MONITOR"));
    }
    
}
