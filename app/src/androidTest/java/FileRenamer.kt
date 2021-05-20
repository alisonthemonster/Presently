import android.app.Instrumentation
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log.d
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.IllegalStateException
import java.util.*

object FileRenamer {

    fun write(instrumentation: Instrumentation) {
        val path = getProjectPath(instrumentation.context)
        write(path, instrumentation)
    }

    private fun write(path: String?, instrumentation: Instrumentation) {
        check(!(path == null || path.isEmpty())) { "No path found!" }
        grantPermission(instrumentation, "android.permission.WRITE_EXTERNAL_STORAGE")
        val sdcard = Environment.getExternalStorageDirectory()
        check(sdcard.exists()) { "No sd card found!" }

        try {
            val coverageFile = File(sdcard, "coverage.ec")
            val renamedCoverageFile = File(sdcard, "$path-coverage.ec")
            val renamed = coverageFile.renameTo(renamedCoverageFile)
            if (renamed) {
                d("file-renamer","File renamed: $path-coverage.ec")
            }else {
                d("file-renamer","File NOT renamed: $path-coverage.ec");
            }
        } catch (exception: IOException) {
            throw IllegalStateException(exception)
        }
    }

    private fun grantPermission(instrumentation: Instrumentation, permission: String) {
        val automation = instrumentation.uiAutomation
        val command = String.format(
            Locale.ENGLISH,
            "pm grant %s %s",
            instrumentation.context.packageName,
            permission
        )
        val parcelFileDescriptor = automation.executeShellCommand(command)
        val stream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        try {
            val buffer = ByteArray(1024)
            while (stream.read(buffer) != -1) {
                //consume until stream is complete
            }
        } catch (exception: IOException) {}
        finally {
            try { stream.close() } catch (exception: IOException) {}
            try { parcelFileDescriptor.close() } catch (exception: IOException) {}
        }
    }

    //uses reflection to gt the PROJECT_PATH value from the BuildConfig.java
    private fun getProjectPath(context: Context): String? {
        try {
            val info = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            val packageName = info.packageName
            val buildConfigClassName = "$packageName.BuildConfig"
            val clazz = Class.forName(buildConfigClassName)
            val field = clazz.getField("PROJECT_PATH")
            return field[""] as String
        } catch (exception: Exception) {
            d("file-renamer", "Unable to get project path: ${exception}")
        }
        return null
    }
}