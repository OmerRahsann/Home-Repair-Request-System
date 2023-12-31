plugins {
	java
	id("org.springframework.boot") version "3.1.4"
	id("io.spring.dependency-management") version "1.1.3"
	id("io.freefair.lombok") version "8.4"
}

group = "homerep"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("jakarta.mail:jakarta.mail-api:2.1.2") // For Email API
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-aop")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.security:spring-security-crypto")
	implementation("org.springframework.session:spring-session-jdbc")
	implementation("org.bouncycastle:bcpkix-jdk15on:1.70") // For Argon2

	implementation("org.im4java:im4java:1.4.0") // For interfacing with ImageMagick
	implementation("com.bucket4j:bucket4j-core:8.5.0") // For rate limiting
	implementation("com.github.ben-manes.caffeine:caffeine") // For cache data structure
	implementation("com.google.maps:google-maps-services:2.2.0") // For geocoding addresses with the Google Maps API
	implementation("org.jsoup:jsoup:1.17.1") // For sanitizing HTML

	implementation("io.micrometer:micrometer-registry-influx")

	runtimeOnly("com.mysql:mysql-connector-j")

	developmentOnly("org.springframework.boot:spring-boot-devtools")
	developmentOnly("com.h2database:h2")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("com.icegreen:greenmail-spring:2.1.0-alpha-2")
	testImplementation("org.eclipse.angus:angus-mail:2.0.2") // For GreenMail to properly start
	testRuntimeOnly("com.h2database:h2")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
