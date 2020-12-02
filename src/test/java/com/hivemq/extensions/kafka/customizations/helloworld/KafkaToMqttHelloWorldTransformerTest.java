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

        when(initInput.getMetricRegistry()).thenReturn(new MetricRegistry());
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
}