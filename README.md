# Getting Started
Spring Boot Sample Using Kafka

## About Kafka
* Apache Kafka is an event streaming platform
* An event is any type of action with a description what happened
* Kafka is based on the abstraction of a distributed commit log
* The commit log is distributed into partitions
* Kafka models events as key/value pairs
* Internally Kafka serializes and deserializes key/value pairs between programming language objects and an internal format
* As internal format JSON, JSON Schema, Avro, or Protobuf can be used
* Values are typically the serialized representation of an application domain object or some form of raw message input, like the output of a sensor
* Keys are often primitive types like strings or integers and used as an identifier of some entity in the system
* A topic is a log of events:
  * An event is appended at the end
  * An event is immutable
  * A specific event can be found by scanning the log entries
* Every topic can be configured to expire data after a certain time frame or to retain messages indefinitely
* The logs which represent Kafka topics are files stored on disk
* Partitioning breaks a topic into multiple logs, each can live on a separate node in the Kafka cluster
* If a message has no key, subsequent messages will be distributed round-robin among all the partitions If the message has a key, the destination partition will be computed from a hash of the key
* Apache Kafka is composed of a network of machines called brokers running a separate Kafka process
* Each broker hosts some set of partitions
* Brokers can also handle replication of partitions between each other

## Starting the application
### Start a local Kafka broker: 
      ./bin/kafka-server-start ./etc/kafka/kraft/server.properties
### Start the spring boot application
      ./gradlew bootRun

## Reference Documentation

For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.6.7/gradle-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.6.7/gradle-plugin/reference/html/#build-image)
* [Coroutines section of the Spring Framework Documentation](https://docs.spring.io/spring/docs/5.3.19/spring-framework-reference/languages.html#coroutines)
* [Spring for Apache Kafka](https://docs.spring.io/spring-boot/docs/2.6.7/reference/htmlsingle/#boot-features-kafka)
* [Spring Reactive Web](https://docs.spring.io/spring-boot/docs/2.6.7/reference/htmlsingle/#web.reactive)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/2.6.7/reference/htmlsingle/#using-boot-devtools)
* [Spring Configuration Processor](https://docs.spring.io/spring-boot/docs/2.6.7/reference/htmlsingle/#configuration-metadata-annotation-processor)
* [Sleuth](https://docs.spring.io/spring-cloud-sleuth/docs/current/reference/htmlsingle/spring-cloud-sleuth.html)

