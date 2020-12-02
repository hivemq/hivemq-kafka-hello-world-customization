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


    private @Nullable MetricRegistry metricRegistry;
    private @Nullable Counter missingValueCounter;

    @Override
    public void init(@NotNull final KafkaToMqttInitInput input) {
        this.metricRegistry = input.getMetricRegistry();
        // build any custom metric based on your business logic and needs
        this.missingValueCounter = metricRegistry.counter("com.hivemq.hello-world-example.missing-value.count");
    }


    @Override
    public void transformKafkaToMqtt(@NotNull final KafkaToMqttInput kafkaToMqttInput,
                                     @NotNull final KafkaToMqttOutput kafkaToMqttOutput) {
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
