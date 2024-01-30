plugins {
    id("java")
}

group = "jarekr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("info.picocli:picocli:4.7.5")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.register<Copy>("install") {
    from(configurations.runtimeClasspath) {
        into(layout.buildDirectory.dir("runtime"))
    }
}

tasks.named("install) {
}


tasks.test {
    useJUnitPlatform()
}
