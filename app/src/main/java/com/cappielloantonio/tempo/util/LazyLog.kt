package com.cappielloantonio.tempo.util

import android.util.Log
import androidx.core.util.Supplier

/**
 * A lazy logging utility that uses inline functions to optimize performance.
 *
 * This object provides a thread-safe logging facade that defers message construction
 * until the log level threshold is met. When a log call is short-circuited (i.e., the
 * message level is below the threshold), the message lambda is never invoked, avoiding
 * expensive string construction and allocation.
 *
 * The log level threshold can be adjusted at runtime via the [threshold] property.
 * All logging functions are `inline`, so when inlined by the Kotlin compiler, they
 * produce zero-overhead abstractions with no wrapper allocations.
 *
 * ## Usage (Kotlin - Lazy)
 * ```kotlin
 * LazyLog.d { "Debug message: \${expensiveCompute()}" }
 * LazyLog.e({ "Error occurred" }, exception)
 * ```
 *
 * ## Usage (Kotlin - Eager)
 * ```kotlin
 * LazyLog.d("Debug message")
 * LazyLog.e("Error occurred", exception)
 * ```
 *
 * ## Usage (Java - Lazy)
 * ```java
 * LazyLog.d(LazyLog.j2k(() -> "Debug message: " + expensiveCompute()));
 * LazyLog.e(LazyLog.j2k(() -> "Error occurred", exception));
 * ```
 *
 * ## Usage (Java - Eager)
 * ```java
 * LazyLog.d("Debug message");
 * LazyLog.e("Error occurred", exception);
 * ```
 */
object LazyLog {

    /**
     * Enum representing different logging levels with their priority order.
     *
     * Higher priority values represent more severe log levels. Only messages with
     * a priority >= the current [threshold] will be logged.
     *
     * @property priority An integer that determines the severity order.
     */
    enum class LogLevel(val priority: Int) {
        VERBOSE(0),
        DEBUG(1),
        INFO(2),
        WARN(3),
        ERROR(4),
        NONE(5)
    }

    /**
     * The current logging threshold. Messages below this level are not logged.
     *
     * Defaults to [LogLevel.INFO]. Change this at runtime to control verbosity.
     * This property is marked `@Volatile` to ensure thread-safe visibility across threads.
     */
    @Volatile var threshold = LogLevel.INFO

    /**
     * Determines whether a message at the given [level] should be logged.
     *
     * This is a fast check performed before expensive message construction.
     *
     * @param level The log level to check.
     * @return `true` if the level's priority is >= the threshold's priority, `false` otherwise.
     */
    inline fun logGuard(level: LogLevel): Boolean = level.priority >= threshold.priority

    /**
     * Extracts the simple class name of the caller for use as a log tag.
     *
     * This function inspects the current call stack to find the first stack frame
     * that is not from the LazyLog class itself, then extracts the simple (non-qualified)
     * class name from that frame.
     *
     * @return The simple class name of the caller, or `"LazyLog"` if it cannot be determined.
     */
    inline fun tag(): String {
        val throwable = Throwable()
        val stack: Array<StackTraceElement> = throwable.stackTrace

        val callerElement: StackTraceElement? = stack.firstOrNull { element ->
            element.className != LazyLog::class.java.name
        }

        val fullyQualifiedName: String? = callerElement?.className
        val baseName: String? = fullyQualifiedName?.substringAfterLast('.')

        return baseName ?: "LazyLog"
    }

    /**
     * Conditionally logs a message if it passes the log level guard.
     *
     * If [logGuard] returns `false` for the given [level], the [message] lambda is
     * never invoked, avoiding expensive computation. When the call is inlined by the
     * compiler, short-circuited calls produce no overhead.
     *
     * @param level The log level for this message.
     * @param message A lambda that produces the log message. Only invoked if the guard passes.
     * @param t An optional throwable to include in the log output.
     */
    inline fun logOrShortCircuit(level: LogLevel, crossinline message: () -> String, t: Throwable?) {
        if (!logGuard(level)) return

        val msg = message() // <- Stripped at compile-time when short-circuit'ed
        val tag = tag()
        systemLog(level, tag, msg, t)
    }

