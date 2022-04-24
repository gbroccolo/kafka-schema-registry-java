FROM openjdk:11

COPY . /source/

RUN apt update && apt install -y maven && \
    cd /source && mvn clean compile package -DschemaRegistryUrl=http://schema-registry:8081 -Dcheckstyle.skip

WORKDIR /source

CMD ["mvn", "exec:java", "-Dexec.mainClass=com.example.kafka_avro_clients.Application"]
