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

package org.eclipse.ecsp;

import io.prometheus.client.CollectorRegistry;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.eclipse.ecsp.configurations.KafkaConfig;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * {@link KafkaProducerTest} contains test cases for kafka producer creation using system env config.
 *
 * @author abhishekkumar
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:/application-base-kafka.properties")
@Profile("test")
public class KafkaProducerTest extends CommonTestBase {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerTest.class);
    
    static {
        // Workaround to avoid duplicate metrics registration in case of Spring
        // Boot dev-tools restarts
        CollectorRegistry.defaultRegistry.clear();
    }
    
    @Autowired
    ApplicationContext apc;
    
    @Autowired
    KafkaConfig kafkaConfig;
    
    private Producer producer;
    
    @Test
    public void testKafkaProducerProperties() throws Exception {
        
        kafkaConfig.initialize();
        producer = kafkaConfig.producer();
        
        KafkaProducer<String, String> kafkaProduce = (KafkaProducer) kafkaConfig.producer();
        LOGGER.debug("Kafak Producer properties : {}", kafkaProduce.toString());
        
        LOGGER.debug("Producer properties : {}", producer);
        
        Assert.assertNotNull(producer);
    }
    
}
