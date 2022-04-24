package com.example.kafka_avro_clients;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Properties;
import java.util.Collections;
import java.time.Duration;
import java.lang.System;
import java.lang.Thread;
import java.lang.Runtime;
import java.util.function.BiConsumer;


public class Subscriber {
    /*
     * Define the subscriber client which takes care of consuming messages deserialising the
     * AVRO format using the schema stored in the Registry
     */

    private static final Properties props = new Properties();
    private final KafkaConsumer<String, Message> consumer;

    public Subscriber(String topic, String schemaRegistryUrl, String bootstrapServers, String groupId) {
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));
    }

    public void poll(float timeout, BiConsumer<String, Message> singleRecordOperation) throws InterruptedException {
        /*
         * Autocommit is in place, just configure the timeout in the poll. Apply the
         * `singleRecordOperation` to each consumed message.
         *
         * NOTE: timeout needs to be expressed in millisecs, as an integer.
         */

        Runtime.getRuntime().addShutdownHook(
            new Thread() {
                @Override
                public void run() {
                    System.out.println("Shutting down...");
                }
            }
        );

        try {
            while (true) {
                final ConsumerRecords<String, Message> records = consumer.poll(Duration.ofMillis((int)(timeout * 1000.)));

                for (final ConsumerRecord<String, Message> record : records) {
                    final String key = record.key();
                    final Message value = record.value();

                    singleRecordOperation.accept(key, value);
                }
            }
        } finally {
            consumer.close();
        }
    }
}
