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
import org.eclipse.ecsp.validations.ServicesHealthCheckCallback;
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
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

    private HealthValidation healthValidation;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        healthValidation = new HealthValidation(healthService);
    }
    
    @Test
    public void testHealthCheck() {
        List<HealthMonitor> healthMonitorList = healthValidation.doInitialHealthValidation();
        assertTrue(healthMonitorList.isEmpty());
    }

    @Test
    public void testHealthCheckFailure() {
        Mockito.doReturn("MONGO_HEALTH_MONITOR").when(igniteDaoMongoConfigWithProps).monitorName();
        Mockito.doReturn(List.of(igniteDaoMongoConfigWithProps)).when(healthService).triggerInitialCheck();

        List<HealthMonitor> healthMonitorList = healthValidation.doInitialHealthValidation();
        assertFalse(healthMonitorList.isEmpty());
        assertEquals("MONGO_HEALTH_MONITOR", healthMonitorList.get(0).monitorName());
    }

    @Test
    public void testPerformRestart() {
        ServicesHealthCheckCallback servicesHealthCheckCallback = new ServicesHealthCheckCallback();
        boolean result = servicesHealthCheckCallback.performRestart();
        assertFalse(result);
    }
    
}
