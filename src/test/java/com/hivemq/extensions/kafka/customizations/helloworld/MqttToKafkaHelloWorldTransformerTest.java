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

import com.hivemq.extension.sdk.api.packets.general.UserProperties;
import com.hivemq.extension.sdk.api.packets.general.UserProperty;
import com.hivemq.extension.sdk.api.packets.publish.PublishPacket;
import com.hivemq.extensions.kafka.api.builders.KafkaRecordBuilder;
import com.hivemq.extensions.kafka.api.model.KafkaCluster;
import com.hivemq.extensions.kafka.api.model.KafkaRecord;
import com.hivemq.extensions.kafka.api.services.KafkaTopicService;
import com.hivemq.extensions.kafka.api.transformers.mqtttokafka.MqttToKafkaInput;
import com.hivemq.extensions.kafka.api.transformers.mqtttokafka.MqttToKafkaOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MqttToKafkaHelloWorldTransformerTest {

    // mock objects
    private MqttToKafkaInput input;
    private MqttToKafkaOutput output;
    private KafkaTopicService topicService;
    private KafkaRecordBuilder recordBuilder;
    private KafkaRecord kafkaRecord;
    private PublishPacket publishPacket;
    private UserProperty userProperty;

    // test object
    private MqttToKafkaHelloWorldTransformer transformer;

    @BeforeEach
    void setUp() {
        input = mock(MqttToKafkaInput.class);
        output = mock(MqttToKafkaOutput.class);
        topicService = mock(KafkaTopicService.class);
        recordBuilder = mock(KafkaRecordBuilder.class);
        kafkaRecord = mock(KafkaRecord.class);
        publishPacket = mock(PublishPacket.class);

        when(input.getPublishPacket()).thenReturn(publishPacket);
        final KafkaCluster kafkaCluster = mock(KafkaCluster.class);
        when(input.getKafkaCluster()).thenReturn(kafkaCluster);
        when(input.getKafkaTopicService()).thenReturn(topicService);

        when(output.newKafkaRecordBuilder()).thenReturn(recordBuilder);

        when(topicService.getKafkaTopicState(anyString())).thenReturn(KafkaTopicService.KafkaTopicState.EXISTS);
        when(topicService.createKafkaTopic(anyString())).thenReturn(KafkaTopicService.KafkaTopicState.CREATED);

        when(recordBuilder.topic(anyString())).thenReturn(recordBuilder);
        when(recordBuilder.value(any(ByteBuffer.class))).thenReturn(recordBuilder);
        when(recordBuilder.header(anyString(), anyString())).thenReturn(recordBuilder);

        when(recordBuilder.build()).thenReturn(kafkaRecord);

        when(publishPacket.getTopic()).thenReturn("test/topic");
        when(publishPacket.getPayload()).thenReturn(Optional.of(ByteBuffer.wrap("test-payload".getBytes(StandardCharsets.UTF_8))));
        final UserProperties userProperties = mock(UserProperties.class);
        userProperty = mock(UserProperty.class);
        when(userProperties.asList()).thenReturn(List.of(userProperty));
        when(userProperty.getName()).thenReturn("test-name");
        when(userProperty.getValue()).thenReturn("test-value");
        when(publishPacket.getUserProperties()).thenReturn(userProperties);

        transformer = new MqttToKafkaHelloWorldTransformer();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void transformMqttToKafka_setsDataFromPublishPacket() {
        transformer.transformMqttToKafka(input, output);

        verify(recordBuilder).topic(eq(KafkaTopicUtil.mqttToKafkaTopic(publishPacket.getTopic())));
        verify(recordBuilder).value(eq(publishPacket.getPayload().get()));
        // work around for a mockito bug
        final String value = userProperty.getValue();
        verify(recordBuilder).header(eq(userProperty.getName()), eq(value));
        verify(output).setKafkaRecords(eq(List.of(kafkaRecord)));
    }

    @Test
    void transformMqttToKafka_queriesForTopicExistence() {
        transformer.transformMqttToKafka(input, output);

        verify(topicService).getKafkaTopicState(eq(KafkaTopicUtil.mqttToKafkaTopic(publishPacket.getTopic())));
    }

    @Test
    void transformMqttToKafka_createsMissingTopic() {
        final String kafkaTopic = KafkaTopicUtil.mqttToKafkaTopic(publishPacket.getTopic());
        when(topicService.getKafkaTopicState(eq(kafkaTopic))).thenReturn(KafkaTopicService.KafkaTopicState.MISSING);
        transformer.transformMqttToKafka(input, output);

        verify(topicService).createKafkaTopic(eq(kafkaTopic));
    }

    @Test
    void transformMqttToKafka_abortsAfterFailedTopicQuery() {
        final String kafkaTopic = KafkaTopicUtil.mqttToKafkaTopic(publishPacket.getTopic());
        when(topicService.getKafkaTopicState(eq(kafkaTopic))).thenReturn(KafkaTopicService.KafkaTopicState.FAILURE);
        transformer.transformMqttToKafka(input, output);

        verify(topicService, never()).createKafkaTopic(anyString());
        verify(output, never()).setKafkaRecords(anyList());
    }

    @Test
    void transformMqttToKafka_abortsAfterFailedTopicCreation() {
        final String kafkaTopic = KafkaTopicUtil.mqttToKafkaTopic(publishPacket.getTopic());
        when(topicService.getKafkaTopicState(eq(kafkaTopic))).thenReturn(KafkaTopicService.KafkaTopicState.MISSING);
        when(topicService.createKafkaTopic(eq(kafkaTopic))).thenReturn(KafkaTopicService.KafkaTopicState.FAILURE);
        transformer.transformMqttToKafka(input, output);

        verify(output, never()).setKafkaRecords(anyList());
    }
}