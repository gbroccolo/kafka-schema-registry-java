package com.example.kafka_avro_clients;

import java.lang.System;
import java.lang.Thread;
import java.lang.Float;
import java.util.function.BiConsumer;

import com.example.kafka_avro_clients.Producer;
import com.example.kafka_avro_clients.Subscriber;

public class Application {
    /*
     * The entrypoint, depending on running mode it runs a producer or a subscriber
     * serialising/deserialising messages in AVRO format using the Schema Registry server
     */

    public static void main(String[] args) throws Exception {
        /*
         * Get all the parameters as ENVs. If subscriber related ones are valued, we assume the
         * node is a subscriber
         */

        String bootstrapServers = System.getenv("BOOTSTRAP_SERVERS");
        String topic = System.getenv("TOPIC");
        String schemaRegistryUrl = System.getenv("SCHEMA_REGISTRY_URL");
        String groupId = System.getenv().getOrDefault("KAFKA_GROUP_ID", "");
        String timeout = System.getenv().getOrDefault("KAFKA_POLL_TIMEOUT", "");

        // wait a bit the Registry server is ready to serve via HTTP
        Thread.sleep((long)(15 * 1000));

        if (groupId == "") {
            // producer node
            Producer producer = new Producer(schemaRegistryUrl, bootstrapServers);

            // this starts an infinite loop, SIGTERM is already handled here
            producer.produce(topic);
        } else {
            // subscriber node
            Subscriber subscriber = new Subscriber(topic, schemaRegistryUrl, bootstrapServers, groupId);

            // pass this closure to the Subscriber's `poll` method, which will process the single
            // record
            BiConsumer<String, Message> singleRecordOperation = (key, message) -> {
                String record = "Consumed record " + key + ":\tstring: " + String.valueOf(message.getStringKey()) + ", number: " + String.valueOf(message.getIntKey());

                System.out.println(record);
            };

            // this starts an infinite loop, SIGTERM is already handled here
            subscriber.poll(Float.parseFloat(timeout), singleRecordOperation);
        }
    }
}
