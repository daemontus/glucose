package com.glucose

import android.util.Log

/**
 * We introduce extra LogLevel class because Android Log level system is a little richer and not
 * exactly type safe.
 */
enum class LogLevel(
        val androidLogLevel: Int
) : Comparable<LogLevel> {
    VERBOSE(android.util.Log.VERBOSE), DEBUG(android.util.Log.DEBUG), INFO(android.util.Log.INFO), WARNING(android.util.Log.WARN), ERROR(android.util.Log.ERROR);
}

interface Logger {
    /**
     * Log some data on specified level, if this level is accepted. If not, ignore.
     */
    fun log(level: LogLevel, message: String?, throwable: Throwable?)

    /**
     * True if given log level is accepted.
     */
    fun canLog(level: LogLevel): Boolean
}

object Log {

    val loggers = arrayListOf(AndroidConsoleLogger())

    //Note: Make sure log is called with the same stack depth so that the line can be easily determined
    //Also there is currently a problem with inline functions, since they have bad line numbers.
    //Another problem is default arguments, these also throw off the stack, so avoid them :)

    inline fun v(loggable: () -> String) {
        log(LogLevel.VERBOSE, unwrap(LogLevel.VERBOSE, loggable))
    }

    fun v(message: String?) {
        log(LogLevel.VERBOSE, message, null)
    }

    fun v(message: String?, throwable: Throwable?) {
        log(LogLevel.VERBOSE, message, throwable)
    }

    inline fun d(loggable: () -> String) {
        log(LogLevel.DEBUG, unwrap(LogLevel.DEBUG, loggable))
    }

    fun d(message: String?) {
        log(LogLevel.DEBUG, message, null)
    }

    fun d(message: String?, throwable: Throwable?) {
        log(LogLevel.DEBUG, message, throwable)
    }

    inline fun i(loggable: () -> String) {
        log(LogLevel.INFO, unwrap(LogLevel.INFO, loggable))
    }

    fun i(message: String?) {
        log(LogLevel.INFO, message, null)
    }

    fun i(message: String?, throwable: Throwable?) {
        log(LogLevel.INFO, message, throwable)
    }

    inline fun w(loggable: () -> String) {
        log(LogLevel.WARNING, unwrap(LogLevel.WARNING, loggable))
    }

    fun w(message: String?) {
        log(LogLevel.WARNING, message, null)
    }

    fun w(message: String?, throwable: Throwable?) {
        log(LogLevel.WARNING, message, throwable)
    }

    inline fun e(loggable: () -> String) {
        log(LogLevel.ERROR, unwrap(LogLevel.ERROR, loggable))
    }

    fun e(message: String?) {
        log(LogLevel.ERROR, message, null)
    }

    fun e(message: String?, throwable: Throwable?) {
        log(LogLevel.ERROR, message, throwable)
    }

    fun log(level: LogLevel, message: String? = null, throwable: Throwable? = null) {
        loggers.forEach {
            it.log(level, message, throwable)
        }
    }

    inline fun unwrap(level: LogLevel, loggable: () -> String): String? {
        return if (loggers.any { it.canLog(level) }) {
            loggable()
        } else {
            null
        }
    }
}