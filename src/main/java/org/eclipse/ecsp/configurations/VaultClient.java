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

import jakarta.annotation.PostConstruct;
import org.eclipse.ecsp.constants.Constants;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.Properties;

/**
 * {@link VaultClient} class to load properties from vault.
 * This is applicable if vault.enabled is set to false
 *
 * @author abhishekkumar
 */
@Component("vaultClient")
@ConditionalOnProperty(name = "vault.enabled", havingValue = "false", matchIfMissing = true)
public class VaultClient {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VaultClient.class);

    @PostConstruct
    private void initialize() {
        LOGGER.info("Vault is disabled load config from system properties.");
        Properties systemProps = System.getProperties();
        LOGGER.debug("Properties fetched from System {}", systemProps.toString());
        systemProps.forEach((k, v) -> {
            String key = (String) k;
            if (key.contains(Constants.UNDER_SCORE)) {
                String appKey = ((String) k).replace(Constants.UNDER_SCORE, Constants.DOT);
                systemProps.setProperty(appKey, (String) v);
            }
        });
        LOGGER.debug("System properties after loading : {}", System.getProperties().toString());
    }
}