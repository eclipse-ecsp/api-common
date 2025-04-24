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

import io.prometheus.client.CollectorRegistry;
import kafka.server.KafkaConfig;
import kafka.server.KafkaConfig$;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.junit.rules.ExternalResource;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * Runs an in-memory, "embedded" Kafka cluster with 1 ZooKeeper instance and 1
 * Kafka broker.
 */
public class SingleNodeKafkaCluster extends ExternalResource {
    
    private static final IgniteLogger LOGGER =
        IgniteLoggerFactory.getLogger(SingleNodeKafkaCluster.class);

    /**
     * INT_30.
     */
    public static final int INT_30 = 30;

    /**
     * INT_1000.
     */
    public static final int INT_1000 = 1000;

    /**
     * INT_60.
     */
    public static final int INT_60 = 60;

    /**
     * INT_2.
     */
    public static final int INT_2 = 2;

    /**
     * INT_1024.
     */
    public static final int INT_1024 = 1024;

    /**
     * LONG_1024.
     */
    public static final long LONG_1024 = 1024L;

    /**
     * INT_1.
     */
    public static final int INT_1 = 1;

    /**
     * TIMEOUT_MS.
     */
    public static final long TIMEOUT_MS = 60000L;
    private EmbeddedZookeeper zookeeper;
    private EmbeddedKafka broker;
    private final Properties brokerConfig;
    private boolean running;
    
    /**
     * Creates and starts the cluster.
     */
    public SingleNodeKafkaCluster() {
        this(new Properties());
        CollectorRegistry.defaultRegistry.clear();
    }
    
    public String zkConnectString() {
        return zookeeper.connectString();
    }

    public String getKafkaBrokerList() {
        return broker.brokerList();
    }
    
    /**
     * Creates and starts the cluster.
     *
     * @param brokerConfig Additional broker configuration settings.
     */
    public SingleNodeKafkaCluster(final Properties brokerConfig) {
        this.brokerConfig = new Properties();
        this.brokerConfig.putAll(brokerConfig);
    }
    
    /**
     * Creates and starts the cluster.
     */
    public void start() throws Exception {
        LOGGER.debug("Initiating embedded Kafka cluster startup");
        LOGGER.debug("Starting a ZooKeeper instance...");
        zookeeper = new EmbeddedZookeeper();
        LOGGER.debug("ZooKeeper instance is running at {}", zookeeper.connectString());
        
        final Properties effectiveBrokerConfig = effectiveBrokerConfigFrom(brokerConfig, zookeeper);
        LOGGER.debug("Starting a Kafka instance on port {} ...",
            effectiveBrokerConfig.getProperty(KafkaConfig.ListenersProp()));
        broker = new EmbeddedKafka(effectiveBrokerConfig);
        LOGGER.debug("Kafka instance is running at {}, connected to ZooKeeper at {}",
            broker.brokerList(), broker.zookeeperConnect());
    }
    
    private Properties effectiveBrokerConfigFrom(final Properties brokerConfig,
                                                 final EmbeddedZookeeper zookeeper) {
        final Properties effectiveConfig = new Properties();
        effectiveConfig.putAll(brokerConfig);
        effectiveConfig.put(KafkaConfig$.MODULE$.ZkConnectProp(), zookeeper.connectString());
        effectiveConfig.put(KafkaConfig$.MODULE$.ZkSessionTimeoutMsProp(), INT_30 * INT_1000);
        effectiveConfig.put(KafkaConfig$.MODULE$.ZkConnectionTimeoutMsProp(), INT_60 * INT_1000);
        effectiveConfig.put(KafkaConfig$.MODULE$.DeleteTopicEnableProp(), true);
        effectiveConfig.put(KafkaConfig$.MODULE$.LogCleanerDedupeBufferSizeProp(), INT_2 * INT_1024 * LONG_1024);
        effectiveConfig.put(KafkaConfig$.MODULE$.GroupMinSessionTimeoutMsProp(), 0);
        effectiveConfig.put(KafkaConfig$.MODULE$.OffsetsTopicReplicationFactorProp(), (short) INT_1);
        effectiveConfig.put(KafkaConfig$.MODULE$.OffsetsTopicPartitionsProp(), INT_1);
        effectiveConfig.put(KafkaConfig$.MODULE$.AutoCreateTopicsEnableProp(), true);
        return effectiveConfig;
    }
    
    @Override
    protected void before() throws Exception {
        start();
    }
    
    @Override
    protected void after() {
        stop();
    }
    
    /**
     * Stops the cluster.
     */
    public void stop() {
        LOGGER.info("Stopping Confluent");
        try {
            if (broker != null) {
                broker.stop();
            }
            try {
                if (zookeeper != null) {
                    zookeeper.stop();
                }
            } catch (final IOException fatal) {
                throw new RuntimeException(fatal);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            running = false;
        }
        LOGGER.info("Confluent Stopped");
    }
    
    /**
     * This cluster's `bootstrap.servers` value.  Example: `127.0.0.1:9092`.
     *
     * <p>You can use this to tell Kafka Streams applications,<br/>
     * Kafka producers, and Kafka consumers (new
     * consumer API) how to connect to this cluster.
     */
    public String bootstrapServers() {
        return broker.brokerList();
    }
    
    public void deleteTopic(final String topic) {
        broker.deleteTopic(topic);
    }
    
    /**
     * Creates a Kafka topic with 1 partition and a replication factor of 1.
     *
     * @param topic The name of the topic.
     */
    public void createTopic(final String topic) throws InterruptedException {
        createTopic(topic, INT_1, (short) INT_1, Collections.emptyMap());
    }
    
    /**
     * Creates a Kafka topic with the given parameters.
     *
     * @param topic       The name of the topic.
     * @param partitions  The number of partitions for this topic.
     * @param replication The replication factor for (the partitions of) this topic.
     */
    public void createTopic(final String topic, final int partitions, final short replication)
        throws InterruptedException {
        createTopic(topic, partitions, replication, Collections.emptyMap());
    }
    
    /**
     * Creates a Kafka topic with the given parameters.
     *
     * @param topic       The name of the topic.
     * @param partitions  The number of partitions for this topic.
     * @param replication The replication factor for (partitions of) this topic.
     * @param topicConfig Additional topic-level configuration settings.
     */
    public void createTopic(final String topic,
                            final int partitions,
                            final short replication,
                            final Map<String, String> topicConfig) throws InterruptedException {
        createTopic(TIMEOUT_MS, topic, partitions, replication, topicConfig);
    }
    
    /**
     * Creates a Kafka topic with the given parameters and blocks until all topics got created.
     *
     * @param topic       The name of the topic.
     * @param partitions  The number of partitions for this topic.
     * @param replication The replication factor for (partitions of) this topic.
     * @param topicConfig Additional topic-level configuration settings.
     */
    public void createTopic(final long timeoutMs,
                            final String topic,
                            final int partitions,
                            final short replication,
                            final Map<String, String> topicConfig) throws InterruptedException {
        broker.createTopic(topic, partitions, replication, topicConfig);
        
        
    }
    
    /**
     * Deletes multiple topics and blocks until all topics got deleted.
     *
     * @param timeoutMs the max time to wait
     *                  for the topics to be deleted (does not block if {@code <= 0})
     * @param topics    the name of the topics
     */
    public void deleteTopicsAndWait(final long timeoutMs, final String... topics)
        throws InterruptedException {
        for (final String topic : topics) {
            try {
                broker.deleteTopic(topic);
            } catch (final UnknownTopicOrPartitionException expected) {
                // indicates (idempotent) success
            }
        }
        
        
    }
    public boolean isRunning() {
        return running;
    }

}