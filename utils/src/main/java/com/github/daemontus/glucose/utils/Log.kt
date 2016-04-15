package com.github.daemontus.glucose.utils

import android.util.Log

enum class LogLevel(
        val androidLogLevel: Int
) : Comparable<LogLevel> {
    VERBOSE(Log.VERBOSE), DEBUG(Log.DEBUG), INFO(Log.INFO), WARNING(Log.WARN), ERROR(Log.ERROR);
}

interface Logger {
    fun logMessage(level: LogLevel, loggable: () -> String)
    fun logThrowable(level: LogLevel, e: Throwable, loggable: () -> String)
}

var debugLogger: Logger? = AndroidConsoleLogger()
var productionLogger: Logger? = null

fun logD(loggable: () -> String) {
    debugLogger?.logMessage(LogLevel.DEBUG, loggable)
    productionLogger?.logMessage(LogLevel.DEBUG, loggable)
}

fun logV(loggable: () -> String) {
    debugLogger?.logMessage(LogLevel.VERBOSE, loggable)
    productionLogger?.logMessage(LogLevel.VERBOSE, loggable)
}

fun logW(loggable: () -> String) {
    debugLogger?.logMessage(LogLevel.WARNING, loggable)
    productionLogger?.logMessage(LogLevel.WARNING, loggable)
}

fun logI(loggable: () -> String) {
    debugLogger?.logMessage(LogLevel.INFO, loggable)
    productionLogger?.logMessage(LogLevel.INFO, loggable)
}

fun logE(loggable: () -> String) {
    debugLogger?.logMessage(LogLevel.ERROR, loggable)
    productionLogger?.logMessage(LogLevel.ERROR, loggable)
}

fun logD(e: Throwable, loggable: () -> String) {
    debugLogger?.logThrowable(LogLevel.DEBUG, e, loggable)
    productionLogger?.logThrowable(LogLevel.DEBUG, e, loggable)
}

fun logV(e: Throwable, loggable: () -> String) {
    debugLogger?.logThrowable(LogLevel.VERBOSE, e, loggable)
    productionLogger?.logThrowable(LogLevel.VERBOSE, e, loggable)
}

fun logW(e: Throwable, loggable: () -> String) {
    debugLogger?.logThrowable(LogLevel.WARNING, e, loggable)
    productionLogger?.logThrowable(LogLevel.WARNING, e, loggable)
}

fun logI(e: Throwable, loggable: () -> String) {
    debugLogger?.logThrowable(LogLevel.INFO, e, loggable)
    productionLogger?.logThrowable(LogLevel.INFO, e, loggable)
}

fun logE(e: Throwable, loggable: () -> String) {
    debugLogger?.logThrowable(LogLevel.ERROR, e, loggable)
    productionLogger?.logThrowable(LogLevel.ERROR, e, loggable)
}