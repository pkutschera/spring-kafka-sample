package de.pkutschera.spring.kafka.spring.data

import com.fasterxml.jackson.annotation.JsonProperty

enum class Position {
    Goalkeeper,
    Defender,
    Midfield,
    Forward
}

data class Player(
    @JsonProperty("firstname") val firstname: String,
    @JsonProperty("lastname") val lastname: String,
    @JsonProperty("club") val club: String,
    @JsonProperty("position") val position: Position
)
