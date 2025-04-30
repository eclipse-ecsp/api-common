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

import kafka.server.KafkaConfig;
import kafka.server.KafkaConfig$;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Runs an in-memory, "embedded" instance of a Kafka broker, which listens at `127.0.0.1:9092` by
 * default.
 *
 * <p>Requires a running ZooKeeper instance to connect to.<br/>
 * By default, it expects a ZooKeeper instance
 * running at `127.0.0.1:2181`.  You can specify a different ZooKeeper instance by setting the
 * `zookeeper.connect` parameter in the broker's configuration.
 */
@Testcontainers
public class EmbeddedKafka {
    
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(EmbeddedKafka.class);
    private static final EmbeddedZookeeper ZOOKEEPER = new EmbeddedZookeeper();
    @Container
    private static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer("apache/kafka-native:3.8.0")
                .withEnv("KAFKA_ZOOKEEPER_CONNECT", ZOOKEEPER.connectString())
                .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1");

    private Properties config;
    private String brokerList;
    private String zookeeperConnect;
    /**
     * MESSAGE_MAX_BYTES.
     */
    public static final int MESSAGE_MAX_BYTES = 1000000;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        // Get Kafka broker address
        String kafkaBroker = KAFKA_CONTAINER.getHost() + ":" + KAFKA_CONTAINER.getFirstMappedPort();
        registry.add("kafka.broker.url", () -> kafkaBroker);
    }
    
    /**
     * Creates and starts an embedded Kafka broker.
     *
     * @param config Broker configuration settings.  Used to modify, for example, on which port the
     *               broker should listen to.  Note that you cannot change some settings such as
     *               `log.dirs`, `port`.
     */
    public EmbeddedKafka(final Properties config) {
        this.config = config;
        if (!KAFKA_CONTAINER.isRunning()) {
            KAFKA_CONTAINER.start();
        }
        // Get Kafka broker address
        String kafkaBroker = KAFKA_CONTAINER.getHost() + ":" + KAFKA_CONTAINER.getFirstMappedPort();
        System.setProperty("kafka.broker.url", kafkaBroker);
        zookeeperConnect = ZOOKEEPER.zookeeperConnect;
        LOGGER.debug("Kafka broker running at: {}", kafkaBroker);
        LOGGER.debug("Starting embedded Kafka broker (and ZK ensemble at {}) ...",
                zookeeperConnect);
        brokerList = KAFKA_CONTAINER.getBootstrapServers();
        LOGGER.debug("Startup of embedded Kafka broker at {} completed (with ZK ensemble at {}) ...",
                kafkaBroker, zookeeperConnect);
        // create kafka admin client for topic creation and deletion
        Properties properties = effectiveConfigFrom();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
    }
    
    private Properties effectiveConfigFrom() {
        final Properties effectiveConfig = new Properties();
        effectiveConfig.put(KafkaConfig$.MODULE$.BrokerIdProp(), 0);
        effectiveConfig.put(KafkaConfig.ListenersProp(),
                "PLAINTEXT://127.0.0.1:" + KAFKA_CONTAINER.getFirstMappedPort());
        effectiveConfig.put(KafkaConfig$.MODULE$.NumPartitionsProp(), 1);
        effectiveConfig.put(KafkaConfig$.MODULE$.AutoCreateTopicsEnableProp(), true);
        effectiveConfig.put(KafkaConfig$.MODULE$.MessageMaxBytesProp(), MESSAGE_MAX_BYTES);
        effectiveConfig.put(KafkaConfig$.MODULE$.ControlledShutdownEnableProp(), true);
        
        effectiveConfig.putAll(config);
        effectiveConfig.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
            Serdes.String().getClass().getName());
        effectiveConfig.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
            Serdes.String().getClass().getName());
        
        return effectiveConfig;
    }
    
    /**
     * This broker's `metadata.broker.list` value.  Example: `127.0.0.1:9092`.
     *
     * <p>You can use this to tell Kafka producers and consumers how to connect to this instance.
     */
    public String brokerList() {
        return KAFKA_CONTAINER.getBootstrapServers();
    }

    public String zookeeperConnect() {
        return zookeeperConnect;
    }
    
    /**
     * Stop the broker.
     */
    public void stop() throws IOException {
        LOGGER.debug("Shutting down embedded Kafka broker at {} (with ZK ensemble at {}) ...",
                brokerList, zookeeperConnect);
        if (KAFKA_CONTAINER.isRunning()) {
            KAFKA_CONTAINER.stop();
        }
        ZOOKEEPER.stop();
        LOGGER.debug("Shutdown of embedded Kafka broker at {} "
                        + "completed (with ZK ensemble at {}) ...",
                brokerList,  zookeeperConnect);
    }
    
    /**
     * Create a Kafka topic with 1 partition and a replication factor of 1.
     *
     * @param topic The name of the topic.
     */
    public void createTopic(final String topic) {
        createTopic(topic, 1, (short) 1, Collections.emptyMap());
    }
    
    /**
     * Create a Kafka topic with the given parameters.
     *
     * @param topic       The name of the topic.
     * @param partitions  The number of partitions for this topic.
     * @param replication The replication factor for (the partitions of) this topic.
     */
    public void createTopic(final String topic, final int partitions, final short replication) {
        createTopic(topic, partitions, replication, Collections.emptyMap());
    }
    
    /**
     * Create a Kafka topic with the given parameters.
     *
     * @param topic       The name of the topic.
     * @param partitions  The number of partitions for this topic.
     * @param replication The replication factor for (partitions of) this topic.
     * @param topicConfig Additional topic-level configuration settings.
     */
    public void createTopic(final String topic,
                            final int partitions,
                            final short replication,
                            final Map<String, String> topicConfig) {
        LOGGER.debug("Creating topic { name: {}, partitions: {}, replication: {}, config: {} }",
            topic, partitions, replication, topicConfig);
        
        final Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        
        try (final AdminClient adminClient = AdminClient.create(properties)) {
            final NewTopic newTopic = new NewTopic(topic, partitions, replication);
            newTopic.configs(topicConfig);
            adminClient.createTopics(Collections.singleton(newTopic)).all().get();
        } catch (final InterruptedException | ExecutionException fatal) {
            throw new RuntimeException(fatal);
        }
        
    }
    
    /**
     * Delete a Kafka topic.
     *
     * @param topic The name of the topic.
     */
    public void deleteTopic(final String topic) {
        LOGGER.debug("Deleting topic {}", topic);
        final Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        
        try (final AdminClient adminClient = AdminClient.create(properties)) {
            adminClient.deleteTopics(Collections.singleton(topic)).all().get();
        } catch (final InterruptedException e) {
            LOGGER.error("InterruptedException occurred when attempting to delete topics", e);
            throw new RuntimeException(e);
        } catch (final ExecutionException e) {
            if (!(e.getCause() instanceof UnknownTopicOrPartitionException)) {
                LOGGER.error("ExecutionException occurred when attempting to delete topics", e);
                throw new RuntimeException(e);
            }
        }
    }
    
    KafkaContainer kafkaServer() {
        return KAFKA_CONTAINER;
    }
}