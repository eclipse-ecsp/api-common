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

import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import java.io.IOException;

/**
 * Runs an in-memory, "embedded" instance of a ZooKeeper server.
 *
 * <p>The ZooKeeper server instance<br/>
 * it is automatically started when you create a new instance of this class.
 */
@Testcontainers
public class EmbeddedZookeeper {
    
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(EmbeddedZookeeper.class);

    @Container
    private GenericContainer<?> zookeeperContainer = new GenericContainer<>(DockerImageName.parse("confluentinc/cp-zookeeper:7.4.0"))
            .withEnv("ZOOKEEPER_CLIENT_PORT", "2181")
            .withExposedPorts(2181);
    String zookeeperConnect;

    /**
     * Creates and starts a ZooKeeper instance.
     *
     */
    public EmbeddedZookeeper() {
        zookeeperContainer.start();
        // Get ZooKeeper connection string
        zookeeperConnect = zookeeperContainer.getHost() + ":" + zookeeperContainer.getMappedPort(2181);
        LOGGER.debug("ZooKeeper running at: {}", zookeeperConnect);
        LOGGER.debug("Embedded ZooKeeper server at {} uses the temp directory at {}",
                zookeeperConnect, zookeeperContainer.getContainerId());
    }
    
    /**
     * Stops a ZooKeeper instance.
     *
     * @throws IOException when unable to close to server
     */
    public void stop() throws IOException {
        LOGGER.debug("Shutting down embedded ZooKeeper server at {} ...", zookeeperConnect);
        zookeeperContainer.stop();
        LOGGER.debug("Shutdown of embedded ZooKeeper server at {} completed", zookeeperConnect);
    }
    
    /**
     * The ZooKeeper connection string aka `zookeeper.connect` in `hostnameOrIp:port` format.
     * Example: `127.0.0.1:2181`.
     *
     * <p>You can use this to e.g. tell Kafka brokers how to connect to this instance.
     */
    public String connectString() {
        return zookeeperConnect;
    }
    
    /**
     * The hostname of the ZooKeeper instance.  Example: `127.0.0.1`
     */
    public String hostname() {
        // "server:1:2:3" -> "server:1:2"
        return connectString().substring(0, connectString().lastIndexOf(':'));
    }
    
}