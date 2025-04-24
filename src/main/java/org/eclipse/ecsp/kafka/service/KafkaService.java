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

package org.eclipse.ecsp.kafka.service;


import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.NotLeaderForPartitionException;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.transform.GenericIgniteEventTransformer;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * {@link KafkaService} contains various utility method to public messages on kafka topics.<br/>
 * this config is applicable if kafka.producer.service.enabled is set to true
 *
 * @author abhishekkumar
 */
@Service
@ConditionalOnProperty(name = "kafka.producer.service.enabled", matchIfMissing = true)
public class KafkaService {

    private static final int KAFKA_PUBLISH_WAIT_MS = 10;
    private static final long KAFKA_ERROR_WAIT_MS = 100;

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(KafkaService.class);
    /**
     * Error message when sending message to kafka topic.
     */
    public static final String ERR_SENDING_MESSAGE = "Error Sending Ignite Event to Kafka: ";

    @Value("${kafka.sink.topic:}")
    private String topic;

    @Value("${kafka.producer.synchronous.push:false}")
    private boolean isSynchronousPublish;

    @Autowired
    private GenericIgniteEventTransformer eventTransformer;

    @Autowired
    private Producer<byte[], byte[]> producer;

    private void sendToSink(String key, IgniteEvent igniteEvent) throws ExecutionException {
        sendToSinkTopic(key, igniteEvent, topic);
    }

    /**
     * Send ignite event to configured source kafka topic on kafka.sink.topic property.
     * this uses default {@link GenericIgniteEventTransformer} <br/>
     * which convert the {@link IgniteEvent} to bytes<br/>
     * this uses {@link IgniteEvent#getVehicleId()} as kafka key
     *
     * @param igniteEvent event to send to kafka topic
     * @throws ExecutionException if fails to send message to kafka
     */
    public void sendIgniteEvent(IgniteEvent igniteEvent) throws ExecutionException {
        try {
            sendToSink(igniteEvent.getVehicleId(), igniteEvent);
        } catch (ExecutionException e) {
            LOGGER.error(igniteEvent, ERR_SENDING_MESSAGE, e);
            throw e;
        }
    }

    /**
     * Send ignite event to configured specified topic in param.
     * this uses default {@link GenericIgniteEventTransformer}<br/>
     * which convert the {@link IgniteEvent} to bytes<br/>
     *
     * @param key         kafka key
     * @param igniteEvent event to send to kafka topic
     * @param onTopic     kafka topic to which message has to be send
     * @throws ExecutionException if fails to send message to kafka
     */
    public void sendIgniteEvent(String key, IgniteEvent igniteEvent, String onTopic)
            throws ExecutionException {
        try {
            sendToSinkTopic(key, igniteEvent, onTopic);
        } catch (ExecutionException e) {
            LOGGER.error(igniteEvent, ERR_SENDING_MESSAGE, e);
            throw e;
        }
    }

