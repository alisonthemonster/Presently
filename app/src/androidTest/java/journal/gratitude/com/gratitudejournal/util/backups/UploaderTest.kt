package journal.gratitude.com.gratitudejournal.util.backups

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import com.dropbox.core.InvalidAccessTokenException
import com.dropbox.core.LocalizedText
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.auth.AuthError.INVALID_ACCESS_TOKEN
import com.dropbox.core.v2.files.UploadError.OTHER
import com.dropbox.core.v2.files.UploadErrorException
import com.google.common.truth.Truth.assertThat
import com.presently.coroutine_utils.AppCoroutineDispatchers
import com.presently.logging.AnalyticsLogger
import com.presently.logging.CrashReporter
import com.presently.settings.BackupCadence
import com.presently.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.model.CloudUploadResult
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.UploadError
import journal.gratitude.com.gratitudejournal.model.UploadSuccess
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.CloudProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import java.io.File
import java.lang.Exception
import kotlin.test.fail

class UploaderTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private val dispatchers = AppCoroutineDispatchers(
        io = TestCoroutineDispatcher(),
        computation = TestCoroutineDispatcher(),
        main = TestCoroutineDispatcher()
    )

    private val repo = object : EntryRepository {
        override suspend fun getEntries(): List<Entry> {
            return listOf(Entry(LocalDate.of(2021, 12, 25), "Merry Christmas!"))
        }

        override suspend fun getEntry(date: LocalDate): Entry = fail("Not needed in this test")
        override suspend fun getEntriesFlow(): Flow<List<Entry>> = fail("Not needed in this test")
        override fun getWrittenDates(): LiveData<List<LocalDate>> = fail("Not needed in this test")
        override suspend fun addEntry(entry: Entry) = fail("Not needed in this test")
        override suspend fun addEntries(entries: List<Entry>) = fail("Not needed in this test")
        override fun searchEntries(query: String): Flow<PagingData<Entry>> =
            fail("Not needed in this test")
    }

    private var wasCloudProviderCalled = false

    private val cloudProvider = object : CloudProvider {
        override suspend fun uploadToCloud(file: File): CloudUploadResult {
            wasCloudProviderCalled = true
            return UploadSuccess
        }
    }

    private val crashReporter = object : CrashReporter {
        var loggedException: Exception? = null
        override fun logHandledException(exception: Exception) {
            loggedException = exception
        }

        override fun optOutOfCrashReporting() = fail("Not needed in this test")
        override fun optIntoCrashReporting() = fail("Not needed in this test")
    }

    private var wasAccessTokenCleared = false

    private val settings = object : PresentlySettings {
        override fun getAccessToken(): DbxCredential? = null
        override fun clearAccessToken() {
            wasAccessTokenCleared = true
        }
        override fun setAccessToken(newToken: DbxCredential) = fail("Not needed in this test")
        override fun getCurrentTheme(): String = fail("Not needed in this test")
        override fun setTheme(themeName: String) = fail("Not needed in this test")
        override fun isBiometricsEnabled(): Boolean = fail("Not needed in this test")
        override fun shouldLockApp(): Boolean = fail("Not needed in this test")
        override fun setOnPauseTime() = fail("Not needed in this test")
        override fun getFirstDayOfWeek(): Int = fail("Not needed in this test")
        override fun shouldShowQuote(): Boolean = fail("Not needed in this test")
        override fun getAutomaticBackupCadence(): BackupCadence = fail("Not needed in this test")
        override fun getLocale(): String = fail("Not needed in this test")
        override fun hasEnabledNotifications(): Boolean = fail("Not needed in this test")
        override fun getNotificationTime(): LocalTime = fail("Not needed in this test")
        override fun hasUserDisabledAlarmReminders(context: Context): Boolean = fail("Not needed in this test")
        override fun getLinesPerEntryInTimeline(): Int = fail("Not needed in this test")
        override fun shouldShowDayOfWeekInTimeline(): Boolean = fail("Not needed in this test")
        override fun wasDropboxAuthInitiated(): Boolean = fail("Not needed in this test")
        override fun markDropboxAuthAsCancelled() = fail("Not needed in this test")
        override fun markDropboxAuthInitiated() = fail("Not needed in this test")
        override fun isOptedIntoAnalytics(): Boolean = fail("Not needed in this test")
    }

    private val analytics = object : AnalyticsLogger {
        override fun recordEvent(event: String) = fail("Not needed in this test")
        override fun recordEvent(event: String, details: Map<String, Any>) =
            fail("Not needed in this test")
        override fun recordSelectEvent(selectedContent: String, selectedContentType: String) =
            fail("Not needed in this test")
        override fun recordEntryAdded(numEntries: Int) = fail("Not needed in this test")
        override fun recordView(viewName: String) = fail("Not needed in this test")
        override fun optOutOfAnalytics() = fail("Not needed in this test")
        override fun optIntoAnalytics() = fail("Not needed in this test")
    }

    @Test
    fun emptyRepositoryDoesNothing() = runBlockingTest {
        wasCloudProviderCalled = false
        val repo = object : EntryRepository {
            override suspend fun getEntries(): List<Entry> {
                return emptyList()
            }
            override suspend fun getEntry(date: LocalDate): Entry = fail("Not needed in this test")
            override suspend fun getEntriesFlow(): Flow<List<Entry>> = fail("Not needed in this test")
            override fun getWrittenDates(): LiveData<List<LocalDate>> = fail("Not needed in this test")
            override suspend fun addEntry(entry: Entry) = fail("Not needed in this test")
            override suspend fun addEntries(entries: List<Entry>) = fail("Not needed in this test")
            override fun searchEntries(query: String): Flow<PagingData<Entry>> =
                fail("Not needed in this test")
        }

        val uploader = RealUploader(dispatchers, repo, cloudProvider, crashReporter, settings)

        val actual = uploader.uploadEntries(context)

        assertThat(actual).isEqualTo(ListenableWorker.Result.success())
        assertThat(wasCloudProviderCalled).isFalse() //don't use the cloud provider here since there is no data to upload
    }

    @Test
    fun successfulUpload() = runBlockingTest {
        wasCloudProviderCalled = false
        val uploader = RealUploader(dispatchers, repo, cloudProvider, crashReporter, settings)
        val actual = uploader.uploadEntries(context)

        assertThat(actual).isEqualTo(ListenableWorker.Result.success())
        assertThat(wasCloudProviderCalled).isTrue()
    }

    @Test
    fun invalidAccessTokenUpload() = runBlockingTest {
        wasAccessTokenCleared = false
        crashReporter.loggedException = null
        val exception = InvalidAccessTokenException("requestId", "message", INVALID_ACCESS_TOKEN)
        val cloudProvider = object : CloudProvider {
            override suspend fun uploadToCloud(file: File): CloudUploadResult {
                return UploadError(exception)
            }
        }

        val uploader = RealUploader(dispatchers, repo, cloudProvider, crashReporter, settings)
        val actual = uploader.uploadEntries(context)

        assertThat(actual).isEqualTo(ListenableWorker.Result.failure())
        assertThat(wasAccessTokenCleared).isTrue() //clear access tokens
        assertThat(crashReporter.loggedException).isEqualTo(exception) //log the exception
    }

    @Test
    fun insufficientSpaceUpload() = runBlockingTest {
        wasAccessTokenCleared = false
        crashReporter.loggedException = null
        val exception = UploadErrorException("/route", "requestId", LocalizedText("insufficient_space", "en_US"), OTHER)
        val cloudProvider = object : CloudProvider {
            override suspend fun uploadToCloud(file: File): CloudUploadResult {
                return UploadError(exception)
            }
        }

        val uploader = RealUploader(dispatchers, repo, cloudProvider, crashReporter, settings)
        val actual = uploader.uploadEntries(context)

        assertThat(actual).isEqualTo(ListenableWorker.Result.failure())
        assertThat(wasAccessTokenCleared).isFalse() //dont clear access tokens
        assertThat(crashReporter.loggedException).isEqualTo(exception) //log the exception
    }
}