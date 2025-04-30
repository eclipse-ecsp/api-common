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
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.utils.Utils;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Utility functions to make integration testing more convenient.
 */
public class KafkaTestUtils {
    
    private static final int UNLIMITED_MESSAGES = -1;

    /**
     * INT_100.
     */
    public static final int INT_100 = 100;

    /**
     * INT_2000.
     */
    public static final int INT_2000 = 2000;

    /**
     * INT_2.
     */
    public static final int INT_2 = 2;
    
    private KafkaTestUtils() {
        CollectorRegistry.defaultRegistry.clear();
    }
    
    /**
     * Returns up to `maxMessages` message-values from the topic.
     *
     * @param topic          Kafka topic to read messages from
     * @param consumerConfig Kafka consumer configuration
     * @param maxMessages    Maximum number of messages to read via the consumer.
     * @return The values retrieved via the consumer.
     */
    public static <K, V> List<V> readValues(String topic, Properties consumerConfig,
                                            int maxMessages) {
        List<V> returnList = new ArrayList<>();
        List<KeyValue<K, V>> kvs = readKeyValues(topic, consumerConfig, maxMessages);
        for (KeyValue<K, V> kv : kvs) {
            returnList.add(kv.value);
        }
        return returnList;
    }
    
    /**
     * Returns as many messages as possible from the topic until a (currently
     * hardcoded) timeout is reached.
     *
     * @param topic          Kafka topic to read messages from
     * @param consumerConfig Kafka consumer configuration
     * @return The KeyValue elements retrieved via the consumer.
     */
    public static <K, V> List<KeyValue<K, V>> readKeyValues(String topic, Properties consumerConfig) {
        return readKeyValues(topic, consumerConfig, UNLIMITED_MESSAGES);
    }
    
    /**
     * Returns up to `maxMessages` by reading via the provided consumer (the
     * topic(s) to read from are already configured in the consumer).
     *
     * @param topic          Kafka topic to read messages from
     * @param consumerConfig Kafka consumer configuration
     * @param maxMessages    Maximum number of messages to read via the consumer
     * @return The KeyValue elements retrieved via the consumer
     */
    public static <K, V> List<KeyValue<K, V>> readKeyValues(String topic, Properties consumerConfig,
                                                            int maxMessages) {
        KafkaConsumer<K, V> consumer = new KafkaConsumer<>(consumerConfig);
        List<KeyValue<K, V>> consumedValues = new ArrayList<>();
        try {
            consumer.subscribe(Collections.singletonList(topic));
            int pollIntervalMs = INT_100;
            int maxTotalPollTimeMs = INT_2000;
            int totalPollTimeMs = 0;
            while (totalPollTimeMs < maxTotalPollTimeMs
                && continueConsuming(consumedValues.size(), maxMessages)) {
                totalPollTimeMs += pollIntervalMs;
                ConsumerRecords<K, V> records = consumer.poll(Duration.ofMillis(pollIntervalMs));
                for (ConsumerRecord<K, V> kvConsumerRecord : records) {
                    consumedValues.add(new KeyValue<>(kvConsumerRecord.key(), kvConsumerRecord.value()));
                }
            }
        } finally {
            consumer.close();
        }
        return consumedValues;
    }
    
    private static boolean continueConsuming(int messagesConsumed, int maxMessages) {
        return maxMessages <= 0 || messagesConsumed < maxMessages;
    }
    
    /**
     * Removes local state stores. Useful to reset state in-between integration
     * test runs.
     *
     * @param streamsConfiguration Streams configuration settings
     */
    public static void purgeLocalStreamsState(Properties streamsConfiguration) throws IOException {
        String path = streamsConfiguration.getProperty(StreamsConfig.STATE_DIR_CONFIG);
        if (path != null) {
            File node = Paths.get(path).normalize().toFile();
            // Only purge state when it's under /tmp. This is a safety net to
            // prevent accidentally
            // deleting important local directory trees.
            if (node.getAbsolutePath().startsWith("/tmp")) {
                Utils.delete(new File(node.getAbsolutePath()));
            }
        }
    }
    
    /**
     * Sending multiple kafka message in blocking manner to specified topic.<br/>
     * and use producerConfig to prepare producer and closes after sending messages.
     *
     * @param topic          Kafka topic to write the data records to
     * @param records        Data records to write to Kafka
     * @param producerConfig Kafka producer configuration
     * @param <K>            Key type of the data records
     * @param <V>            Value type of the data records
     */
    public static <K, V> void produceKeyValuesSynchronously(
        String topic, Collection<KeyValue<K, V>> records, Properties producerConfig)
        throws ExecutionException, InterruptedException {
        try (Producer<K, V> producer = new KafkaProducer<>(producerConfig)) {
            for (KeyValue<K, V> kvRecord : records) {
                Future<RecordMetadata> f = producer.send(
                        new ProducerRecord<>(topic, kvRecord.key, kvRecord.value));
                f.get();
            }
            producer.flush();
        }
    }
    
