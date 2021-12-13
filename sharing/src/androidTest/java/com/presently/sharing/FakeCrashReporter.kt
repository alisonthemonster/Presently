package com.presently.sharing

import com.presently.logging.CrashReporter
import com.presently.logging.wiring.CrashReportingModule
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import java.lang.Exception
import javax.inject.Singleton

class FakeCrashReporter: CrashReporter {
    override fun logHandledException(exception: Exception) {}
    override fun optOutOfCrashReporting() {}
    override fun optIntoCrashReporting() {}
}

/**
 * CrashReporter binding to use in instrumented tests.
 *
 * Hilt will inject a [FakeCrashReporter] instead of a [RealCrashReporter].
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [CrashReportingModule::class]
)
abstract class FakeAnalyticsModule {
    @Singleton
    @Binds
    abstract fun bindCrashReporter(crashReporter: FakeCrashReporter): CrashReporter
}