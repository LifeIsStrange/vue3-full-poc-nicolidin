import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.4.4"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"

	kotlin("jvm") version "1.4.32"
	kotlin("plugin.spring") version "1.4.32"
	kotlin("plugin.allopen") version "1.4.32"
	kotlin("plugin.jpa") version "1.4.32"
	kotlin("kapt") version "1.4.32"

	id("org.flywaydb.flyway") version "6.5.7"
}

flyway {
	url = System.getenv("POSTGRES_URI") ?: "jdbc:postgresql://localhost:5432/postgres?stringtype=unspecified"
	user = "postgres"
	password = "rebirth"
}

group = "com.nicoSteph"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

// this might no longer be needed
configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
	jcenter()
}

dependencies {

	implementation("org.springframework:spring-context-support:5.3.3") // Used for javamail API
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-hateoas")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.data:spring-data-elasticsearch")

	implementation("org.springframework.security:spring-security-core:latest.release")
	testImplementation("org.springframework.security:spring-security-test:latest.release")
	implementation("org.springframework.security:spring-security-jwt:latest.release")
	implementation("org.bouncycastle:bcprov-jdk15on:latest.release")
	implementation("com.auth0:java-jwt:latest.release")

	developmentOnly("org.springframework.boot:spring-boot-devtools")
	// https://docs.spring.io/spring-boot/docs/1.5.16.RELEASE/reference/html/using-boot-devtools.html#using-boot-devtools
	// to investigate https://dev.to/suin/spring-boot-developer-tools-how-to-enable-automatic-restart-in-intellij-idea-1c6i
	// runtimeOnly("org.springframework.boot:spring-boot-devtools")
	// shouldnt exist ?

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	//implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8") no longer needed since kotlin 1.4

	implementation("org.springframework.boot:spring-boot-starter-integration")
	//testImplementation("org.springframework.integration:spring-integration-test")
	implementation("org.springframework.integration:spring-integration-test")

	// twice, the plugin is for the gradle task, this is for the programmatic api
	implementation("org.flywaydb:flyway-core:6.5.7") // fixme upgrade to 7.x breaking change

	//implementation("org.postgresql:postgresql:latest.release")
	runtimeOnly("org.postgresql:postgresql:latest.release")
	//annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	kapt("org.springframework.boot:spring-boot-configuration-processor")
	testImplementation("org.apache.httpcomponents:httpclient:latest.release")
	testImplementation("org.mockito:mockito-core:latest.release")
	testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:latest.release")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
		exclude(module = "mockito-core")
	}

	testImplementation("org.junit.jupiter:junit-jupiter-api")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
	testImplementation("com.ninja-squad:springmockk:latest.release")
	testImplementation("io.kotlintest:kotlintest-runner-junit5:latest.release")
	testImplementation("io.kotlintest:kotlintest-extensions-spring:latest.release")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}

allOpen {
	annotation("javax.persistence.Entity")
	annotation("javax.persistence.Embeddable")
	annotation("javax.persistence.MappedSuperclass")
}
