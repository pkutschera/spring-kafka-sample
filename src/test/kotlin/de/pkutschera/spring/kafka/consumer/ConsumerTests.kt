package de.pkutschera.spring.kafka.consumer

import de.pkutschera.spring.kafka.avro.Player
import de.pkutschera.spring.kafka.avro.Position
import io.confluent.kafka.schemaregistry.ParsedSchema
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
    private lateinit var topic: String

    private lateinit var producer: Producer<String, Any>

    private lateinit var listenerContainer: KafkaMessageListenerContainer<String, Player>

    @BeforeAll
    fun setup() {
        val schemaRegistryClient = MockSchemaRegistryClient()
        //
        // val parser = Schema.Parser()
        //
        // schemaRegistryClient.register(
        //     "de.pkutschera.spring.kafka.avro.Position", AvroSchema(
        //         parser.parse(
        //             ClassPathResource("de/pkutschera/spring/kafka/avro/position.avsc").file
        //         )
        //     )
        // )
        // schemaRegistryClient.register(
        //     "de.pkutschera.spring.kafka.avro.Player", AvroSchema(
        //         parser.parse(
        //             ClassPathResource("/de/pkutschera/spring/kafka/avro/player.avsc").file
        //         )
        //     )
        // )
        val kafkaAvroSerializer = KafkaAvroSerializer(schemaRegistryClient)
        val kafkaAvroDeserializer = KafkaAvroDeserializer(schemaRegistryClient)

        val producerProperties = KafkaTestUtils.producerProps(embeddedKafkaBroker.getBrokersAsString())
        producerProperties[KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS] = true
        producerProperties[KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = "mock://localhost"
        val producerFactory = DefaultKafkaProducerFactory(producerProperties, StringSerializer(), kafkaAvroSerializer)
        producer = producerFactory.createProducer()

        val consumerProps: MutableMap<String, Any> =
            KafkaTestUtils.consumerProps("consumer", "true", embeddedKafkaBroker)
        consumerProps[KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = "mock://localhost"
        consumerProps["auto.offset.reset"] = "earliest"

        listenerContainer = KafkaMessageListenerContainer<String, Player>(
            DefaultKafkaConsumerFactory(consumerProps, StringDeserializer(), kafkaAvroDeserializer),
            ContainerProperties(topic)
        )
        listenerContainer.setupMessageListener(consumer)
        listenerContainer.start()
    }

    @Test
    fun `retrieve messages from topic`() {
        val messageKey = UUID.randomUUID().toString()
        val messageValue = Player("Erling", "Haaland", "Manchester City", Position.Forward)
        producer.send(ProducerRecord<String, Any>(topic, messageKey, messageValue))
        producer.flush()

        verify(exactly = 1, timeout = 5000L) {
            consumer.onMessage(any())
        }
    }

    @AfterAll
    fun tearDown() {
        listenerContainer.stop()
    }
}
