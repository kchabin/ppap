import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask


plugins {
	kotlin("jvm") version "1.9.24"
	kotlin("plugin.spring") version "1.9.24"
	id("org.springframework.boot") version "3.3.2"
	id("io.spring.dependency-management") version "1.1.6"
	kotlin("plugin.jpa") version "1.9.24"

	//	openapi gen을 위한 플러그인 설정
	id("org.openapi.generator") version "6.2.0"
}
allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.Embeddable")
	annotation("jakarta.persistence.MappedSuperclass")
}
noArg {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
}
group = "swu.pbl"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()

}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation("mysql:mysql-connector-java:8.0.33")
	implementation("org.openapitools:jackson-databind-nullable:0.2.6")
	implementation("javax.validation:validation-api:2.0.1.Final")
	implementation("javax.annotation:javax.annotation-api:1.3.2")
	implementation("javax.servlet:javax.servlet-api:4.0.1")
	implementation("org.apache.httpcomponents.client5:httpclient5:5.2.1")

	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

	implementation("org.springframework.boot:spring-boot-starter-validation")
	runtimeOnly ("com.h2database:h2")

	//spring security
	implementation("org.springframework.boot:spring-boot-starter-security")

	//JWT
	implementation("io.jsonwebtoken:jjwt-api:0.12.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")

	//Redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis")


	// Map Struct
	implementation ("org.mapstruct:mapstruct:1.5.3.Final")
	annotationProcessor ("org.mapstruct:mapstruct-processor:1.5.3.Final")



}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
// openapi 문서를 Generate하기 위한 설정
task<GenerateTask>("generateApiDoc") {
	generatorName.set("html2")
	inputSpec.set("$projectDir/src/main/resources/openapi/test-api.yaml")

	outputDir.set("$buildDir/openapi/doc/")
}

task<GenerateTask>("generateApiServer") {
	generatorName.set("kotlin-spring")
	inputSpec.set("$projectDir/src/main/resources/openapi/test-api.yaml")

	outputDir.set("$buildDir/openapi/server-code/")
	apiPackage.set("swu.pbl.ppap.openapi.generated.controller")
	modelPackage.set("swu.pbl.ppap.openapi.generated.model")
	configOptions.set(
		mapOf(
			"interfaceOnly" to "true",
		)
	)

	additionalProperties.set(
		mapOf(
			"useTags" to "true",
			"jakarta" to "true",
			"enumPropertyNaming" to "UPPERCASE"

		)
	)

	typeMappings.set(mapOf("DateTime" to "LocalDateTime"))
	importMappings.set(mapOf("LocalDateTime" to "java.time.LocalDateTime"))
}

tasks.compileKotlin {
	dependsOn("generateApiServer")
}

kotlin.sourceSets.main {
	kotlin.srcDirs("src/main/kotlin", "$buildDir/openapi/server-code/src/main")
}