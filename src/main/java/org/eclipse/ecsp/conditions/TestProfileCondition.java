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

package org.eclipse.ecsp.conditions;

import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * This class enables to conditionally select configuration. (Class defined
 * with @Configuration) In this case it selects the configuration when running
 * tests locally.
 */
public class TestProfileCondition implements Condition {
    private static final IgniteLogger LOGGER =
        IgniteLoggerFactory.getLogger(TestProfileCondition.class);
    
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        LOGGER.info("Checking TestProfileCondition");
        int activeProfileLength = context.getEnvironment().getActiveProfiles().length;
        if (activeProfileLength == 0) {
            LOGGER.info("It is TestProfileCondition");
            return true;
        }
        return false;
    }
}
