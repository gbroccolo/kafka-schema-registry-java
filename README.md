# An example of AVRO messages exchanged between Java producers and consumers using the Confluent schema registry

The repository contains the `Dockerfile` used to dockerise the producer/subscriber
nodes, in addition to the `docker-compose` configuration to orchestrate the build
of the following cluster:

* a `zookeeper` node used to configure and as a veto for the Kafka cluster (in case
of replicas enabled)
* a `kafka-broker` node
* a `schema-registry` node to store the AVRO schemas in the cluster
* a `subscriber` node, implementing the definitions in `src/`
* a `producer` node, implementing the definitions in `src/`

**NOTE**: the subscriber node is always able to get the latest version of the schema
from the registry node; also, in case of changes of the AVRO schema, it is possible to
check the compatibility with the latest version of the schema present in the registry
through the `kafka-schema-registry-maven-plugin` during the build with Mavien.

## Schema of the repository

* `src/main/java` - the Java code used to implement the producer and the subscriber
* `src/main/resources` - where the AVRO schema of the messages is stored

## How to run the example

* start up the cluster
```
$ docker-compose up -d
```

* messages are produced every two seconds: to see the consumed, deserialised messages
```
$ docker logs -f subscriber
[...]
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-toolchain/1.0/maven-toolchain-1.0.jar (33 kB at 514 kB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/commons/commons-exec/1.3/commons-exec-1.3.jar (54 kB at 324 kB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/3.0.20/plexus-utils-3.0.20.jar (243 kB at 1.1 MB/s)
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Consumed record 1e64032a-792e-433e-b452-c8969dabeb4f:	string: foo, number: 42
Consumed record 6b3b56cc-7a47-442c-96c5-303134901987:	string: foo, number: 42
Consumed record 37ffd34f-7bfa-49e1-9af4-811af1c98984:	string: foo, number: 42
Consumed record 12d5efc2-dfa7-40b7-81ae-c7b5198fe68a:	string: foo, number: 42
```

**NOTE** the build can take some time. Also, it needs to download all the packages locally
from the Confluent maven repository.

## sending requests to the registry

Schemas, versions, etc. of messages related to the topic can be retrieved via
HTTP from the schema registry:

```
$ curl -X GET http://localhost:8081/subjects
["messages-value"]

$ curl -X GET http://localhost:8081/subjects/messages-value/versions
[1]

$ curl -X GET http://localhost:8081/subjects/messages-value/versions/1
{"subject":"messages-value","version":1,"id":1,"schema":"{\"type\":\"record\",\"name\":\"Name\",\"namespace\":\"com.example.kafka_avro_clients\",\"fields\":[{\"name\":\"stringKey\",\"type\":\"string\"},{\"name\":\"intKey\",\"type\":\"int\"}]}"}
```
