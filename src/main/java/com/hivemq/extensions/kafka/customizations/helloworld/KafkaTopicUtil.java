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

/**
 * @author Georg Held
 */
public class KafkaTopicUtil {

    /**
     * Util class should not be instantiable.
     */
    private KafkaTopicUtil() {}

    /**
     * Converts a MQTT style topic - different topic levels separated by a forward slash '/' - to a Kafka style topic -
     * different topic levels separated by a dot '.'.
     * <p>
     * The returned string is not guaranteed to be a valid Kafka topic.
     *
     * @param mqttTopic a single or multilevel MQTT topic
     * @return a single or multilevel Kafka topic
     * @see <a href="https://stackoverflow.com/questions/37062904/what-are-apache-kafka-topic-name-limitations">What
     *         are Apache Kafka topic name limitations?</a>
     */
    public static @NotNull String mqttToKafkaTopic(final @NotNull String mqttTopic) {
        return mqttTopic.replaceAll("/", ".");
    }
}
