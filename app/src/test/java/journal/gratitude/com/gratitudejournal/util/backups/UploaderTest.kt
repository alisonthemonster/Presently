package journal.gratitude.com.gratitudejournal.util.backups

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.ListenableWorker
import com.dropbox.core.InvalidAccessTokenException
import com.dropbox.core.LocalizedText
import com.dropbox.core.NetworkIOException
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.auth.AuthError.INVALID_ACCESS_TOKEN
import com.dropbox.core.v2.files.UploadError.OTHER
import com.dropbox.core.v2.files.UploadErrorException
import com.google.common.truth.Truth.assertThat
import journal.gratitude.com.gratitudejournal.coroutine_utils.AppCoroutineDispatchers
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.logging.CrashReporter
import journal.gratitude.com.gratitudejournal.settings.BackupCadence
import journal.gratitude.com.gratitudejournal.settings.RealBackupStatusStore
import journal.gratitude.com.gratitudejournal.util.backups.status.UploadFailure
import journal.gratitude.com.gratitudejournal.util.backups.status.UploadFailureType
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.model.CloudUploadResult
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.UploadError
import journal.gratitude.com.gratitudejournal.model.UploadSuccess
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.CloudProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.lang.Exception
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.fail

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = UploaderTest.TestApplication::class)
class UploaderTest {

    class TestApplication : Application(), Configuration.Provider {
        override val workManagerConfiguration: Configuration
            get() = Configuration.Builder().build()
    }

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private fun TestScope.createDispatchers() = AppCoroutineDispatchers(
        io = StandardTestDispatcher(testScheduler),
        computation = StandardTestDispatcher(testScheduler),
        main = StandardTestDispatcher(testScheduler)
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
        override suspend fun deleteAllEntries() = fail("Not needed in this test")
        override fun searchEntries(query: String): Flow<PagingData<Entry>> =
            fail("Not needed in this test")
    }

    private var wasCloudProviderCalled = false
    private var nowMillis = 1_000L
    private val store = RealBackupStatusStore(
        context.getSharedPreferences("uploader-test", Context.MODE_PRIVATE).also {
            it.edit().clear().commit()
        }
    ) { nowMillis }

    private val cloudProvider = object : CloudProvider {
        override suspend fun uploadToCloud(file: File): CloudUploadResult {
            wasCloudProviderCalled = true
            return UploadSuccess
        }
    }

    private val analytics = object : AnalyticsLogger {
        val events = mutableListOf<Pair<String, Map<String, Any>>>()
        override fun recordEvent(event: String) {
            events += event to emptyMap()
        }
        override fun recordEvent(event: String, details: Map<String, Any>) {
            events += event to details
        }
        override fun recordSelectEvent(selectedContent: String, selectedContentType: String) = fail("Not needed in this test")
        override fun recordEntryAdded(numEntries: Int) = fail("Not needed in this test")
        override fun recordView(viewName: String) = fail("Not needed in this test")
        override fun optOutOfAnalytics() = fail("Not needed in this test")
        override fun optIntoAnalytics() = fail("Not needed in this test")
        override fun setUserProperty(name: String, value: String) = fail("Not needed in this test")
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
        override fun setNotificationsEnabled(enabled: Boolean) = fail("Not needed in this test")
        override fun getNotificationTime(): LocalTime = fail("Not needed in this test")
        override fun setNotificationTime(time: LocalTime) = fail("Not needed in this test")
        override fun hasUserDisabledAlarmReminders(context: Context): Boolean = fail("Not needed in this test")
        override fun hasSeenReminderOnboarding(): Boolean = fail("Not needed in this test")
        override fun markReminderOnboardingSeen() = fail("Not needed in this test")
        override fun clearReminderOnboardingSeen() = fail("Not needed in this test")
        override fun hasRequestedNotificationPermission(): Boolean = fail("Not needed in this test")
        override fun markNotificationPermissionRequested() = fail("Not needed in this test")
        override fun getLinesPerEntryInTimeline(): Int = fail("Not needed in this test")
        override fun shouldShowDayOfWeekInTimeline(): Boolean = fail("Not needed in this test")
        override fun getTimelineFontScale(): Int = fail("Not needed in this test")
        override fun wasDropboxAuthInitiated(): Boolean = fail("Not needed in this test")
        override fun markDropboxAuthAsCancelled() = fail("Not needed in this test")
        override fun markDropboxAuthInitiated() = fail("Not needed in this test")
        override fun isOptedIntoAnalytics(): Boolean = fail("Not needed in this test")
    }

