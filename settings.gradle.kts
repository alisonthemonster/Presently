include(":ui")
include(":app")
include(":strings")
include(":settings")
include(":testing")
include(":logging")
include(":coroutineutils")

// needed for running jvmToolchain on CircleCi
plugins {
    id("org.gradle.toolchains.foojay-resolver") version "0.4.0"
}

// needed for running jvmToolchain on CircleCi
toolchainManagement {
    jvm {
        javaRepositories {
            repository("foojay") {
                resolverClass.set(org.gradle.toolchains.foojay.FoojayToolchainResolver::class.java)
            }
        }
    }
}
