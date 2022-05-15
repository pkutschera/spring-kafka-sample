package de.pkutschera.spring.kafka.spring.consumer

import mu.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.support.TopicPartitionOffset
import org.springframework.stereotype.Component

val logger = KotlinLogging.logger {}

@EnableKafka
@Configuration
class KafkaConsumerConfig(
    @Value("\${kafka.bootstrapAddress.url}")
    private val url: String,

    @Value("\${app.topic.name}")
    private val topic: String,

    @Value("\${app.groupid}")
    private val groupId: String,

    @Value("\${app.partition}")
    private val partitionId: Int,

    @Value("\${app.offset}")
    private val partitionOffset: Long,
) {
    @Bean
    fun consumerFactory(): ConsumerFactory<String, Any> {
        val props = HashMap<String, Any>()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = url
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        return DefaultKafkaConsumerFactory(props);
    }

    @Bean
    fun consumerProperties(): ContainerProperties {
        val properties = ContainerProperties(TopicPartitionOffset(topic, partitionId, partitionOffset))
        properties.setGroupId(groupId)
        properties.setMessageListener(MessageConsumer())
        return properties;
    }

    @Bean
    fun kafkaMessageListenerContainer() = ConcurrentMessageListenerContainer(consumerFactory(), consumerProperties())
}

@Component
class MessageConsumer: MessageListener<String, String> {

    override fun onMessage(data: ConsumerRecord<String, String>) {
        logger.info("got record with value: {}", data.value())
    }
}
