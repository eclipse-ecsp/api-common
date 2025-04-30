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

package org.eclipse.ecsp.validations;

import org.eclipse.ecsp.healthcheck.HealthMonitor;
import org.eclipse.ecsp.healthcheck.HealthService;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * {@link HealthValidation} perform initial health checks and periodic health checks.
 *
 * @author abhishekkumar
 */
@Component
public class HealthValidation {
    
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(HealthValidation.class);

    private final HealthService healthService;

    /**
     * Constructor for {@link HealthValidation}.
     *
     * @param healthService {@link HealthService}
     */
    public HealthValidation(HealthService healthService) {
        this.healthService = healthService;
    }
    
    /**
     * This method is an {@link ApplicationStartedEvent} callback.<br/>
     * which performs initial startup health checks.
     *
     * @return list of {@link HealthMonitor}
     */
    @EventListener(classes = ApplicationStartedEvent.class)
    public List<HealthMonitor> doInitialHealthValidation() {
        LOGGER.info("API Commons initial health check trigger started");
        List<HealthMonitor> healthMonitorList = healthService.triggerInitialCheck();
        LOGGER.info("API Commons initial health check trigger completed");
        return healthMonitorList;
        
    }
    
    /**
     * This method is an {@link ApplicationReadyEvent} callback.<br/>
     * which performs periodic health checks.
     */
    @EventListener(classes = ApplicationReadyEvent.class)
    public void startPeriodicHealthValidation() {
        
        LOGGER.info("API Commons initialing periodic health check");
        
        healthService.registerCallBack(new ServicesHealthCheckCallback());
        healthService.startHealthServiceExecutor();
        
        LOGGER.info("API Commons periodic health check successfully initiated at {}",
            System.currentTimeMillis());
        
    }
    
}