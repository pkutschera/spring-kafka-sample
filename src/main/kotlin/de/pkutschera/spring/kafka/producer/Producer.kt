package de.pkutschera.spring.kafka.producer

import de.pkutschera.spring.kafka.avro.Player
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.ResponseEntity
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Configuration
class KafkaProducerConfig(
    @Value("\${kafka.bootstrapAddress.url}")
    private val serverUrl: String,

    @Value("\${kafka.schemaRegistry.url}")
    private val schemaRegistryUrl: String
) {
    @Bean
    fun producerFactory(): ProducerFactory<String, Any> {
        val producerProperties = HashMap<String, Any>()
        producerProperties[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = serverUrl
        producerProperties[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        producerProperties[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
        producerProperties[KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = "http://localhost:8081"
        producerProperties[KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS] = true
        return DefaultKafkaProducerFactory(producerProperties);
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactory())
    }
}

@RestController
class ProducerController(
    @Value("\${app.topic.name}") private val topic: String,
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {

    @PostMapping("/produce")
    fun produceMessage(@RequestBody message: Player): ResponseEntity<String> {
        val listenableFuture = kafkaTemplate.send(topic, message)
        return ResponseEntity.ok(" message sent to " + listenableFuture.get().producerRecord.topic());
    }
}
