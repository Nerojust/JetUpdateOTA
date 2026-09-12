import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlin.jvm)
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
}

mavenPublishing {
    coordinates("io.github.nerojust", "ota-testing", "0.1.0")

    pom {
        name.set("JetUpdateOTA Testing")
        description.set("In-memory fakes of JetUpdateOTA's collaborator interfaces, for consumers' own tests.")
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
