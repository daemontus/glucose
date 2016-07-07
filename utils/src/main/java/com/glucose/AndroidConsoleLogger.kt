package com.glucose

import android.util.Log

class AndroidConsoleLogger(
        private val printTraceInfo: Boolean = true,
        private val minimalLogLevel: LogLevel = LogLevel.VERBOSE,
        private val stackTraceDepth: Int = 6
): Logger {

    override fun log(level: LogLevel, message: String?, throwable: Throwable?) {
        if (canLog(level)) {
            val tag = getTag()
            val actualMessage = when {
                printTraceInfo && message != null -> "${getLineNumber()} | $message"
                printTraceInfo && message == null -> "${getLineNumber()}"
                else -> message ?: ""
            }
            if (throwable != null) {
                when (level) {
                    LogLevel.VERBOSE -> android.util.Log.v(tag, actualMessage, throwable)
                    LogLevel.DEBUG -> android.util.Log.d(tag, actualMessage, throwable)
                    LogLevel.INFO -> android.util.Log.i(tag, actualMessage, throwable)
                    LogLevel.WARNING -> android.util.Log.w(tag, actualMessage, throwable)
                    LogLevel.ERROR -> android.util.Log.e(tag, actualMessage, throwable)
                }
            } else {
                when (level) {
                    LogLevel.VERBOSE -> android.util.Log.v(tag, actualMessage)
                    LogLevel.DEBUG -> android.util.Log.d(tag, actualMessage)
                    LogLevel.INFO -> android.util.Log.i(tag, actualMessage)
                    LogLevel.WARNING -> android.util.Log.w(tag, actualMessage)
                    LogLevel.ERROR -> android.util.Log.e(tag, actualMessage)
                }
            }
        }
    }

    override fun canLog(level: LogLevel): Boolean = level >= minimalLogLevel

    private fun getTag(): String {
        val element = Thread.currentThread().stackTrace[stackTraceDepth]
        return element.className.substring(element.className.lastIndexOf("") + 1)
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