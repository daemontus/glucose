package com.github.daemontus.glucose.utils

import android.util.Log

class AndroidConsoleLogger(
        private val printTraceInfo: Boolean = true,
        private val minimalLogLevel: LogLevel = LogLevel.VERBOSE,
        private val stackTraceDepth: Int = 5
): Logger {

    override fun logMessage(level: LogLevel, loggable: () -> String) {
        if (level >= minimalLogLevel) {
            val message = if (printTraceInfo) {
                "${getLineNumber()} | ${loggable()}"
            } else loggable()
            when (level) {
                LogLevel.VERBOSE -> Log.v(getTag(), message)
                LogLevel.DEBUG -> Log.d(getTag(), message)
                LogLevel.INFO -> Log.i(getTag(), message)
                LogLevel.WARNING -> Log.w(getTag(), message)
                LogLevel.ERROR -> Log.e(getTag(), message)
            }
        }
    }

    override fun logThrowable(level: LogLevel, e: Throwable, loggable: () -> String) {
        if (level >= minimalLogLevel) {
            val message = if (printTraceInfo) {
                "${getLineNumber()} | ${loggable()}"
            } else loggable()
            when (level) {
                LogLevel.VERBOSE -> Log.v(getTag(), message, e)
                LogLevel.DEBUG -> Log.d(getTag(), message, e)
                LogLevel.INFO -> Log.i(getTag(), message, e)
                LogLevel.WARNING -> Log.w(getTag(), message, e)
                LogLevel.ERROR -> Log.e(getTag(), message, e)
            }
        }
    }

    private fun getTag(): String {
        val element = Thread.currentThread().stackTrace[stackTraceDepth]
        return element.className.substring(element.className.lastIndexOf(".") + 1)
    }

    private fun getLineNumber(): String {
        val element = Thread.currentThread().stackTrace[stackTraceDepth]
        val fullClassName = element.className
        val fileName = element.fileName
        val methodName = element.methodName
        val lineNumber = element.lineNumber
        return "$fullClassName.$methodName($fileName:$lineNumber)"
    }

}