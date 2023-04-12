plugins {
    id("java-library")
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
