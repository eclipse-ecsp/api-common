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


package org.eclipse.ecsp.testutils;

import com.redis.testcontainers.RedisContainer;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.junit.rules.ExternalResource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Runs an in-memory, "embedded" instance of a Redis server.
 *
 * <p>The Redis server instance<br/>
 * it is automatically started when you create a new instance of this class.
 */
@Testcontainers
public class EmbeddedRedisServer extends ExternalResource {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(EmbeddedRedisServer.class);

    @Container
    private static final RedisContainer REDIS_CONTAINER = new RedisContainer(
            RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG));

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("redis.address", () -> REDIS_CONTAINER.getHost() + ":" + REDIS_CONTAINER.getFirstMappedPort());
    }

    @Override
    public void before() throws Throwable {
        if (!REDIS_CONTAINER.isRunning()) {
            REDIS_CONTAINER.start();
        }
        System.setProperty("redis.address", REDIS_CONTAINER.getHost() + ":" + REDIS_CONTAINER.getFirstMappedPort());
        LOGGER.info("Embedded Redis server started on port {} ", REDIS_CONTAINER.getFirstMappedPort());
        LOGGER.info("Redis container started at: {}", REDIS_CONTAINER.getRedisURI());
    }

    @Override
    protected void after() {
        if (REDIS_CONTAINER.isRunning()) {
            REDIS_CONTAINER.stop();
        }
    }

    /**
     * Returns the Redis address.
     *
     * @return the Redis address
     */
    public String getRedisAddress() {
        return REDIS_CONTAINER.getHost() + ":" + REDIS_CONTAINER.getFirstMappedPort();
    }

}
