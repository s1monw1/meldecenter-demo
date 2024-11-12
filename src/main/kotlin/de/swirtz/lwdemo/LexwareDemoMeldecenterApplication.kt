package de.swirtz.lwdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class LexwareDemoMeldecenterApplication

fun main(args: Array<String>) {
    runApplication<LexwareDemoMeldecenterApplication>(*args)
}
