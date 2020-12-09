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

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.annotations.Nullable;
import com.hivemq.extension.sdk.api.services.builder.PublishBuilder;
import com.hivemq.extensions.kafka.api.model.KafkaRecord;
import com.hivemq.extensions.kafka.api.transformers.kafkatomqtt.KafkaToMqttInitInput;
import com.hivemq.extensions.kafka.api.transformers.kafkatomqtt.KafkaToMqttInput;
import com.hivemq.extensions.kafka.api.transformers.kafkatomqtt.KafkaToMqttOutput;
import com.hivemq.extensions.kafka.api.transformers.kafkatomqtt.KafkaToMqttTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This example {@link KafkaToMqttTransformer} accepts a Kafka record and tries to create a new MQTT PUBLISH from it.
 * <p>
 * It performs the following computational steps:
 * <ol>
 *     <li> Convert the Kafka topic to a MQTT topic.
 *     <li> Create a new MQTT publish consisting of:
 *         <ul>
 *             <li> The kafka topic as MQTT topic.
 *             <li> The value as payload
 *             <li> All present kafka header as user properties.
 *         </ul>
 *      <li> Give the MQTT publish to the customization framework for publishing.
 * </ol>
 * <p>
 * An example kafka-configuration.xml enabling this transformer is provided in {@code src/main/resources}.
 *
 * @author Daniel Krüger
 */
public class KafkaToMqttHelloWorldTransformer implements KafkaToMqttTransformer {

    private static final @NotNull Logger log = LoggerFactory.getLogger(KafkaToMqttHelloWorldTransformer.class);

    public static final @NotNull String MISSING_VALUE_COUNTER_NAME = "com.hivemq.hello-world-example.missing-value.count";

    private @Nullable MetricRegistry metricRegistry;
    private @Nullable Counter missingValueCounter;

    @Override
    public void init(final @NotNull KafkaToMqttInitInput input) {
        this.metricRegistry = input.getMetricRegistry();
        // build any custom metric based on your business logic and needs
        this.missingValueCounter = metricRegistry.counter(MISSING_VALUE_COUNTER_NAME);
    }


    @Override
    public void transformKafkaToMqtt(final @NotNull KafkaToMqttInput kafkaToMqttInput,
                                     final @NotNull KafkaToMqttOutput kafkaToMqttOutput) {
        // get the Kafka record from the input
        final KafkaRecord kafkaRecord = kafkaToMqttInput.getKafkaRecord();

        // get the PublishBuilder object from the output
        final PublishBuilder publishBuilder = kafkaToMqttOutput.newPublishBuilder().topic(kafkaRecord.getTopic());

        // set kafka record value as payload, if present. Otherwise increase the missing kafka record value counter.
        kafkaRecord.getValue().ifPresentOrElse(publishBuilder::payload, missingValueCounter::inc);

        // convert Kafka header to MQTT user properties
        kafkaRecord.getHeaders()
                .asList()
                .forEach(kafkaHeader -> publishBuilder.userProperty(kafkaHeader.getKey(), kafkaHeader.getValueAsString()));

        kafkaToMqttOutput.setPublishes(List.of(publishBuilder.build()));
    }
}
