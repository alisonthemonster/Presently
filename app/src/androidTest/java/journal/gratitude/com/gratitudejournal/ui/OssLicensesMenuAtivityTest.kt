package journal.gratitude.com.gratitudejournal.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import journal.gratitude.com.gratitudejournal.ui.licenses.OssLicensesActivity
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class OssLicensesMenuAtivityTest {
    @Test
    fun should_load_data() {
        val scenario =
                ActivityScenario.launch(OssLicensesActivity::class.java)
//        scenario.onActivity { activity ->
        }
}