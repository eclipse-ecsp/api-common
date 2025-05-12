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
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.errors.TopicExistsException;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.junit.ClassRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A convenient base class for integration testing.
 * One should call super.setup() and super.createTopics() to initialize the test base appropriately
 */
@Testcontainers
public class CommonTestBase {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(CommonTestBase.class);

    public CommonTestBase() {
        CollectorRegistry.defaultRegistry.clear();
    }

    /**
     * changed from @Rule to @ClassRule and also making as static as part of.
     * Running Kafka<br/>
     * and Zookeeper on dynamic ports to resolve bind address issue in<br/>
     * api-commons project
     */
    @ClassRule
    public static final SingleNodeKafkaCluster KAFKA_CLUSTER = new SingleNodeKafkaCluster();

    @ClassRule
    public static final MongoServer MONGO_SERVER = new MongoServer();

    @ClassRule
    public static final EmbeddedRedisServer REDIS_SERVER = new EmbeddedRedisServer();
    
    @Value("${kafka.key.deserializer}")
    private String keyDeserializer;
    
    @Value("${kafka.value.deserializer}")
    private String valueDeserializer;
    
    @Value("${kafka.key.serializer}")
    private String keySerializer;
    
    @Value("${kafka.value.serializer}")
    private String valueSerializer;
    
    @Value("${topic.create.interval.ms:500}")
    private long topicCreateIntervalMs;
    
    protected final Properties consumerProps = new Properties();
    protected final Properties producerProps = new Properties();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("mongodb.hosts", MONGO_SERVER::getMongoServerHost);
        registry.add("mongodb.port", MONGO_SERVER::getMongoServerPort);
        registry.add("redis.address", REDIS_SERVER::getRedisAddress);
        registry.add("kafka.broker.url", KAFKA_CLUSTER::getKafkaBrokerList);
    }

    protected void setup() {
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CLUSTER.bootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "tc-consumer");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            keyDeserializer);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            valueDeserializer);
        
        producerProps.put(ProducerConfig.ACKS_CONFIG, "all");
        producerProps.put(ProducerConfig.RETRIES_CONFIG, 0);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            keySerializer);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            valueSerializer);
    }
    
    // Creates topics and ensures they are created by deleting if need be before
    // creating
    protected void createTopics(String... topics) {
        for (String topic : topics) {
            while (true) {
                LOGGER.info("Deleting topic {}", topic);
                try {
                    KAFKA_CLUSTER.deleteTopic(topic);
                } catch (Exception e) {
                    LOGGER.error("Deleting topic: " + topic + " got exception", e);
                }
                try {
                    CountDownLatch countDownLatch = new CountDownLatch(1);
                    if (!countDownLatch.await(topicCreateIntervalMs, TimeUnit.MILLISECONDS)) {
                        LOGGER.warn("Timeout occurred while waiting for latch countdown");
                    }
                } catch (InterruptedException e) {
                    // ignore
                }
                LOGGER.info("Creating topic {}", topic);
                try {
                    KAFKA_CLUSTER.createTopic(topic);
                } catch (TopicExistsException tee) {
                    LOGGER.error("Creating topic {} failed. Will delete and try again", topic);
                    continue;
                }
                break;
            }
        }
    }
    
    /**
     * Hook to add cleanup related code.
     */
    protected void teardown() throws Exception {
        // no-op
    }
}