package journal.gratitude.com.gratitudejournal.util

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * ThreeTenABP bundles its own time-zone database, which goes stale when a region changes its
 * rules and made `LocalTime.now()` disagree with the device clock. Use the platform `java.time`.
 */
class NoThreeTenDependencyTest {

    private val appDir = File(System.getProperty("user.dir"))

    @Test
    fun sourcesDoNotImportThreeTen() {
        val offenders = File(appDir, "src").walkTopDown()
            .filter { it.isFile && it.extension in setOf("kt", "java") }
            .filter { it.name != "NoThreeTenDependencyTest.kt" }
            .filter { file -> file.readLines().any { it.startsWith("import org.threeten") } }
            .map { it.relativeTo(appDir).path }
            .toList()

        assertTrue("Use java.time instead of org.threeten in: $offenders", offenders.isEmpty())
    }

    @Test
    fun buildFilesDoNotDeclareThreeTen() {
        val buildFiles = listOf(
            File(appDir, "build.gradle.kts"),
            File(appDir.parentFile, "gradle/libs.versions.toml"),
        ).filter { it.isFile }

        assertTrue("Expected to find build files from $appDir", buildFiles.isNotEmpty())
        val offenders = buildFiles.filter { f ->
            f.readText().contains(Regex("three-?ten", RegexOption.IGNORE_CASE))
        }.map { it.name }

        assertTrue("ThreeTen dependency declared in: $offenders", offenders.isEmpty())
    }
}
