package journal.gratitude.com.gratitudejournal.testUtils

import dagger.hilt.android.testing.CustomTestApplication
import journal.gratitude.com.gratitudejournal.BaseGratitudeApplication

/**
 * https://developer.android.com/training/dependency-injection/hilt-testing#custom-application
 */
@CustomTestApplication(BaseGratitudeApplication::class)
interface HiltTestApplication
