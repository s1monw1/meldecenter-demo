package de.swirtz.lwdemo

import de.swirtz.lwdemo.service.dispatching.DispatcherProperties
import de.swirtz.lwdemo.service.reporting.KVReportServiceConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(KVReportServiceConfiguration::class, DispatcherProperties::class)
class LexwareDemoMeldecenterApplication

fun main(args: Array<String>) {
    runApplication<LexwareDemoMeldecenterApplication>(*args)
}
