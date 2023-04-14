plugins {
    id("java-library")
    id("maven-publish")
}

apply {
    version = "1.0.0"
    group = "org.teacon"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(11));
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
}

testing {
    suites.getByName("test", JvmTestSuite::class) { useJUnitJupiter("5.7.2") }
}

tasks {
    processResources.get().apply {
        from(file("LICENSE")) { into("META-INF/") }
    }
    jar.get().manifest.attributes(mapOf(
        "Specification-Title" to "urlpattern",
        "Specification-Version" to "${project.version}",
        "Specification-Vendor" to "teacon.org",
        "Implementation-Title" to "urlpattern",
        "Implementation-Version" to "${project.version}",
        "Implementation-Vendor" to "teacon.org",
    ))
}

publishing {
    publications.create<MavenPublication>("mavenJava") {
        groupId = "org.teacon"
        artifactId = "urlpattern"
        version = "${project.version}"
        pom {
            name.set("URLPattern")
            url.set("https://github.com/teaconmc/urlpattern")
            description.set("Java implementation of URLPattern API standard (https://wicg.github.io/urlpattern)")
            licenses {
                license {
                    name.set("GNU Lesser General Public License, Version 3.0")
                    url.set("https://www.gnu.org/licenses/lgpl-3.0.txt")
                }
            }
            developers {
                developer {
                    id.set("teaconmc")
                    name.set("TeaConMC")
                    email.set("contact@teacon.org")
                }
                developer {
                    id.set("ustc-zzzz")
                    name.set("Yanbing Zhao")
                    email.set("zzzz.mail.ustc@gmail.com")
                }
            }
            issueManagement {
                system.set("GitHub Issues")
                url.set("https://github.com/teaconmc/urlpattern/issues")
            }
            scm {
                url.set("https://github.com/teaconmc/urlpattern")
                connection.set("scm:git:git://github.com/teaconmc/urlpattern.git")
                developerConnection.set("scm:git:ssh://github.com/teaconmc/urlpattern.git")
            }
        }
        artifact(tasks.jar)
    }
}
