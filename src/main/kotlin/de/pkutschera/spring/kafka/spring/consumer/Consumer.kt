package de.pkutschera.spring.kafka.spring.consumer

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka

val logger = KotlinLogging.logger {}

@EnableKafka
@Configuration
class KafkaConsumerConfig(
    @Value("\${kafka.bootstrapAddress.localhost}")
    private val servers: String,

    @Value("\${app.topic.name}")
    private val topic: String,

    @Value("\${app.groupid}")
    private val groupId: String,
)

@Component
class KotlinConsumer {
    @KafkaListener(topics = ["\${app.topic.name}"], groupId = "\${app.groupid}")
    fun processMessage(message: String) {
        logger.info("got message: {}", message)
    }
}
