package de.pkutschera.spring.kafka.spring.data

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Serializer

class PlayerSerializer : Serializer<Player> {
    private val objectMapper = ObjectMapper()
    override fun configure(configs: Map<String, *>, isKey: Boolean) {}
    override fun serialize(topic: String, data: Player): ByteArray {
        return try {
            println("Serializing...")
            objectMapper.writeValueAsBytes(data)
        } catch (e: Exception) {
            throw SerializationException("Error when serializing Player to byte[]")
        }
    }

    override fun close() {}
}