    @Test
    fun emptyRepositoryDoesNothing() = runTest {
        wasCloudProviderCalled = false
        val dispatchers = createDispatchers()
        val repo = object : EntryRepository {
            override suspend fun getEntries(): List<Entry> {
                return emptyList()
            }
            override suspend fun getEntry(date: LocalDate): Entry = fail("Not needed in this test")
            override suspend fun getEntriesFlow(): Flow<List<Entry>> = fail("Not needed in this test")
            override fun getWrittenDates(): LiveData<List<LocalDate>> = fail("Not needed in this test")
            override suspend fun addEntry(entry: Entry) = fail("Not needed in this test")
            override suspend fun addEntries(entries: List<Entry>) = fail("Not needed in this test")
            override suspend fun deleteAllEntries() = fail("Not needed in this test")
            override fun searchEntries(query: String): Flow<PagingData<Entry>> =
                fail("Not needed in this test")
        }

        val uploader = RealUploader(dispatchers, repo, cloudProvider, crashReporter, settings, store, analytics)

        val actual = uploader.uploadEntries(context)

        assertThat(actual).isEqualTo(ListenableWorker.Result.success())
        assertThat(wasCloudProviderCalled).isFalse()
        assertThat(store.getLastDropboxSuccessAt()).isNull()
        assertThat(store.getLastUploadFailure()).isNull()
        assertThat(analytics.events).containsExactly(ATTEMPTED)
    }

    @Test
    fun successfulUpload() = runTest {
        wasCloudProviderCalled = false
        val dispatchers = createDispatchers()
        val uploader = RealUploader(dispatchers, repo, cloudProvider, crashReporter, settings, store, analytics)
        val actual = uploader.uploadEntries(context)

        assertThat(actual).isEqualTo(ListenableWorker.Result.success())
        assertThat(wasCloudProviderCalled).isTrue()
        assertThat(store.getLastDropboxSuccessAt()).isEqualTo(1_000L)
        assertThat(store.getLastUploadFailure()).isNull()
        assertThat(store.getDropboxEnabledAt()).isEqualTo(1_000L)
        assertThat(analytics.events).containsExactly(ATTEMPTED, "dropboxUploadSucceeded" to emptyMap<String, Any>())
    }

    @Test
    fun invalidAccessTokenUpload() = runTest {
        wasAccessTokenCleared = false
        crashReporter.loggedException = null
        val dispatchers = createDispatchers()
        val exception = InvalidAccessTokenException("requestId", "message", INVALID_ACCESS_TOKEN)
        val cloudProvider = object : CloudProvider {
            override suspend fun uploadToCloud(file: File): CloudUploadResult {
                return UploadError(exception)
            }
        }

        val uploader = RealUploader(dispatchers, repo, cloudProvider, crashReporter, settings, store, analytics)
        val actual = uploader.uploadEntries(context)

        assertThat(actual).isEqualTo(ListenableWorker.Result.failure())
        assertThat(wasAccessTokenCleared).isTrue()
        assertThat(crashReporter.loggedException).isEqualTo(exception)
        assertThat(store.getLastUploadFailure())
            .isEqualTo(UploadFailure(UploadFailureType.AUTH, 1_000L))
        assertThat(store.wasDropboxDisconnectedUnexpectedly()).isTrue()
        assertFailedEvent("auth")
    }

    @Test
    fun insufficientSpaceUpload() = runTest {
        wasAccessTokenCleared = false
        crashReporter.loggedException = null
        val dispatchers = createDispatchers()
        val exception = UploadErrorException(
            "/route",
            "requestId",
            LocalizedText("insufficient_space", "en_US"),
            OTHER
        )
        val cloudProvider = object : CloudProvider {
            override suspend fun uploadToCloud(file: File): CloudUploadResult {
                return UploadError(exception)
            }
        }

        val uploader = RealUploader(dispatchers, repo, cloudProvider, crashReporter, settings, store, analytics)
        val actual = uploader.uploadEntries(context)

        assertThat(actual).isEqualTo(ListenableWorker.Result.failure())
        assertThat(wasAccessTokenCleared).isFalse()
        assertThat(crashReporter.loggedException).isEqualTo(exception)
        assertThat(store.getLastUploadFailure())
            .isEqualTo(UploadFailure(UploadFailureType.STORAGE_FULL, 1_000L))
        assertThat(store.wasDropboxDisconnectedUnexpectedly()).isFalse()
        assertFailedEvent("storage_full")
    }