    /**
     * Send ignite event to configured specified topic in param.
     * this uses default {@link GenericIgniteEventTransformer}<br/>
     * which convert the {@link IgniteEvent} to bytes<br/>
     *
     * @param key         kafka key
     * @param igniteEvent event to send to kafka topic
     * @param onTopic     kafka topic to which message has to be send
     * @throws ExecutionException if fails to send message to kafka
     */
    private void sendToSinkTopic(String key, IgniteEvent igniteEvent, String onTopic)
            throws ExecutionException {
        LOGGER.debug("Sending key:{} and value:{} to topic:{}", key, igniteEvent, onTopic);
        Future<RecordMetadata> response = producer.send(
                new ProducerRecord<>(onTopic, key.getBytes(StandardCharsets.UTF_8),
                        eventTransformer.toBlob(igniteEvent)));

        if (!isSynchronousPublish) {
            while (!response.isDone()) {
                try {
                    Thread.sleep(KAFKA_PUBLISH_WAIT_MS);
                } catch (InterruptedException exp) {
                    // restore the interrupt status and move on
                    LOGGER.warn(
                            "Interrupted while waiting for response from Kafka for the vehicleId: {} exception: ",
                            igniteEvent.getVehicleId(),
                            exp);
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            if (response.isDone()) {
                handleResponse(key, igniteEvent, onTopic, response);
            }
        } else {
            handleAsycResponse(key, igniteEvent, onTopic, response);
        }
    }

    private void handleAsycResponse(String key, IgniteEvent igniteEvent, String onTopic,
                                    Future<RecordMetadata> response) throws ExecutionException {
        try {
            RecordMetadata responseMetadata = response.get();
            LOGGER.info("Published topic: {}, Partition: {}, Offset: {}", responseMetadata.topic(),
                    responseMetadata.partition(), responseMetadata.offset());
        } catch (InterruptedException e) {
            // restore the interrupt status and move on
            LOGGER.warn("Error in the response: {} for the vehicle: {}", response,
                    igniteEvent.getVehicleId());
            Thread.currentThread().interrupt();
        } catch (NotLeaderForPartitionException nlfpe) {
            try {
                Thread.sleep(KAFKA_ERROR_WAIT_MS);
            } catch (InterruptedException e) {
                // restore the interrupt status and move on
                LOGGER.warn(
                        "Interrupted when waiting to retry publishing to "
                                + "Kafka because of a NotLeaderForPartitionException for the vehicle: {}",
                        response, igniteEvent.getVehicleId());
                Thread.currentThread().interrupt();
                return;
            }
            sendToSinkTopic(key, igniteEvent, onTopic);
        }
    }

    private void handleResponse(String key, IgniteEvent igniteEvent, String onTopic,
                                Future<RecordMetadata> response) throws ExecutionException {
        try {
            RecordMetadata responseMetadata = response.get();
            LOGGER.info("Published asnyc to topic: {}, Partition: {}, Offset: {}", responseMetadata.topic(),
                    responseMetadata.partition(),
                    responseMetadata.offset());
        } catch (InterruptedException e) {
            // restore the interrupt status and move on
            LOGGER.warn(
                    "Interrupted when getting response result for the response: {} for the vehicle: {}",
                    response,
                    igniteEvent.getVehicleId());
            Thread.currentThread().interrupt();
        } catch (NotLeaderForPartitionException nlfpe) {
            LOGGER.warn("Caught exception when publishing to Kafka. Will attempt retry after {}",
                    KAFKA_ERROR_WAIT_MS, nlfpe);
            try {
                Thread.sleep(KAFKA_ERROR_WAIT_MS);
            } catch (InterruptedException e) {
                // restore the interrupt status and move on
                LOGGER.warn(
                        "Interrupted when waiting to retry publishing to Kafka "
                                + "because of a NotLeaderForPartitionException for the vehicle: {}",
                        response, igniteEvent.getVehicleId());
                Thread.currentThread().interrupt();
                return;
            }
            sendToSinkTopic(key, igniteEvent, onTopic);
        }
    }

    /**
     * Send ignite event to configured specified topic in param.
     * this uses default {@link GenericIgniteEventTransformer}<br/>
     * which convert the {@link IgniteEvent} to bytes<br/>
     * this uses {@link IgniteEvent#getVehicleId()} as kafka key
     *
     * @param igniteEvent event to send to kafka topic
     * @param onTopic     kafka topic to which message has to be send
     * @throws ExecutionException if fails to send message to kafka
     */
    public void sendIgniteEventonTopic(IgniteEvent igniteEvent, String onTopic)
            throws ExecutionException {
        try {
            sendToSinkTopic(igniteEvent.getVehicleId(), igniteEvent, onTopic);
        } catch (ExecutionException e) {
            LOGGER.error(igniteEvent, ERR_SENDING_MESSAGE, e);
            throw e;
        }
    }


    /**
     * flush and closing kafka producer.
     *
     * @throws Exception when unable to flush or close kafka producer
     */
    @PreDestroy
    public void cleanUp() throws Exception {
        LOGGER.info("Flushing and closing kafka producer");
        producer.flush();
        producer.close();
    }
}