    /**
     * Eagerly logs a message if it passes the log level guard.
     *
     * Unlike the lazy overloads, this function evaluates the message immediately,
     * regardless of the log level. Use this for pre-computed or simple string messages.
     *
     * @param level The log level for this message.
     * @param message The log message string.
     * @param t An optional throwable to include in the log output.
     */
    inline fun logEager(level: LogLevel, message: String, t: Throwable?) {
        if (!logGuard(level)) return

        val tag = tag()
        systemLog(level, tag, message, t)
    }

    /**
     * Sends a log message to Android's system logger.
     *
     * Delegates to the appropriate [Log] method based on the [level].
     *
     * @param level The log level determining which Log method to call.
     * @param tag The log tag (typically the class name).
     * @param msg The message string to log.
     * @param t An optional throwable to log alongside the message.
     */
    inline fun systemLog(level: LogLevel, tag: String, msg: String, t: Throwable?) {
        when (level) {
            LogLevel.VERBOSE -> Log.v(tag, msg, t)
            LogLevel.DEBUG   -> Log.d(tag, msg, t)
            LogLevel.INFO    -> Log.i(tag, msg, t)
            LogLevel.WARN    -> Log.w(tag, msg, t)
            LogLevel.ERROR   -> Log.e(tag, msg, t)
            LogLevel.NONE    -> {}
        }
    }

    /**
     * Convert a Java `Supplier<T>` into a Kotlin `() -> T` lambda.
     *
     * This is useful for calling LazyLog from Java code where lambda syntax
     * may be less convenient.
     *
     * @param T The type of value supplied.
     * @param supplier A Java Supplier that produces a value of type T.
     * @return A Kotlin lambda that wraps the supplier's get() method.
     */
    @JvmStatic
    inline fun <T> j2k(supplier: Supplier<T>): () -> T = {
        supplier.get()
    }

    /**
     * Convert a Java `Supplier<T>` and `Throwable` into a Kotlin `Pair<() -> T, Throwable>`.
     *
     * This overload is useful when you want to log both a lazily-computed message
     * and an exception from Java code.
     *
     * @param T The type of value supplied.
     * @param supplier A Java Supplier that produces a value of type T.
     * @param throwable The throwable to pair with the message supplier.
     * @return A Pair containing a Kotlin lambda that wraps the supplier and the throwable.
     */
    @JvmStatic
    inline fun <T> j2k(supplier: Supplier<T>, throwable: Throwable): Pair<() -> T, Throwable> =
        ({ supplier.get() }) to throwable

    // VERBOSE LOGGING

    /**
     * Logs a verbose-level message (lazy evaluation).
     *
     * @param message A lambda that produces the log message.
     */
    @JvmStatic
    inline fun v(crossinline message: () -> String) =
        logOrShortCircuit(LogLevel.VERBOSE, message, null)

    /**
     * Logs a verbose-level message with an associated throwable (lazy evaluation).
     *
     * @param message A lambda that produces the log message.
     * @param t The throwable to include in the log output.
     */
    @JvmStatic
    inline fun v(crossinline message: () -> String, t: Throwable) =
        logOrShortCircuit(LogLevel.VERBOSE, message, t)

    /**
     * Logs a verbose-level message (eager evaluation).
     *
     * @param message The log message string.
     */
    @JvmStatic
    inline fun v(message: String) =
        logEager(LogLevel.VERBOSE, message, null)

    /**
     * Logs a verbose-level message with an associated throwable (eager evaluation).
     *
     * @param message The log message string.
     * @param t The throwable to include in the log output.
     */
    @JvmStatic
    inline fun v(message: String, t: Throwable) =
        logEager(LogLevel.VERBOSE, message, t)

    // DEBUG LOGGING

    /**
     * Logs a debug-level message (lazy evaluation).
     *
     * @param message A lambda that produces the log message.
     */
    @JvmStatic
    inline fun d(crossinline message: () -> String) =
        logOrShortCircuit(LogLevel.DEBUG, message, null)

