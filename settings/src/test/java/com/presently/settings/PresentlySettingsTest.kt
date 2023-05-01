package com.presently.settings

import android.content.SharedPreferences
import com.dropbox.core.oauth.DbxCredential
import com.google.common.truth.Truth.assertThat
import com.presently.logging.AnalyticsLogger
import junit.framework.Assert.fail
import org.junit.Test
import org.threeten.bp.LocalTime
import java.util.Date

class PresentlySettingsTest {

    @Test
    fun `GIVEN RealPresentlySettings WHEN getCurrentTheme is called THEN shared preferences is called`() {
        val expected = "currTheme"
        val sharedPrefs = getFakeSharedPreferences(expected)
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        val actual = settings.getCurrentTheme()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN setTheme is called THEN shared preferences is called`() {
        editStringWasCalled = false
        editString = ""
        val expected = "newTheme"
        val sharedPrefs = getFakeSharedPreferences()
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        settings.setTheme(expected)

        assertThat(editStringWasCalled).isTrue()
        assertThat(editString).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN setTheme is called THEN analytics event is logged`() {
        content = ""
        val sharedPrefs = getFakeSharedPreferences()
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        settings.setTheme("newTheme")

        assertThat(recordSelectEventWasCalled).isTrue()
        assertThat(content).isEqualTo("newTheme")
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN isBiometricsEnabled is called THEN shared preferences is called`() {
        val expected = true
        val sharedPrefs = getFakeSharedPreferences(boolean = expected)
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        val actual = settings.isBiometricsEnabled()
        assertThat(actual).isEqualTo(expected)
    }

    // todo
    @Test
    fun `GIVEN RealPresentlySettings with more than 5 minutes WHEN shouldLockApp is called THEN shared preferences is called`() {
        val fiveMinutesInThePast = Date(System.currentTimeMillis()).time - 300001L
        val sharedPrefs = getFakeSharedPreferences(boolean = true, long = fiveMinutesInThePast)
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        val actual = settings.shouldLockApp()
        assertThat(actual).isTrue()
    }

    @Test
    fun `GIVEN RealPresentlySettings with less than 5 minutes WHEN shouldLockApp is called THEN shared preferences is called`() {
        val expected = false
        val now = Date(System.currentTimeMillis()).time
        val sharedPrefs = getFakeSharedPreferences(long = now)
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        val actual = settings.shouldLockApp()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN shouldShowQuote is called THEN shared preferences is called`() {
        val expected = false
        val sharedPrefs = getFakeSharedPreferences(boolean = expected)
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        val actual = settings.shouldShowQuote()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings and daily cadence WHEN getAutomaticBackupCadence is called THEN shared preferences is called`() {
        val expected = BackupCadence.DAILY
        val sharedPrefs = getFakeSharedPreferences(string = "0")
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        val actual = settings.getAutomaticBackupCadence()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings and weekly cadence WHEN getAutomaticBackupCadence is called THEN shared preferences is called`() {
        val expected = BackupCadence.WEEKLY
        val sharedPrefs = getFakeSharedPreferences(string = "1")
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        val actual = settings.getAutomaticBackupCadence()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings and every change cadence WHEN getAutomaticBackupCadence is called THEN shared preferences is called`() {
        val expected = BackupCadence.EVERY_CHANGE
        val sharedPrefs = getFakeSharedPreferences(string = "2")
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        val actual = settings.getAutomaticBackupCadence()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings with no language preference WHEN getLocale is called THEN locale is checked`() {
        val sharedPrefs = getFakeSharedPreferences(string = null)
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        val actual = settings.getLocale()
        assertThat(actual).isNotNull()
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN getLocale is called THEN shared preferences is called`() {
        val expected = "fr"
        val sharedPrefs = getFakeSharedPreferences(string = expected)
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        val actual = settings.getLocale()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN hasEnabledNotifications is called THEN shared preferences is called`() {
        val expected = false
        val sharedPrefs = getFakeSharedPreferences(boolean = expected)
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        val actual = settings.hasEnabledNotifications()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings with no time set WHEN getNotificationTime is called THEN shared preferences is called`() {
        val expected = LocalTime.parse("21:00")
        val sharedPrefs = getFakeSharedPreferences(string = null)
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        val actual = settings.getNotificationTime()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN getNotificationTime is called THEN shared preferences is called`() {
        val expected = LocalTime.parse("11:00")
        val sharedPrefs = getFakeSharedPreferences(string = "11:00")
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        val actual = settings.getNotificationTime()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN getLinesPerEntryInTimeline is called THEN shared preferences is called`() {
        val expected = 12
        val sharedPrefs = getFakeSharedPreferences(int = expected)
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        val actual = settings.getLinesPerEntryInTimeline()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN shouldShowDayOfWeekInTimeline is called THEN shared preferences is called`() {
        val expected = true
        val sharedPrefs = getFakeSharedPreferences(boolean = expected)
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        val actual = settings.shouldShowDayOfWeekInTimeline()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings and no token WHEN getAccessToken is called THEN shared preferences is called`() {
        val expected = null
        val sharedPrefs = getFakeSharedPreferences(string = expected)
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        val actual = settings.getAccessToken()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings AND a user that attempted to auth with Dropbox WHEN getAccessToken is called THEN shared preferences is called`() {
        val expected = null
        val sharedPrefs = getFakeSharedPreferences(string = "attempted")
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        val actual = settings.getAccessToken()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN getAccessToken is called THEN shared preferences is called`() {
        val expected = DbxCredential("accessToken")
        val sharedPrefs = getFakeSharedPreferences(string = "accessToken")
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        val actual = settings.getAccessToken()
        assertThat(actual.toString()).isEqualTo(expected.toString())
    }

    @Test
    fun `GIVEN RealPresentlySettings AND a serialized DbxCredential with refresh tokens WHEN getAccessToken is called THEN shared preferences is called`() {
        val expected = DbxCredential("accessToken", 1000L, "refreshtoken", "appkey", null)
        val sharedPrefs = getFakeSharedPreferences(string = expected.toString())
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        val actual = settings.getAccessToken()
        assertThat(actual.toString()).isEqualTo(expected.toString())
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN setAccessToken is called THEN shared preferences is called`() {
        editStringWasCalled = false
        editString = ""
        val credential = DbxCredential("accessToken", 1000L, "refreshtoken", "appkey", null)
        val expected = credential.toString()
        val sharedPrefs = getFakeSharedPreferences()
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        settings.setAccessToken(credential)

        assertThat(editStringWasCalled).isTrue()
        assertThat(editString).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN setAccessToken is called THEN an analytics event is logged`() {
        recordedEvent = ""
        recordEventWasCalled = false
        val credential = DbxCredential("accessToken", 1000L, "refreshtoken", "appkey", null)
        val expected = "dropboxAuthorizaitonSuccess"
        val sharedPrefs = getFakeSharedPreferences()
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        settings.setAccessToken(credential)

        assertThat(recordEventWasCalled).isTrue()
        assertThat(recordedEvent).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN markDropboxAuthInitiated is called THEN shared preferences is called`() {
        editStringWasCalled = false
        editString = ""
        val expected = "attempted"
        val sharedPrefs = getFakeSharedPreferences()
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        settings.markDropboxAuthInitiated()

        assertThat(editStringWasCalled).isTrue()
        assertThat(editString).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings AND a user that attempted to auth with dropbox WHEN wasDropboxAuthInitiated is called THEN return true`() {
        val sharedPrefs = getFakeSharedPreferences(string = "attempted")
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        val actual = settings.wasDropboxAuthInitiated()
        assertThat(actual).isTrue()
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN markDropboxAuthAsCancelled is called THEN shared preferences is called`() {
        removeWasCalled = false
        putBooleanWasCalled = false
        val sharedPrefs = getFakeSharedPreferences()
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        settings.markDropboxAuthAsCancelled()

        assertThat(removeWasCalled).isTrue()
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN markDropboxAuthAsCancelled is called THEN an analytics event is logged`() {
        recordedEvent = ""
        recordEventWasCalled = false
        val expected = "dropboxAuthorizaitonQuit"
        val sharedPrefs = getFakeSharedPreferences()
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        settings.markDropboxAuthAsCancelled()

        assertThat(recordEventWasCalled).isTrue()
        assertThat(recordedEvent).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN clearAccessToken is called THEN shared preferences is called`() {
        removeWasCalled = false
        val sharedPrefs = getFakeSharedPreferences()
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        settings.clearAccessToken()

        assertThat(removeWasCalled).isTrue()
    }

    @Test
    fun `GIVEN RealPresentlySettings WHEN isOptedIntoAnalytics is called THEN shared preferences is called`() {
        val expected = true
        val sharedPrefs = getFakeSharedPreferences(boolean = expected)
        val settings = RealPresentlySettings(sharedPrefs, fakeAnalyticsLogger)
        val actual = settings.isOptedIntoAnalytics()
        assertThat(actual).isEqualTo(expected)
    }

    var recordEventWasCalled = false
    var recordedEvent = ""
    var recordSelectEventWasCalled = false
    var content = ""
    var contentType = ""
    var analyticsIsOptedIn = false

    private val fakeAnalyticsLogger = object : AnalyticsLogger {
        override fun recordEvent(event: String) {
            recordEventWasCalled = true
            recordedEvent = event
        }

        override fun recordEvent(event: String, details: Map<String, Any>) {
            fail("recordEvent with details should not be called")
        }

        override fun recordSelectEvent(selectedContent: String, selectedContentType: String) {
            recordSelectEventWasCalled = true
            content = selectedContent
            contentType = selectedContentType
        }

        override fun recordEntryAdded(numEntries: Int) {
            fail("recordEntryAdded should not be called")
        }

        override fun recordView(viewName: String) {
            fail("recordView should not be called")
        }

        override fun optOutOfAnalytics() {
            analyticsIsOptedIn = false
        }

        override fun optIntoAnalytics() {
            analyticsIsOptedIn = true
        }
    }

    var editStringWasCalled = false
    var editString = ""
    var editLongWasCalled = false
    var removeWasCalled = false
    var putBooleanWasCalled = false

    private fun getFakeSharedPreferences(string: String? = "", int: Int = 0, long: Long = 0L, boolean: Boolean = false): SharedPreferences {
        val editor = object : SharedPreferences.Editor {
            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                editStringWasCalled = true
                editString = requireNotNull(value)
                return this
            }

            override fun putStringSet(
                key: String?,
                values: MutableSet<String>?,
            ): SharedPreferences.Editor {
                fail("putStringSet should not be called")
                return this
            }

            override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
                fail("putInt should not be called")
                return this
            }

            override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
                editLongWasCalled = true
                return this
            }

            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
                fail("putFloat should not be called")
                return this
            }

            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
                putBooleanWasCalled = true
                return this
            }

            override fun remove(key: String?): SharedPreferences.Editor {
                removeWasCalled = true
                return this
            }

            override fun clear(): SharedPreferences.Editor {
                fail("clear should not be called")
                return this
            }

            override fun commit(): Boolean {
                return true
            }

            override fun apply() {}
        }

        return object : SharedPreferences {
            override fun getAll(): MutableMap<String, Any> {
                fail("getAll should be used")
                return mutableMapOf()
            }

            override fun getString(key: String?, defValue: String?): String? {
                return string ?: defValue
            }

            override fun getStringSet(
                key: String?,
                defValues: MutableSet<String>?,
            ): MutableSet<String> {
                fail("getStringSet should be used")
                return mutableSetOf()
            }

            override fun getInt(key: String?, defValue: Int): Int {
                return int
            }

            override fun getLong(key: String?, defValue: Long): Long {
                return long
            }

            override fun getFloat(key: String?, defValue: Float): Float {
                fail("getFloat should be used")
                return 0f
            }

            override fun getBoolean(key: String?, defValue: Boolean): Boolean {
                return boolean
            }

            override fun contains(key: String?): Boolean {
                fail("contains should be used")
                return false
            }

            override fun edit(): SharedPreferences.Editor {
                return editor
            }

            override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
                fail("registerOnSharedPreferenceChangeListener should be used")
            }

            override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
                fail("unregisterOnSharedPreferenceChangeListener should be used")
            }
        }
    }
}
