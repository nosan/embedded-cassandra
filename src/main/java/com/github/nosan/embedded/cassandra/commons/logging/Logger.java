/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nosan.embedded.cassandra.commons.logging;

import java.util.Objects;

import com.github.nosan.embedded.cassandra.commons.StringUtils;

/**
 * Simple logger interface.
 * <p>
 * <b>This is a private interface and should not be used outside this project.</b>
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public interface Logger {

	/**
	 * The root logger name.
	 */
	String ROOT = "ROOT";

	/**
	 * Gets a {@link Logger} for the specified class.
	 *
	 * @param clazz the class for which to get the logger
	 * @return the logger
	 */
	static Logger get(Class<?> clazz) {
		Objects.requireNonNull(clazz, "Class must not be null");
		return LoggerFactory.getLogger(clazz.getCanonicalName());
	}

	/**
	 * Gets a {@link Logger} for the specified name.
	 *
	 * @param name the name for which to get the logger
	 * @return the logger
	 */
	static Logger get(String name) {
		Objects.requireNonNull(name, "Name must not be null");
		if (!StringUtils.hasText(name)) {
			name = ROOT;
		}
		return LoggerFactory.getLogger(name.trim());
	}

	/**
	 * Gets the name of this <code>Logger</code> instance.
	 *
	 * @return name of this logger instance
	 */
	String getName();

	/**
	 * Is the logger instance enabled for the ERROR level.
	 *
	 * @return {@code true} if this Logger is enabled for the ERROR level
	 */
	boolean isErrorEnabled();

	/**
	 * Log the message from the provided message at error level.
	 *
	 * @param message the message string to be logged
	 */
	void error(String message);

	/**
	 * Log the message from the provided message at error level.
	 *
	 * @param message the pattern string
	 * @param args object(s) to format
	 * @see java.text.MessageFormat#format(String, Object...)
	 */
	void error(String message, Object... args);

	/**
	 * Log the provided {@code Throwable} and message at error level.
	 *
	 * @param message the message string to be logged
	 * @param throwable the exception (throwable) to be logged
	 */
	void error(Throwable throwable, String message);

	/**
	 * Log the provided {@code Throwable} and message at error level.
	 *
	 * @param throwable the exception (throwable) to be logged
	 * @param message the pattern string
	 * @param args object(s) to format
	 * @see java.text.MessageFormat#format(String, Object...)
	 */
	void error(Throwable throwable, String message, Object... args);

	/**
	 * Is the logger instance enabled for the WARN level.
	 *
	 * @return {@code true} if this Logger is enabled for the WARN level
	 */
	boolean isWarnEnabled();

	/**
	 * Log the message from the provided message at warn level.
	 *
	 * @param message the message string to be logged
	 */
	void warn(String message);

	/**
	 * Log the message from the provided message at warn level.
	 *
	 * @param message the pattern string
	 * @param args object(s) to format
	 * @see java.text.MessageFormat#format(String, Object...)
	 */
	void warn(String message, Object... args);

	/**
	 * Log the provided {@code Throwable} and message at warn level.
	 *
	 * @param throwable the exception (throwable) to be logged
	 * @param message the message string to be logged
	 */
	void warn(Throwable throwable, String message);

	/**
	 * Log the provided {@code Throwable} and message at warn level.
	 *
	 * @param throwable the exception (throwable) to be logged
	 * @param message the pattern string
	 * @param args object(s) to format
	 * @see java.text.MessageFormat#format(String, Object...)
	 */
	void warn(Throwable throwable, String message, Object... args);

	/**
	 * Is the logger instance enabled for the INFO level.
	 *
	 * @return {@code true} if this Logger is enabled for the INFO level
	 */
	boolean isInfoEnabled();

	/**
	 * Log the message from the provided  at info level.
	 *
	 * @param message the message string to be logged
	 */
	void info(String message);

	/**
	 * Log the message from the provided  at info level.
	 *
	 * @param message the pattern string
	 * @param args object(s) to format
	 * @see java.text.MessageFormat#format(String, Object...)
	 */
	void info(String message, Object... args);

	/**
	 * Log the provided {@code Throwable} and message at info level.
	 *
	 * @param throwable the exception (throwable) to be logged
	 * @param message the message string to be logged
	 */
	void info(Throwable throwable, String message);

	/**
	 * Log the provided {@code Throwable} and message at info level.
	 *
	 * @param throwable the exception (throwable) to be logged
	 * @param message the pattern string
	 * @param args object(s) to format
	 * @see java.text.MessageFormat#format(String, Object...)
	 */
	void info(Throwable throwable, String message, Object... args);

	/**
	 * Is the logger instance enabled for the DEBUG level.
	 *
	 * @return {@code true} if this Logger is enabled for the DEBUG level
	 */
	boolean isDebugEnabled();

	/**
	 * Log the message from the provided message at debug level.
	 *
	 * @param message the message string to be logged
	 */
	void debug(String message);

	/**
	 * Log the message from the provided message at debug level.
	 *
	 * @param message the pattern string
	 * @param args object(s) to format
	 * @see java.text.MessageFormat#format(String, Object...)
	 */
	void debug(String message, Object... args);

	/**
	 * Log the provided {@code Throwable} and message at debug level.
	 *
	 * @param throwable the exception (throwable) to be logged
	 * @param message the message string to be logged
	 */
	void debug(Throwable throwable, String message);

	/**
	 * Log the provided {@code Throwable} and message at debug level.
	 *
	 * @param throwable the exception (throwable) to be logged
	 * @param message the pattern string
	 * @param args object(s) to format
	 * @see java.text.MessageFormat#format(String, Object...)
	 */
	void debug(Throwable throwable, String message, Object... args);

	/**
	 * Is the logger instance enabled for the TRACE level.
	 *
	 * @return {@code true} if this Logger is enabled for the TRACE level
	 */
	boolean isTraceEnabled();

	/**
	 * Log the message from the provided message at trace level.
	 *
	 * @param message the message string to be logged
	 */
	void trace(String message);

	/**
	 * Log the message from the provided message at trace level.
	 *
	 * @param message the pattern string
	 * @param args object(s) to format
	 * @see java.text.MessageFormat#format(String, Object...)
	 */
	void trace(String message, Object... args);

	/**
	 * Log the provided {@code Throwable} and message at trace level.
	 *
	 * @param throwable the exception (throwable) to be logged
	 * @param message the message string to be logged
	 */
	void trace(Throwable throwable, String message);

	/**
	 * Log the provided {@code Throwable} and message at trace level.
	 *
	 * @param throwable the exception (throwable) to be logged
	 * @param message the pattern string
	 * @param args object(s) to format
	 * @see java.text.MessageFormat#format(String, Object...)
	 */
	void trace(Throwable throwable, String message, Object... args);

}
