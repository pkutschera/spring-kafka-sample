package de.pkutschera.spring.kafka.consumer

import de.pkutschera.spring.kafka.avro.Player
import de.pkutschera.spring.kafka.avro.Position
import io.confluent.kafka.schemaregistry.avro.AvroSchema
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.apache.avro.Schema
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode.ALL
import java.util.UUID

@ActiveProfiles("Tests")
@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestConstructor(autowireMode = ALL)
class ConsumerTests(
    val embeddedKafkaBroker: EmbeddedKafkaBroker
) {
    @MockK
    private val consumer = mockk<MessageConsumer>(relaxed = true)

    @Value("\${app.topic.name}")
    private lateinit var topicName: String

    private lateinit var kafkaTemplate: KafkaTemplate<String, Any>

    private lateinit var listenerContainer: KafkaMessageListenerContainer<String, Player>

    @BeforeAll
    fun setup() {
        val schemaRegistryClient = MockSchemaRegistryClient()
        val kafkaAvroSerializer = KafkaAvroSerializer(schemaRegistryClient)
        val kafkaAvroDeserializer = KafkaAvroDeserializer(schemaRegistryClient)

        val producerProperties = KafkaTestUtils.producerProps(embeddedKafkaBroker.getBrokersAsString())
        producerProperties[KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS] = true
        producerProperties[KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = "mock://localhost"
        val producerFactory = DefaultKafkaProducerFactory(producerProperties, StringSerializer(), kafkaAvroSerializer)

        kafkaTemplate = KafkaTemplate(producerFactory)

        val consumerProps: MutableMap<String, Any> =
            KafkaTestUtils.consumerProps("consumer", "true", embeddedKafkaBroker)
        consumerProps[KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = "mock://localhost"
        consumerProps["auto.offset.reset"] = "earliest"

        listenerContainer = KafkaMessageListenerContainer<String, Player>(
            DefaultKafkaConsumerFactory(consumerProps, StringDeserializer(), kafkaAvroDeserializer),
            ContainerProperties(topicName)
        )
        listenerContainer.setupMessageListener(consumer)
        listenerContainer.start()
    }

    @Test
    fun `retrieve messages from topic`() {
        val messageValue = Player("Erling", "Haaland", "Manchester City", Position.Forward)
        kafkaTemplate.send(topicName, messageValue)

        verify(exactly = 1, timeout = 5000L) {
            consumer.onMessage(any())
        }
    }

    @AfterAll
    fun tearDown() {
        listenerContainer.stop()
    }
}
