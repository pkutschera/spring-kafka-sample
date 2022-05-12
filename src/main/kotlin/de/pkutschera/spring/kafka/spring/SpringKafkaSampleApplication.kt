package de.pkutschera.spring.kafka.spring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringKafkaSampleApplication

fun main(args: Array<String>) {
    runApplication<SpringKafkaSampleApplication>(*args)
}
