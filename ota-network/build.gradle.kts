import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.maven.publish)
}

kotlin {
    jvmToolchain(11)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    api(project(":ota-core"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    api(libs.okhttp)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
}

tasks.test {
    useJUnitPlatform()
}

mavenPublishing {
    coordinates("io.github.nerojust", "ota-network", "0.1.0")

    pom {
        name.set("JetUpdateOTA Network")
        description.set("HTTP manifest checking for the JetUpdateOTA update library, built on OkHttp.")
        inceptionYear.set("2026")
        url.set("https://github.com/nerojust/JetUpdateOTA")
        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("nerojust")
                name.set("nerojust")
                url.set("https://github.com/nerojust")
            }
        }
        scm {
            url.set("https://github.com/nerojust/JetUpdateOTA")
            connection.set("scm:git:git://github.com/nerojust/JetUpdateOTA.git")
            developerConnection.set("scm:git:ssh://git@github.com/nerojust/JetUpdateOTA.git")
        }
    }

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
}
