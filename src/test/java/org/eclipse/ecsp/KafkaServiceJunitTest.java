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

import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.NotLeaderForPartitionException;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.eclipse.ecsp.domain.AbstractBlobEventData.Encoding;
import org.eclipse.ecsp.domain.BlobDataV1_0;
import org.eclipse.ecsp.domain.IgniteEventSource;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.eclipse.ecsp.transform.GenericIgniteEventTransformer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link KafkaServiceJunitTest} contains test cases for {@link KafkaService}.<br/>
 * sending messages to kafka.
 *
 * @author abhishekkumar
 */
@RunWith(MockitoJUnitRunner.class)
public class KafkaServiceJunitTest {

    private KafkaService kafkaService;

    private String sinkTopic = "test";

    @Mock
    private GenericIgniteEventTransformer transformer;

    MockProducer<byte[], byte[]> producer;

    /**
     * preparing kafka service with mock kafka producer.
     *
     * @throws Exception if any error occur during setup
     */
    @Before
    public void setup() throws Exception {
        producer = new MockProducer<>(true, new ByteArraySerializer(), new ByteArraySerializer());
        kafkaService = new KafkaService(producer, transformer);
        setProducer(producer);
        ReflectionTestUtils.setField(kafkaService, "isSynchronousPublish", false);
        ReflectionTestUtils.setField(kafkaService, "topic", sinkTopic);
    }

    @Test
    public void validSendEvent() throws Exception {
        kafkaService.sendIgniteEvent("userId010101", createIgniteEvent(Version.V1_0, "dummy", "FOOBAR"),
                sinkTopic);
        assertEquals(1, producer.history().size());
    }

    @Test
    public void validSendEventSinkTopic() throws Exception {
        kafkaService.sendIgniteEvent(createIgniteEvent(Version.V1_0, "dummy", "FOOBAR"));
        assertEquals(1, producer.history().size());
    }

    @Test
    public void validSendEventOnSinkTopic() throws Exception {
        kafkaService.sendIgniteEventonTopic(createIgniteEvent(Version.V1_0, "dummy", "FOOBAR"),
                sinkTopic);
        assertEquals(1, producer.history().size());
    }

    @Test
    public void validSendEventAsync() throws Exception {
        ReflectionTestUtils.setField(kafkaService, "isSynchronousPublish", true);
        MockProducer<byte[], byte[]> mockProducer =
                new MockProducer<>(true, new ByteArraySerializer(), new ByteArraySerializer());
        setProducer(mockProducer);
        kafkaService.sendIgniteEvent("userId010101", createIgniteEvent(Version.V1_0, "dummy", "FOOBAR"),
                sinkTopic);
        assertEquals(1, mockProducer.history().size());
    }

    @Test
    public void sendEventException() throws Exception {
        ReflectionTestUtils.setField(kafkaService, "isSynchronousPublish", true);
        MockProducer<byte[], byte[]> mockProducer = Mockito.mock(MockProducer.class);
        setProducer(mockProducer);
        Future<RecordMetadata> future = Mockito.mock(Future.class);
        when(mockProducer.send(any(ProducerRecord.class))).thenReturn(future);
        doThrow(InterruptedException.class).when(future).get();
        kafkaService.sendIgniteEvent("userId010101", createIgniteEvent(Version.V1_0, "dummy", "FOOBAR"),
                sinkTopic);
        verify(mockProducer, atLeastOnce()).send(any(ProducerRecord.class));
    }

    @Test(expected = RuntimeException.class)
    public void sendEventRuntimeException() throws Exception {
        ReflectionTestUtils.setField(kafkaService, "isSynchronousPublish", true);
        MockProducer<byte[], byte[]> mockProducer = Mockito.mock(MockProducer.class);
        setProducer(mockProducer);
        Future<RecordMetadata> future = Mockito.mock(Future.class);
        when(mockProducer.send(any(ProducerRecord.class))).thenReturn(future)
                .thenThrow(RuntimeException.class);
        doThrow(NotLeaderForPartitionException.class).when(future).get();
        kafkaService.sendIgniteEvent("userId010101", createIgniteEvent(Version.V1_0, "dummy", "FOOBAR"),
                sinkTopic);
    }

