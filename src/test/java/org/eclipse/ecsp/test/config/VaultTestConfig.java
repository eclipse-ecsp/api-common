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

package org.eclipse.ecsp.test.config;

import org.aspectj.weaver.bcel.Utility;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * functions to allow Vault testing without complete Vault Server implementation.
 *
 * @author msingh7
 * @see Utility
 */
@Configuration
@Profile("test")
@ConditionalOnProperty(name = "vault.enabled", havingValue = "true")
@ComponentScan(basePackages = {"org.eclipse.ecsp.vault"})
public class VaultTestConfig {
    
    // Hook to add cleanup related code
    protected void teardown() throws Exception {
    
    }
}