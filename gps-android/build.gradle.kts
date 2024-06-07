plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "s4y.demo.mapsdksdemo.gps.android"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":gps"))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("androidx.lifecycle:lifecycle-service:2.8.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.truth:truth:1.1.5")
    testImplementation(kotlin("test-junit"))
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

publishing {
    publications {
        create<MavenPublication>("Release") {
            groupId = "solutions.s4y.gps"
            artifactId = "gps-android"
            version = "1.0.0" // replace with your actual version

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
        }
    }
}