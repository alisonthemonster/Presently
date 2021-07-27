package com.presently.logging

import java.lang.Exception

interface CrashReporter {

    fun logHandledException(exception: Exception)
}