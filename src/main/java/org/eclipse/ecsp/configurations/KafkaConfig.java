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
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.config.internals.BrokerSecurityConfigs;
import org.eclipse.ecsp.constants.Constants;
import org.eclipse.ecsp.utils.ObjectUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import java.util.Map;
import java.util.Properties;

/**
 * {@link KafkaConfig} spring config class which contains kafka related configs.
 * this config is applicable if kafka.producer.service.enabled is set to true
 *
 * @author abhishekkumar
 */
@Configuration
@ConditionalOnProperty(name = "kafka.producer.service.enabled", matchIfMissing = true)
@DependsOn("vaultClient")
public class KafkaConfig {
    
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(KafkaConfig.class);

    /**
     * RETRIES.
     */
    public static final int RETRIES = 3;
    
    @Value("${kafka.broker.url}")
    private String brokerUrl;
    
    @Value("${kafka.acks}")
    private String acks;
    
    @Value("${kafka.linger.ms:50}")
    private String lingerMs;
    
    @Value("${kafka.key.serializer}")
    private String serializerKey;
    
    @Value("${kafka.value.serializer}")
    private String serializerValue;
    
    @Value("${kafka.batch.size}")
    private String batchSize;
    
    @Value("${kafka.buffer.memory}")
    private String bufferMemory;
    
    @Value("${kafka.ssl.enable:}")
    private String sslEnabled;
    
    @Value("${kafka.ssl.client.auth:}")
    private String clientAuth;
    
    @Value("${kafka.client.keystore:}")
    private String kafkaKeystore;
    
    @Value("${kafka.client.keystore.password:}")
    private String kafkaKeystorePassword;
    
    @Value("${kafka.client.key.password:}")
    private String kafkaKeyPassword;
    
    @Value("${kafka.client.truststore:}")
    private String kafkaTrustStore;
    
    @Value("${kafka.client.truststore.password:}")
    private String kafkaTruststorePassword;
    
    @Value("${vault.enabled:false}")
    private boolean vaultEnabled;
    
    @Value("${kafka.max.request.size:1048576}")
    private String maxRequestSize;
    
    @Value("${kafka.request.timeout.ms:30000}")
    private String requestTimeoutMs;
    
    @Value("${kafka.delivery.timeout.ms:120000}")
    private String deliveryTimeoutMs;

    @Value("${kafka.compression.type:none}")
    private String compressionType;
    
    /**
     * This method validate and process kafka config from system properties.
     * This is applicable if vault.enabled is set to false
     *
     * @throws Exception error if exception occurred during producer initialization.
     */
    @PostConstruct
    public void initialize() throws Exception {
        if (Boolean.parseBoolean(sslEnabled) && vaultEnabled) {
            try {
                Map<Object, Object> properties = System.getProperties();
                LOGGER.info("Obtained kafka config from system properties");
                
                kafkaKeystorePassword = (String) properties.get(
                    Constants.KAFKA_CLIENT_KEYSTORE_PASS_KEY.replace(Constants.UNDER_SCORE,
                        Constants.DOT));
                kafkaKeyPassword = (String) properties.get(
                    Constants.KAFKA_CLIENT_KEY_PASS_KEY.replace(Constants.UNDER_SCORE,
                        Constants.DOT));
                kafkaTruststorePassword = (String) properties.get(
                    Constants.KAFKA_CLIENT_TRUSTSTORE_PASS_KEY.replace(Constants.UNDER_SCORE,
                        Constants.DOT));
                
                ObjectUtils.requireNonEmpty(kafkaKeystorePassword,
                    "kafkaKeystorePassword is either null or empty");
                ObjectUtils.requireNonEmpty(kafkaKeyPassword, "kafkaKeyPassword is either null or empty");
                ObjectUtils.requireNonEmpty(kafkaTruststorePassword,
                    "kafkaTruststorePassword is either null or empty");
                
            } catch (Exception e) {
                throw new RuntimeException(
                    "Exception while loading  kafka config from system properties with message :- " + e.getMessage());
            }
        }
    }

    /**
     * This method is a getter for properties.
     *
     * @return Properties
     */
    private Properties getProperties() {
        Properties props = new Properties();
        
        LOGGER.info("URL used to connect to kafka:{}", brokerUrl);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
        props.put(ProducerConfig.ACKS_CONFIG, acks);
        props.put(ProducerConfig.RETRIES_CONFIG, RETRIES);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        props.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, serializerKey);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, serializerValue);
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, maxRequestSize);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeoutMs);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);

        LOGGER.info("Kafka SSL enabled : {}", sslEnabled);
        if (Boolean.parseBoolean(sslEnabled)) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
            props.put(BrokerSecurityConfigs.SSL_CLIENT_AUTH_CONFIG, clientAuth);
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, kafkaKeystore);
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, kafkaKeystorePassword);
            props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, kafkaKeyPassword);
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, kafkaTrustStore);
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, kafkaTruststorePassword);
            LOGGER.info("Kafka SSL properties set");
        }
        
        return props;
    }

    /**
     * Create Kafka producer bean.
     *
     * @param <K> generic key type
     * @param <V> generic value type
     *
     * @return instance of {@link Producer}
     * @throws Exception error if exception occurred during producer initialization.
     */
    @Bean
    public <K, V> Producer<K, V> producer() throws Exception {
        Producer<K, V> producer = new KafkaProducer<K, V>(getProperties());
        return producer;
    }
}