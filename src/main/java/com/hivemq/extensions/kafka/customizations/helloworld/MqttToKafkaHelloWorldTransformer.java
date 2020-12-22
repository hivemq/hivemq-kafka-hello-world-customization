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

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.packets.publish.PublishPacket;
import com.hivemq.extensions.kafka.api.builders.KafkaRecordBuilder;
import com.hivemq.extensions.kafka.api.model.KafkaCluster;
import com.hivemq.extensions.kafka.api.services.KafkaTopicService;
import com.hivemq.extensions.kafka.api.transformers.mqtttokafka.MqttToKafkaInitInput;
import com.hivemq.extensions.kafka.api.transformers.mqtttokafka.MqttToKafkaInput;
import com.hivemq.extensions.kafka.api.transformers.mqtttokafka.MqttToKafkaOutput;
import com.hivemq.extensions.kafka.api.transformers.mqtttokafka.MqttToKafkaTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This example {@link MqttToKafkaTransformer} accepts an MQTT PUBLISH and tries to create a new Kafka record from it.
 * <p>
 * It performs the following computational steps:
 * <ol>
 *     <li> Convert the MQTT topic to a Kafka topic.
 *     <li> Check if the topic exists on the Kafka cluster.
 *     <li> If it does not, try to create it.
 *     <li> Create a new Kafka record consisting of:
 *         <ul>
 *             <li> The converted topic as Kafka topic.
 *             <li> The payload as value.
 *             <li> All present user properties as header.
 *         </ul>
 *      <li> Give the record to the customization framework for publishing.
 * </ol>
 * <p>
 * An example kafka-configuration.xml enabling this transformer is provided in {@code src/main/resources}.
 *
 * @author Georg Held
 */
public class MqttToKafkaHelloWorldTransformer implements MqttToKafkaTransformer {

    private static final @NotNull Logger log = LoggerFactory.getLogger(MqttToKafkaHelloWorldTransformer.class);

    @Override
    public void init(final @NotNull MqttToKafkaInitInput input) {
        final KafkaCluster kafkaCluster = input.getKafkaCluster();

        log.info(
                "Hello-World-Transformer for Kafka cluster '{}' with boot strap servers '{}' initialized.",
                kafkaCluster.getId(),
                kafkaCluster.getBootstrapServers());
    }

    @Override
    public void transformMqttToKafka(
            final @NotNull MqttToKafkaInput mqttToKafkaInput, final @NotNull MqttToKafkaOutput mqttToKafkaOutput) {

        final PublishPacket publishPacket = mqttToKafkaInput.getPublishPacket();
        final String mqttTopic = publishPacket.getTopic();
        final String kafkaClusterId = mqttToKafkaInput.getKafkaCluster().getId();
        final KafkaTopicService kafkaTopicService = mqttToKafkaInput.getKafkaTopicService();

        // first transform the mqtt topic to a Kafka topic
        final String kafkaTopic = KafkaTopicUtil.mqttToKafkaTopic(mqttTopic);

        // check if kafkaTopic exists on the Kafka cluster
        KafkaTopicService.KafkaTopicState state = kafkaTopicService.getKafkaTopicState(kafkaTopic);
        if (state == KafkaTopicService.KafkaTopicState.MISSING) {
            log.info(
                    "Kafka topic '{}' does not exist on the Kafka cluster '{}'. Creating it.",
                    kafkaTopic,
                    kafkaClusterId);

            // try to create the topic
            state = kafkaTopicService.createKafkaTopic(kafkaTopic);
        }

        if (state == KafkaTopicService.KafkaTopicState.FAILURE) {
            log.warn(
                    "Kafka topic operations for topic '{}' and Kafka cluster '{}' failed. Dropping MQTT message on topic '{}'.",
                    kafkaTopic,
                    kafkaClusterId,
                    mqttTopic);

            return;
        }

        if (log.isTraceEnabled()) {
            log.trace("Pushing a new Kafka record to topic '{}' on cluster '{}'.", kafkaTopic, kafkaClusterId);
        }

        // get a new Kafka record builder
        final KafkaRecordBuilder recordBuilder = mqttToKafkaOutput.newKafkaRecordBuilder()
                // set the Kafka topic
                .topic(kafkaTopic);

        // copy the MQTT payload if present
        publishPacket.getPayload().ifPresent(recordBuilder::value);

        // convert MQTT user properties to Kafka header
        publishPacket.getUserProperties()
                .asList()
                .forEach(userProperty -> recordBuilder.header(userProperty.getName(), userProperty.getValue()));

        // build and set the Kafka record that the HiveMQ Enterprise Extension for Kafka will push
        mqttToKafkaOutput.setKafkaRecords(List.of(recordBuilder.build()));
    }
}
