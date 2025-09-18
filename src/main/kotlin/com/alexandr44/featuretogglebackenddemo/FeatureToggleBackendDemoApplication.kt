package com.alexandr44.featuretogglebackenddemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@SpringBootApplication
class FeatureToggleBackendDemoApplication

fun main(args: Array<String>) {
    runApplication<FeatureToggleBackendDemoApplication>(*args)
}
