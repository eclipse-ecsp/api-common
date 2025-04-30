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
import org.apache.kafka.streams.KeyValue;
import org.eclipse.ecsp.domain.AbstractBlobEventData.Encoding;
import org.eclipse.ecsp.domain.BlobDataV1_0;
import org.eclipse.ecsp.domain.IgniteEventSource;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.EventData;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.eclipse.ecsp.testutils.KafkaTestUtils;
import org.eclipse.ecsp.transform.GenericIgniteEventTransformer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * {@link KafkaServiceTest} contains test cases to test kafka service.<br/>
 * publishing message to kafka topics.
 *
 * @author abhishekkumar
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@TestPropertySource("classpath:/application-base.properties")
@ContextConfiguration(initializers = {KafkaServiceTest.Initializer.class})
public class KafkaServiceTest extends CommonTestBase {
    
    static class Initializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                "kafka.broker.url=" + KAFKA_CLUSTER.bootstrapServers()
            ).applyTo(configurableApplicationContext.getEnvironment());
            
            // create topic only once
            KAFKA_CLUSTER.createTopic("test");

            CollectorRegistry.defaultRegistry.clear();
        }
    }
    
    @Autowired
    private KafkaService kafkaService;
    
    @Value("${kafka.sink.topic}")
    private String sinkTopic = "test";
    
    @Autowired
    private GenericIgniteEventTransformer transformer;
    
    @Override
    @Before
    public void setup() {
        super.setup();
    }
    
    @Test
    public void validSendEvent() throws Exception {
        BlobDataV1_0 eventData = new BlobDataV1_0();
        eventData.setEncoding(Encoding.JSON);
        eventData.setEventSource(IgniteEventSource.IGNITE);
        String speedEvent = "{\"EventID\": \"Speed\",\"Version\": \"1.0\",\"Data\": {\"value\":20.0}}";
        eventData.setPayload(speedEvent.getBytes());
        
        try {
            kafkaService.sendIgniteEvent(createIgniteEvent(Version.V1_0, "dummy1", "FOOBAR1", eventData));
        } catch (ExecutionException e) {
            throw new Exception("Error sending event data to kafka: " + e.getMessage());
        }
        
        List<byte[]> messages = KafkaTestUtils.readValues(sinkTopic, consumerProps, 1);
        assertEquals("Expected a single message", 1, messages.size());
        // using transformer get the event
        IgniteEvent transformedEvent = transformer.fromBlob(messages.get(0), Optional.empty());
        assertEquals("EventId should match", "dummy1", transformedEvent.getEventId());
    }
    
    @Test
    public void validSendEventWithTopic() throws Exception {
        BlobDataV1_0 eventData = new BlobDataV1_0();
        eventData.setEncoding(Encoding.JSON);
        eventData.setEventSource(IgniteEventSource.IGNITE);
        String speedEvent = "{\"EventID\": \"Speed\",\"Version\": \"1.0\",\"Data\": {\"value\":20.0}}";
        eventData.setPayload(speedEvent.getBytes());
        
        try {
            kafkaService.sendIgniteEventonTopic(
                createIgniteEvent(Version.V1_0, "dummy2", "FOOBAR2", eventData), sinkTopic);
        } catch (ExecutionException e) {
            throw new Exception("Error sending event data to kafka: " + e.getMessage());
        }
        
        List<byte[]> messages = KafkaTestUtils.readValues(sinkTopic, consumerProps, 1);
        assertEquals("Expected a single message", 1, messages.size());
        // using transformer get the event
        IgniteEvent transformedEvent = transformer.fromBlob(messages.get(0), Optional.empty());
        assertEquals("EventId should match", "dummy2", transformedEvent.getEventId());
    }
    
    @Test
    public void validSendEventWithKey() {
        BlobDataV1_0 eventData = new BlobDataV1_0();
        eventData.setEncoding(Encoding.JSON);
        eventData.setEventSource(IgniteEventSource.IGNITE);
        String speedEvent = "{\"EventID\": \"Speed\",\"Version\": \"1.0\",\"Data\": {\"value\":20.0}}";
        eventData.setPayload(speedEvent.getBytes());
        String userId = "userId010101";
        
        try {
            kafkaService.sendIgniteEvent(userId,
                createIgniteEvent(Version.V4_0, "dummy3", "FOOBAR3", eventData), sinkTopic);
        } catch (ExecutionException e) {
            fail("Error sending event data to kafka: " + e.getMessage());
        }
        
        List<KeyValue<Object, Object>> messages =
            KafkaTestUtils.readKeyValues(sinkTopic, consumerProps, 1);
        assertEquals("Expected a single message", 1, messages.size());
        assertEquals(userId, new String(((byte[]) messages.get(0).key)));
        
        // using transformer get the event
        IgniteEvent transformedEvent =
            transformer.fromBlob((byte[]) messages.get(0).value, Optional.empty());
        assertEquals(Version.V4_0, transformedEvent.getVersion());
        assertEquals("Event id should match with that of the sent message", "dummy3",
            transformedEvent.getEventId());
    }
    
    @After
    public void tearDown() throws Exception {
        super.teardown();
    }
    
    private IgniteEvent createIgniteEvent(Version version, String eventId, String vehicleId,
                                          EventData eventData) {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventData(eventData);
        igniteEvent.setEventId(eventId);
        igniteEvent.setVersion(version);
        igniteEvent.setVehicleId(vehicleId);
        igniteEvent.setTimestamp(System.currentTimeMillis());
        return igniteEvent;
    }
}
