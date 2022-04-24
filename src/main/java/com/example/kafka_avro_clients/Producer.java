package com.example.kafka_avro_clients;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.common.errors.SerializationException;

import java.util.Properties;
import java.lang.System;
import java.lang.Thread;
import java.lang.Runtime;
import java.util.UUID;

public class Producer {
    /*
     * Define the producer client which takes care of publishing messages serialising them in
     * AVRO format and pushing the schema to the Registry
     */

    private static final Properties props = new Properties();
    private final KafkaProducer<String, Message> producer;

    public Producer(String schemaRegistryUrl, String bootstrapServers) {
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        producer = new KafkaProducer<String, Message>(props);
    }

    public void produce(String topic) throws InterruptedException {
        /*
         * Produce messages in loop - NOTE the message is abstracted as a class `Message`
         * which source code is generated through the `avro-maven-plugin` included in the pom file.
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
                // Create the message using the abstraction `Message`
                final Message message = new Message("foo", 42);
                final ProducerRecord<String, Message> record = new ProducerRecord<String, Message>(topic, UUID.randomUUID().toString(), message);
                producer.send(record);
                Thread.sleep(2000L);
            }
        } finally {
            // flush the last published messages before definitely close the client
            producer.flush();
        }
    }
}
