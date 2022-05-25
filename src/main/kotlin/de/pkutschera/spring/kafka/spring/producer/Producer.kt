package de.pkutschera.spring.kafka.spring.producer

import de.pkutschera.spring.kafka.spring.data.Player
import de.pkutschera.spring.kafka.spring.data.PlayerSerializer
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
    private val url: String
) {
    @Bean
    fun producerFactory(): ProducerFactory<String, Any> {
        val props = HashMap<String, Any>()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = url
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = PlayerSerializer::class.java
        return DefaultKafkaProducerFactory(props);
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
