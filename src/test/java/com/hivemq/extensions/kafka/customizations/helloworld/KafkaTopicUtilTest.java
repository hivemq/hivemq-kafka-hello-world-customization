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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KafkaTopicUtilTest {

    @Test
    void mqttToKafkaTopic_singleLevelTopic() {
        final String mqttTopic = "my-topic";
        final String kafkaTopic = "my-topic";

        assertEquals(kafkaTopic, KafkaTopicUtil.mqttToKafkaTopic(mqttTopic));
    }

    @Test
    void mqttToKafkaTopic_multiLevelTopic() {
        final String mqttTopic = "my-topic/with/levels";
        final String kafkaTopic = "my-topic.with.levels";

        assertEquals(kafkaTopic, KafkaTopicUtil.mqttToKafkaTopic(mqttTopic));
    }
}