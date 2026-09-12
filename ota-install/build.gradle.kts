import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
}

android {
    namespace = "com.nerojust.ota.install"
    compileSdk {
        version =
            release(37) {
                minorApiLevel = 1
            }
    }

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

dependencies {
    api(project(":ota-core"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit.jupiter)
    testImplementation("org.junit.vintage:junit-vintage-engine:5.11.0")
    testRuntimeOnly(libs.junit.platform.launcher)
}

mavenPublishing {
    configure(AndroidSingleVariantLibrary(variant = "release", sourcesJar = true, publishJavadocJar = true))

    coordinates("io.github.nerojust", "ota-install", "0.1.0")

    pom {
        name.set("JetUpdateOTA Install")
        description.set("Signature verification, state persistence, and PackageInstaller integration.")
        inceptionYear.set("2026")
        url.set("https://github.com/nerojust/JetUpdateOTA")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
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