    /**
     * Sending kafka message in blocking manner to specified topic.<br/>
     * and use producerConfig to prepare producer and closes after sending messages.
     *
     * @param topic          Kafka topic to write the data records to
     * @param records        Data records to write to Kafka
     * @param producerConfig Kafka producer configuration
     */
    public static <V> void produceValuesSynchronously(
        String topic, Collection<V> records, Properties producerConfig)
        throws ExecutionException, InterruptedException {
        Collection<KeyValue<Object, V>> keyedRecords = new ArrayList<>();
        for (V value : records) {
            KeyValue<Object, V> kv = new KeyValue<>(null, value);
            keyedRecords.add(kv);
        }
        produceKeyValuesSynchronously(topic, keyedRecords, producerConfig);
    }
    
    /**
     * read message from specified kafka topic.<br/>
     *
     * @param topic         Kafka topic to read messages from
     * @param consumerProps Kafka consumer configuration
     * @param i             Maximum number of messages to read via the consume
     * @return The KeyValue elements retrieved via the consumer
     * @throws TimeoutException if kafka timeout occures while reading message from kafka
     */
    public static List<String[]> readMessages(String topic, Properties consumerProps, int i) {
        return KafkaTestUtils.readKeyValues(topic, consumerProps, i).stream()
            .map(t -> new String[] {(String) t.key, (String) t.value})
            .toList();
    }
    
    /**
     * Send message to specified kafka topic.
     *
     * @param topic         Kafka topic to send messages to
     * @param producerProps Kafka producer configuration
     * @param strings       message 0th element is key and 1st element is value and so on..
     * @throws ExecutionException   if any error during sending message
     * @throws InterruptedException if any interrupted occurs while sending message
     */
    public static void sendMessages(String topic, Properties producerProps, String... strings)
        throws ExecutionException, InterruptedException {
        Collection<KeyValue<Object, Object>> kvs = new ArrayList<>();
        for (int i = 1; i <= strings.length; i++) {
            if (i % INT_2 == 0) {
                kvs.add(new KeyValue(strings[i - INT_2], strings[i - 1]));
            }
        }
        KafkaTestUtils.produceKeyValuesSynchronously(topic, kvs, producerProps);
    }
    
    /**
     * Send message to specified kafka topic.
     *
     * @param topic         Kafka topic to send messages to
     * @param producerProps Kafka producer configuration
     * @param key           kafka message key
     * @param value         kafka message
     * @throws ExecutionException   if any error during sending message
     * @throws InterruptedException if any interrupted occurs while sending message
     */
    public static <K, V> void sendMessages(String topic, Properties producerProps, K key, V value)
        throws ExecutionException, InterruptedException {
        Collection<KeyValue<K, V>> kvs = new ArrayList<>();
        kvs.add(new KeyValue<K, V>(key, value));
        KafkaTestUtils.produceKeyValuesSynchronously(topic, kvs, producerProps);
    }
    
    /**
     * send messages to kafka topic using provided topic and producer configuration.
     *
     * @param topic         kafka topic to send message to
     * @param producerProps Kafka producer configuration
     * @param bytes         list of kafka messages
     * @throws ExecutionException   if any error during sending message
     * @throws InterruptedException if any interrupted occurs while sending message
     */
    public static void sendMessages(String topic, Properties producerProps, List<byte[]> bytes)
        throws ExecutionException, InterruptedException {
        Collection<KeyValue<Object, Object>> kvs = new ArrayList<>();
        for (int i = 1; i <= bytes.size(); i++) {
            if (i % INT_2 == 0) {
                kvs.add(new KeyValue(bytes.get(i - INT_2), bytes.get(i - 1)));
            }
        }
        KafkaTestUtils.produceKeyValuesSynchronously(topic, kvs, producerProps);
    }
    
    /**
     * read message from specified kafka topic.<br/>
     *
     * @param topic         Kafka topic to read messages from
     * @param consumerProps Kafka consumer configuration
     * @param n             Maximum number of messages to read via the consumer
     * @param waitTime      wait for specified time before reading the next message from kafka
     * @return The KeyValue elements retrieved via the consumer
     * @throws InterruptedException if any interrupted occurs while sending message
     */
    public static List<String[]> getMessages(String topic, Properties consumerProps, int n,
                                             int waitTime)
        throws InterruptedException {
        int timeWaited = 0;
        int increment = INT_2000;
        List<String[]> messages = new ArrayList<>();
        while ((messages.size() < n) && (timeWaited <= waitTime)) {
            messages.addAll(KafkaTestUtils.readMessages(topic, consumerProps, n));
            CountDownLatch countDownLatch = new CountDownLatch(1);
            if (!countDownLatch.await(increment, TimeUnit.MILLISECONDS)) {
                // timeout occurred
            }
            timeWaited = timeWaited + increment;
        }
        return messages;
    }
    
}