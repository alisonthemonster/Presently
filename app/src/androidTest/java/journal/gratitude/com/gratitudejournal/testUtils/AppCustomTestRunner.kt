package journal.gratitude.com.gratitudejournal.testUtils

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import com.presently.testing.FileRenamer

/**
 * A custom [AndroidJUnitRunner] used to update coverage filenames after tests complete.
 */
class AppCustomTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication_Application::class.java.name, context)
    }

    override fun finish(resultCode: Int, results: Bundle?) {
        FileRenamer.write(this)
        super.finish(resultCode, results)
    }
}