    @Test
    public void sendEventInterruptedException() throws Exception {
        ReflectionTestUtils.setField(kafkaService, "isSynchronousPublish", false);
        MockProducer<byte[], byte[]> mockProducer = Mockito.mock(MockProducer.class);
        setProducer(mockProducer);
        Future<RecordMetadata> future = Mockito.mock(Future.class);
        when(mockProducer.send(any(ProducerRecord.class))).thenReturn(future);
        doThrow(InterruptedException.class).when(future).get();
        doReturn(true).when(future).isDone();
        kafkaService.sendIgniteEvent("userId010101", createIgniteEvent(Version.V1_0, "dummy", "FOOBAR"),
                sinkTopic);
        Mockito.verify(future, atLeastOnce()).get();
    }

    @Test(expected = ExecutionException.class)
    public void sendEventExecutionException() throws Exception {
        ReflectionTestUtils.setField(kafkaService, "isSynchronousPublish", false);
        MockProducer<byte[], byte[]> mockProducer = Mockito.mock(MockProducer.class);
        setProducer(mockProducer);
        Future<RecordMetadata> future = Mockito.mock(Future.class);
        when(mockProducer.send(any(ProducerRecord.class))).thenReturn(future);
        doThrow(ExecutionException.class).when(future).get();
        doReturn(true).when(future).isDone();
        kafkaService.sendIgniteEvent("userId010101", createIgniteEvent(Version.V1_0, "dummy", "FOOBAR"),
                sinkTopic);
    }

    @Test(expected = ExecutionException.class)
    public void sendEventOnTopicException() throws Exception {
        ReflectionTestUtils.setField(kafkaService, "isSynchronousPublish", false);
        MockProducer<byte[], byte[]> mockProducer = Mockito.mock(MockProducer.class);
        setProducer(mockProducer);
        Future<RecordMetadata> future = Mockito.mock(Future.class);
        when(mockProducer.send(any(ProducerRecord.class))).thenReturn(future);
        doThrow(ExecutionException.class).when(future).get();
        doReturn(true).when(future).isDone();
        kafkaService.sendIgniteEventonTopic(createIgniteEvent(Version.V1_0, "dummy", "FOOBAR"),
                sinkTopic);
    }

    @Test(expected = ExecutionException.class)
    public void sendIgniteEventException() throws Exception {
        ReflectionTestUtils.setField(kafkaService, "isSynchronousPublish", false);
        MockProducer<byte[], byte[]> mockProducer = Mockito.mock(MockProducer.class);
        setProducer(mockProducer);
        Future<RecordMetadata> future = Mockito.mock(Future.class);
        when(mockProducer.send(any(ProducerRecord.class))).thenReturn(future);
        doThrow(ExecutionException.class).when(future).get();
        doReturn(true).when(future).isDone();
        kafkaService.sendIgniteEvent(createIgniteEvent(Version.V1_0, "dummy", "FOOBAR"));
    }

    @Test(expected = RuntimeException.class)
    public void sendIgniteEventPartitionException() throws Exception {
        ReflectionTestUtils.setField(kafkaService, "isSynchronousPublish", false);
        MockProducer<byte[], byte[]> mockProducer = Mockito.mock(MockProducer.class);
        setProducer(mockProducer);
        Future<RecordMetadata> future = Mockito.mock(Future.class);
        when(mockProducer.send(any(ProducerRecord.class))).thenReturn(future)
                .thenThrow(RuntimeException.class);
        doThrow(NotLeaderForPartitionException.class).when(future).get();
        doReturn(true).when(future).isDone();
        kafkaService.sendIgniteEvent(createIgniteEvent(Version.V1_0, "dummy", "FOOBAR"));
    }

    private IgniteEvent createIgniteEvent(Version version, String eventId, String vehicleId) {
        BlobDataV1_0 eventData = new BlobDataV1_0();
        eventData.setEncoding(Encoding.JSON);
        eventData.setEventSource(IgniteEventSource.IGNITE);
        String speedEvent = "{\"EventID\": \"Speed\",\"Version\": \"1.0\",\"Data\": {\"value\":20.0}}";
        eventData.setPayload(speedEvent.getBytes());
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventData(eventData);
        igniteEvent.setEventId(eventId);
        igniteEvent.setVersion(version);
        igniteEvent.setVehicleId(vehicleId);
        igniteEvent.setTimestamp(System.currentTimeMillis());
        return igniteEvent;
    }

    @Test
    public void kafkaCleanup() throws Exception {
        MockProducer<byte[], byte[]> producer =
                new MockProducer<>(false, new ByteArraySerializer(), new ByteArraySerializer());
        setProducer(producer);
        kafkaService.cleanUp();
        assertTrue(producer.closed());
    }

    public void setProducer(MockProducer mockProducer) {
        ReflectionTestUtils.setField(kafkaService, "producer", mockProducer);
    }
}