    @Test
    fun networkFailureIsRecordedWithoutDisconnecting() = runTest {
        wasAccessTokenCleared = false
        val dispatchers = createDispatchers()
        val cloudProvider = object : CloudProvider {
            override suspend fun uploadToCloud(file: File): CloudUploadResult {
                return UploadError(NetworkIOException(java.io.IOException("offline")))
            }
        }

        val uploader = RealUploader(dispatchers, repo, cloudProvider, crashReporter, settings, store, analytics)
        val actual = uploader.uploadEntries(context)

        assertThat(actual).isEqualTo(ListenableWorker.Result.failure())
        assertThat(wasAccessTokenCleared).isFalse()
        assertThat(store.getLastUploadFailure())
            .isEqualTo(UploadFailure(UploadFailureType.NETWORK, 1_000L))
        assertThat(store.wasDropboxDisconnectedUnexpectedly()).isFalse()
        assertFailedEvent("network")
    }

    @Test
    fun transientServerFailureLogsFailedEvent() = runTest {
        val cloudProvider = object : CloudProvider {
            override suspend fun uploadToCloud(file: File): CloudUploadResult =
                UploadError(com.dropbox.core.ServerException("requestId", "boom"))
        }
        RealUploader(createDispatchers(), repo, cloudProvider, crashReporter, settings, store, analytics)
            .uploadEntries(context)
        assertFailedEvent(UploadFailureType.TRANSIENT_SERVER.code)
    }

    @Test
    fun otherFailureLogsFailedEvent() = runTest {
        val cloudProvider = object : CloudProvider {
            override suspend fun uploadToCloud(file: File): CloudUploadResult =
                UploadError(IllegalStateException("weird"))
        }
        RealUploader(createDispatchers(), repo, cloudProvider, crashReporter, settings, store, analytics)
            .uploadEntries(context)
        assertFailedEvent("other")
    }

    @Test
    fun csvFailureLogsOtherFailedEvent() = runTest {
        wasCloudProviderCalled = false
        //an entry with a null date makes CSV creation throw, which surfaces as CsvFileError
        val brokenEntry = Entry(LocalDate.of(2021, 12, 25), "x").also {
            Entry::class.java.getDeclaredField("entryDate").apply { isAccessible = true }.set(it, null)
        }
        val repo = object : EntryRepository by this@UploaderTest.repo {
            override suspend fun getEntries(): List<Entry> = listOf(brokenEntry)
        }
        val actual = RealUploader(createDispatchers(), repo, cloudProvider, crashReporter, settings, store, analytics)
            .uploadEntries(context)

        assertThat(actual).isEqualTo(ListenableWorker.Result.failure())
        assertThat(wasCloudProviderCalled).isFalse()
        assertFailedEvent("other")
    }

    private val ATTEMPTED = "dropboxUploadAttempted" to emptyMap<String, Any>()

    private fun assertFailedEvent(code: String) {
        assertThat(analytics.events)
            .containsExactly(ATTEMPTED, "dropboxUploadFailed" to mapOf<String, Any>("failureType" to code))
    }

    @Test
    fun networkFailureDoesNotOverwriteUnresolvedStorageFull() = runTest {
        nowMillis = 500L
        store.recordDropboxUploadFailure(UploadFailureType.STORAGE_FULL)
        nowMillis = 2_000L
        val dispatchers = createDispatchers()
        val cloudProvider = object : CloudProvider {
            override suspend fun uploadToCloud(file: File): CloudUploadResult {
                return UploadError(NetworkIOException(java.io.IOException("offline")))
            }
        }

        RealUploader(dispatchers, repo, cloudProvider, crashReporter, settings, store, analytics)
            .uploadEntries(context)

        assertThat(store.getLastUploadFailure())
            .isEqualTo(UploadFailure(UploadFailureType.STORAGE_FULL, 500L))
    }

    @Test
    fun existingBaselineIsNotOverwritten() = runTest {
        nowMillis = 123L
        store.seedDropboxBaselineIfMissing()
        nowMillis = 9_999L
        val dispatchers = createDispatchers()

        RealUploader(dispatchers, repo, cloudProvider, crashReporter, settings, store, analytics)
            .uploadEntries(context)

        assertThat(store.getDropboxEnabledAt()).isEqualTo(123L)
    }
}
