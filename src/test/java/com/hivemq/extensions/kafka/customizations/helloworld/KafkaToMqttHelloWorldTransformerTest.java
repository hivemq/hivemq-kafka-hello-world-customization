/*
 * Copyright 2020-present HiveMQ GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hivemq.extensions.kafka.customizations.helloworld;

import com.codahale.metrics.MetricRegistry;
import com.hivemq.extension.sdk.api.packets.general.UserProperty;
import com.hivemq.extension.sdk.api.packets.publish.PublishPacket;
import com.hivemq.extension.sdk.api.services.builder.PublishBuilder;
import com.hivemq.extension.sdk.api.services.publish.Publish;
import com.hivemq.extensions.kafka.api.builders.KafkaRecordBuilder;
import com.hivemq.extensions.kafka.api.model.KafkaHeader;
import com.hivemq.extensions.kafka.api.model.KafkaHeaders;
import com.hivemq.extensions.kafka.api.model.KafkaRecord;
import com.hivemq.extensions.kafka.api.services.KafkaTopicService;
import com.hivemq.extensions.kafka.api.transformers.kafkatomqtt.KafkaToMqttInitInput;
import com.hivemq.extensions.kafka.api.transformers.kafkatomqtt.KafkaToMqttInput;
import com.hivemq.extensions.kafka.api.transformers.kafkatomqtt.KafkaToMqttOutput;
import com.hivemq.extensions.kafka.api.transformers.mqtttokafka.MqttToKafkaInput;
import com.hivemq.extensions.kafka.api.transformers.mqtttokafka.MqttToKafkaOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class KafkaToMqttHelloWorldTransformerTest {

    // mock objects
    private KafkaToMqttInput input;
    private KafkaToMqttInitInput initInput;
    private KafkaToMqttOutput output;
    private PublishBuilder publishBuilder;
    private KafkaRecord kafkaRecord;
    private KafkaHeaders kafkaHeaders;
    private KafkaHeader kafkaHeader;
    private Publish publish;

    // test object
    private KafkaToMqttHelloWorldTransformer transformer;

    private MetricRegistry metricRegistry;

    @BeforeEach
    void setUp() {
        input = mock(KafkaToMqttInput.class);
        initInput = mock(KafkaToMqttInitInput.class);
        output = mock(KafkaToMqttOutput.class);
        kafkaRecord  = mock(KafkaRecord.class);
        publishBuilder = mock(PublishBuilder.class);
        publish = mock(Publish.class);
        kafkaHeaders = mock(KafkaHeaders.class);
        kafkaHeader = mock(KafkaHeader.class);
        metricRegistry = new MetricRegistry();

        when(input.getKafkaRecord()).thenReturn(kafkaRecord);
        when(output.newPublishBuilder()).thenReturn(publishBuilder);
        transformer= new KafkaToMqttHelloWorldTransformer();

        when(kafkaRecord.getHeaders()).thenReturn(kafkaHeaders);
        when(kafkaRecord.getTopic()).thenReturn("topic");
        when(kafkaRecord.getValue()).thenReturn(Optional.of(ByteBuffer.wrap("test".getBytes())));

        when(kafkaHeaders.asList()).thenReturn(List.of(kafkaHeader));
        when(kafkaHeader.getKey()).thenReturn("test-key");
        when(kafkaHeader.getValueAsString()).thenReturn("test-value");

        when(publishBuilder.build()).thenReturn(publish);
        when(publishBuilder.contentType(any())).thenReturn(publishBuilder);
        when(publishBuilder.topic(anyString())).thenReturn(publishBuilder);
        when(publishBuilder.correlationData(any())).thenReturn(publishBuilder);
        when(publishBuilder.messageExpiryInterval(anyLong())).thenReturn(publishBuilder);
        when(publishBuilder.payloadFormatIndicator(any())).thenReturn(publishBuilder);
        when(publishBuilder.userProperty(anyString(), anyString())).thenReturn(publishBuilder);

        when(initInput.getMetricRegistry()).thenReturn(metricRegistry);
        transformer.init(initInput);
    }

    @Test
    void transformMqttToKafka_setsDataFromKafkaRecord() {
        transformer.transformKafkaToMqtt(input, output);
        verify(publishBuilder).topic(eq(kafkaRecord.getTopic()));
        verify(publishBuilder).payload(eq(kafkaRecord.getValue().get()));
        // work around for a mockito bug
        final String value = kafkaHeader.getValueAsString();
        verify(publishBuilder).userProperty(eq(kafkaHeader.getKey()), eq(value));
        verify(output).setPublishes(eq(List.of(publish)));
    }

    @Test
    void transformMqttToKafka_missingValueIncCounter() {
        when(kafkaRecord.getValue()).thenReturn(Optional.empty());
        transformer.transformKafkaToMqtt(input, output);
        assertEquals(1, metricRegistry.counter(KafkaToMqttHelloWorldTransformer.MISSING_VALUE_COUNTER_NAME).getCount());
    }
}