package com.rebirthCorp.rebirth

import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.repository.config.BootstrapMode
import org.springframework.integration.config.EnableIntegration

@SpringBootApplication
@EnableIntegration
@EnableJpaRepositories("com.rebirthCorp.rebirth.repositories", bootstrapMode = BootstrapMode.DEFERRED)
class RebirthApplication

fun main(args: Array<String>) {
	if (System.getenv("MIGRATE_ON_STARTUP") == "true") {
		val flyway = Flyway.configure().dataSource(
				System.getenv("POSTGRES_URI")!!,
				System.getenv("POSTGRES_USER")!!,
				System.getenv("POSTGRES_PASSWORD")!!).load()
		flyway.migrate()
	}
	runApplication<RebirthApplication>(*args)
}