    /**
     * Logs a debug-level message with an associated throwable (lazy evaluation).
     *
     * @param message A lambda that produces the log message.
     * @param t The throwable to include in the log output.
     */
    @JvmStatic
    inline fun d(crossinline message: () -> String, t: Throwable) =
        logOrShortCircuit(LogLevel.DEBUG, message, t)

    /**
     * Logs a debug-level message (eager evaluation).
     *
     * @param message The log message string.
     */
    @JvmStatic
    inline fun d(message: String) =
        logEager(LogLevel.DEBUG, message, null)

    /**
     * Logs a debug-level message with an associated throwable (eager evaluation).
     *
     * @param message The log message string.
     * @param t The throwable to include in the log output.
     */
    @JvmStatic
    inline fun d(message: String, t: Throwable) =
        logEager(LogLevel.DEBUG, message, t)

    // INFO LOGGING

    /**
     * Logs an info-level message (lazy evaluation).
     *
     * @param message A lambda that produces the log message.
     */
    @JvmStatic
    inline fun i(crossinline message: () -> String) =
        logOrShortCircuit(LogLevel.INFO, message, null)

    /**
     * Logs an info-level message with an associated throwable (lazy evaluation).
     *
     * @param message A lambda that produces the log message.
     * @param t The throwable to include in the log output.
     */
    @JvmStatic
    inline fun i(crossinline message: () -> String, t: Throwable) =
        logOrShortCircuit(LogLevel.INFO, message, t)

    /**
     * Logs an info-level message (eager evaluation).
     *
     * @param message The log message string.
     */
    @JvmStatic
    inline fun i(message: String) =
        logEager(LogLevel.INFO, message, null)

    /**
     * Logs an info-level message with an associated throwable (eager evaluation).
     *
     * @param message The log message string.
     * @param t The throwable to include in the log output.
     */
    @JvmStatic
    inline fun i(message: String, t: Throwable) =
        logEager(LogLevel.INFO, message, t)

    // WARN LOGGING

    /**
     * Logs a warning-level message (lazy evaluation).
     *
     * @param message A lambda that produces the log message.
     */
    @JvmStatic
    inline fun w(crossinline message: () -> String) =
        logOrShortCircuit(LogLevel.WARN, message, null)

    /**
     * Logs a warning-level message with an associated throwable (lazy evaluation).
     *
     * @param message A lambda that produces the log message.
     * @param t The throwable to include in the log output.
     */
    @JvmStatic
    inline fun w(crossinline message: () -> String, t: Throwable) =
        logOrShortCircuit(LogLevel.WARN, message, t)

    /**
     * Logs a warning-level message (eager evaluation).
     *
     * @param message The log message string.
     */
    @JvmStatic
    inline fun w(message: String) =
        logEager(LogLevel.WARN, message, null)

    /**
     * Logs a warning-level message with an associated throwable (eager evaluation).
     *
     * @param message The log message string.
     * @param t The throwable to include in the log output.
     */
    @JvmStatic
    inline fun w(message: String, t: Throwable) =
        logEager(LogLevel.WARN, message, t)

    // ERROR LOGGING

    /**
     * Logs an error-level message (lazy evaluation).
     *
     * @param message A lambda that produces the log message.
     */
    @JvmStatic
    inline fun e(crossinline message: () -> String) =
        logOrShortCircuit(LogLevel.ERROR, message, null)

    /**
     * Logs an error-level message with an associated throwable (lazy evaluation).
     *
     * @param message A lambda that produces the log message.
     * @param t The throwable to include in the log output.
     */
    @JvmStatic
    inline fun e(crossinline message: () -> String, t: Throwable) =
        logOrShortCircuit(LogLevel.ERROR, message, t)

    /**
     * Logs an error-level message (eager evaluation).
     *
     * @param message The log message string.
     */
    @JvmStatic
    inline fun e(message: String) =
        logEager(LogLevel.ERROR, message, null)

    /**
     * Logs an error-level message with an associated throwable (eager evaluation).
     *
     * @param message The log message string.
     * @param t The throwable to include in the log output.
     */
    @JvmStatic
    inline fun e(message: String, t: Throwable) =
        logEager(LogLevel.ERROR, message, t)

}
