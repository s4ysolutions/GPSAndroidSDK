plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "s4y.gps.sdk.android"
    buildFeatures {
        buildConfig = true
    }

    compileSdk = 34

    defaultConfig {
        minSdk = 18

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin {
    jvmToolchain{
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

dependencies {
    api(libs.androidx.appcompat)
    api(libs.commons.math3)
    api(libs.play.services.location)
    api(libs.kotlinx.coroutines.core)
    api(libs.androidx.lifecycle.service)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.kotlin.test.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

publishing {
    publications {
        create<MavenPublication>("Release") {
            groupId = "solutions.s4y.gps"
            artifactId = "gps-sdk-android"
            version = "3.0.0-alpha1"

            pom {
                packaging = "aar"
                name.set("S4Y SDK for Android")
                description.set("Android GPS current location and updates providers")
                url.set("https://github.com/s4ysolutions/GPSAndroidSDK")

                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://github.com/s4ysolutions/GPSAndroidSDK/blob/main/LICENSE")
                    }
                }

                developers {
                    developer {
                        id.set("dsame")
                        name.set("Sergey Dolin")
                        email.set("sergey@s4y.solutions")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/s4ysolutions/GPSAndroidSDK.git")
                    developerConnection.set("scm:git:ssh://github.com/s4ysolutions/GPSAndroidSDK.git")
                    url.set("https://github.com/s4ysolutions/GPSAndroidSDK")
                }
            }

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}