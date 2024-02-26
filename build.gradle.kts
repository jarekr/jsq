plugins {
    id("java")
    id("jacoco")
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


tasks.register<Copy>("installRuntimeCp") {
    from(configurations.runtimeClasspath)
    from(layout.buildDirectory.dir("libs"))
    into(layout.buildDirectory.dir("install/lib"))
    dependsOn(tasks.named("build"))
}

tasks.register<Copy>("installResources") {
  from (files(layout.projectDirectory.dir("src/main/resources/bin")))
  into(layout.buildDirectory.dir("install/bin"))
}

tasks.register<Task>("install") {
	dependsOn(tasks.named("installResources"))
	dependsOn(tasks.named("installRuntimeCp"))

  doLast() {
    println("jsq installed into " + layout.buildDirectory.file("install").get());
    println("to execute:");
    println("  JSQLIBPATH=build/install/lib/jsq-1.0-SNAPSHOT.jar:build/install/lib/picocli-4.7.5.jar ./build/install/bin/jsq --help");
  }
}

tasks.test {
    useJUnitPlatform()
}
