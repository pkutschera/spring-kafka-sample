package de.pkutschera.spring.kafka.spring.consumer

import de.pkutschera.spring.kafka.spring.data.Player
import de.pkutschera.spring.kafka.spring.data.PlayerDeserializer
import de.pkutschera.spring.kafka.spring.data.PlayerSerializer
import de.pkutschera.spring.kafka.spring.data.Position
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.annotation.DirtiesContext
import java.util.UUID

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConsumerTests {
    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    @MockK
    private val consumer = mockk<MessageConsumer>(relaxed = true)

    @Value("\${app.topic.name}")
    private lateinit var topic: String

    private lateinit var producer: Producer<String, Player>

    private lateinit var listenerContainer: KafkaMessageListenerContainer<String, Player>

    @BeforeAll
    fun setup() {
        producer = KafkaProducer(
            KafkaTestUtils.producerProps(embeddedKafkaBroker.getBrokersAsString()),
            StringSerializer(),
            PlayerSerializer()
        )

        val consumerProps: MutableMap<String, Any> =
            KafkaTestUtils.consumerProps("consumer", "true", embeddedKafkaBroker)
        consumerProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        consumerProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = PlayerDeserializer::class.java
        consumerProps["auto.offset.reset"] = "earliest"

        listenerContainer = KafkaMessageListenerContainer<String, Player>(
            DefaultKafkaConsumerFactory(consumerProps),
            ContainerProperties(topic)
        )
        listenerContainer.setupMessageListener(consumer)
        listenerContainer.start()
    }

    @Test
    fun `retrieve messages from topic`() {
        val messageKey = UUID.randomUUID().toString()
        val messageValue = Player("Erling", "Haaland", "Manchester City", Position.Forward)
        producer.send(ProducerRecord<String, Player>(topic, messageKey, messageValue))
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
