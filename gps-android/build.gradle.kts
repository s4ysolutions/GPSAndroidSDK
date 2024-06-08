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
        minSdk = 24

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

dependencies {
    api(project(":gps"))
    implementation(libs.androidx.appcompat)
    implementation(libs.play.services.location)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.lifecycle.service)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlin.test.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

tasks.register<Jar>("javaSourcesJar") {
    archiveClassifier.set("sources")
    from(android.sourceSets["main"].java.srcDirs)
}


publishing {
    publications {
        create<MavenPublication>("Release") {
            groupId = "solutions.s4y.gps"
            artifactId = "gps-sdk-android"
            version = "1.0.0-dev.2"

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
                artifact(tasks["bundleReleaseAar"])
                artifact(project(":gps").tasks["jar"])
                artifact(tasks["javaSourcesJar"])
            }
        }
    }
}