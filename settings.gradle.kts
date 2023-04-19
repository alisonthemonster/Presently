include(":ui")
include(":app")
include(":strings")
include(":settings")
include(":testing")
include(":logging")
include(":coroutine_utils")

plugins {
    id("org.gradle.toolchains.foojay-resolver") version "0.4.0"
}

toolchainManagement {
    jvm {
        javaRepositories {
            repository("foojay") {
                resolverClass.set(org.gradle.toolchains.foojay.FoojayToolchainResolver::class.java)
            }
        }
    }
}