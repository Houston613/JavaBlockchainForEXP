plugins {
    id("java")
    id("application")
}


group = "org.icst"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}


application {
    mainClass.set("org.icst.Main")
}

tasks.create("MyFatJar", Jar::class) {
    group = "build"
    description = "Creates a self-contained fat JAR of the application that can be run."
    manifest.attributes["Main-Class"] = "org.icst.Main"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    val dependencies = configurations
        .runtimeClasspath
        .get()
        .map(::zipTree)
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/INDEX.LIST")
    from(dependencies)
    with(tasks.jar.get())
}

tasks {
    // Use the native JUnit support of Gradle.
    "test"(Test::class) {
        useJUnitPlatform()
    }
}