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

import java.io.PrintStream;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class LoggerFactory {

	private static final Map<String, Logger> LOGGERS = new ConcurrentHashMap<>(64);

	private static final LogApi LOG_API;

	private static final String SLF4J_API = "org.slf4j.LoggerFactory";

	static {
		LogApi api;
		if (isPresent(SLF4J_API, LoggerFactory.class.getClassLoader()) && !isNOPLoggerFactory()) {
			api = LogApi.SLF4J;
		}
		else {
			api = LogApi.CONSOLE;
		}
		LOG_API = api;
	}

	private LoggerFactory() {
	}

	static Logger getLogger(String name) {
		return LOGGERS.computeIfAbsent(name, LoggerFactory::createLogger);
	}

	private static Logger createLogger(String name) {
		if (LOG_API == LogApi.SLF4J) {
			return new Slf4jLogger(org.slf4j.LoggerFactory.getLogger(name));
		}
		return new ConsoleLogger(name, System.out, System.err);
	}

	private static boolean isNOPLoggerFactory() {
		return org.slf4j.LoggerFactory.getILoggerFactory() instanceof org.slf4j.helpers.NOPLoggerFactory;
	}

	private static boolean isPresent(String clazz, ClassLoader classLoader) {
		try {
			Class.forName(clazz, false, classLoader);
			return true;
		}
		catch (ClassNotFoundException ex) {
			return false;
		}
	}

	private static String format(String message, Object... args) {
		if (message == null) {
			return null;
		}
		if (args == null || args.length == 0) {
			return message;
		}
		return MessageFormat.format(message, args);
	}

	private enum LogApi {
		SLF4J,
		CONSOLE
	}

	private static final class Slf4jLogger implements Logger {

		private final org.slf4j.Logger logger;

		private Slf4jLogger(org.slf4j.Logger logger) {
			this.logger = logger;
		}

		@Override
		public String getName() {
			return this.logger.getName();
		}

		@Override
		public boolean isErrorEnabled() {
			return this.logger.isErrorEnabled();
		}

		@Override
		public void error(String message) {
			if (this.logger.isErrorEnabled()) {
				this.logger.error(message);
			}
		}

		@Override
		public void error(String message, Object... args) {
			if (this.logger.isErrorEnabled()) {
				this.logger.error(format(message, args));
			}
		}

		@Override
		public void error(Throwable throwable, String message) {
			if (this.logger.isErrorEnabled()) {
				this.logger.error(message, throwable);
			}
		}

		@Override
		public void error(Throwable throwable, String message, Object... args) {
			if (this.logger.isErrorEnabled()) {
				this.logger.error(format(message, args), throwable);
			}
		}

		@Override
		public boolean isWarnEnabled() {
			return this.logger.isWarnEnabled();
		}

		@Override
		public void warn(String message) {
			if (this.logger.isWarnEnabled()) {
				this.logger.warn(message);
			}
		}

		@Override
		public void warn(String message, Object... args) {
			if (this.logger.isWarnEnabled()) {
				this.logger.warn(format(message, args));
			}
		}

		@Override
		public void warn(Throwable throwable, String message) {
			if (this.logger.isWarnEnabled()) {
				this.logger.warn(message, throwable);
			}
		}

		@Override
		public void warn(Throwable throwable, String message, Object... args) {
			if (this.logger.isWarnEnabled()) {
				this.logger.warn(format(message, args), throwable);
			}
		}

		@Override
		public boolean isInfoEnabled() {
			return this.logger.isInfoEnabled();
		}

		@Override
		public void info(String message) {
			if (this.logger.isInfoEnabled()) {
				this.logger.info(message);
			}
		}

		@Override
		public void info(String message, Object... args) {
			if (this.logger.isInfoEnabled()) {
				this.logger.info(format(message, args));
			}
		}

		@Override
		public void info(Throwable throwable, String message) {
			if (this.logger.isInfoEnabled()) {
				this.logger.info(message, throwable);
			}
		}

		@Override
		public void info(Throwable throwable, String message, Object... args) {
			if (this.logger.isInfoEnabled()) {
				this.logger.info(format(message, args), throwable);
			}
		}

		@Override
		public void debug(String message, Object... args) {
			if (this.logger.isDebugEnabled()) {
				this.logger.debug(format(message, args));
			}
		}

		@Override
		public void debug(Throwable throwable, String message) {
			if (this.logger.isDebugEnabled()) {
				this.logger.debug(message, throwable);
			}
		}

		@Override
		public void debug(Throwable throwable, String message, Object... args) {
			if (this.logger.isDebugEnabled()) {
				this.logger.debug(format(message, args), throwable);
			}
		}

		@Override
		public boolean isDebugEnabled() {
			return this.logger.isDebugEnabled();
		}

		@Override
		public void debug(String message) {
			if (this.logger.isDebugEnabled()) {
				this.logger.debug(message);
			}
		}

		@Override
		public void trace(String message, Object... args) {
			if (this.logger.isTraceEnabled()) {
				this.logger.trace(format(message, args));
			}
		}

		@Override
		public void trace(Throwable throwable, String message) {
			if (this.logger.isTraceEnabled()) {
				this.logger.trace(message, throwable);
			}
		}

		@Override
		public void trace(Throwable throwable, String message, Object... args) {
			if (this.logger.isTraceEnabled()) {
				this.logger.trace(format(message, args), throwable);
			}
		}

		@Override
		public boolean isTraceEnabled() {
			return this.logger.isTraceEnabled();
		}

		@Override
		public void trace(String message) {
			if (this.logger.isTraceEnabled()) {
				this.logger.trace(message);
			}
		}

	}

	private static final class ConsoleLogger implements Logger {

		private final String name;

		private final PrintStream out;

		private final PrintStream err;

		private ConsoleLogger(String name, PrintStream out, PrintStream err) {
			this.name = name;
			this.out = out;
			this.err = err;
		}

		@Override
		public synchronized void error(String message, Object... args) {
			this.err.format("%s [%s] ERROR %s - %s%n", getNow(), getThread(), this.name, format(message, args));
		}

		@Override
		public synchronized void error(Throwable throwable, String message) {
			this.err.format("%s [%s] ERROR %s - %s%n", getNow(), getThread(), this.name, message);
			if (throwable != null) {
				throwable.printStackTrace(this.err);
			}
		}

		@Override
		public synchronized void error(Throwable throwable, String message, Object... args) {
			this.err.format("%s [%s] ERROR %s - %s%n", getNow(), getThread(), this.name, format(message, args));
			if (throwable != null) {
				throwable.printStackTrace(this.err);
			}
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public boolean isErrorEnabled() {
			return true;
		}

		@Override
		public synchronized void error(String message) {
			this.err.format("%s [%s] ERROR %s - %s%n", getNow(), getThread(), this.name, message);
		}

		@Override
		public synchronized void warn(String message, Object... args) {
			this.out.format("%s [%s] WARN %s - %s%n", getNow(), getThread(), this.name, format(message, args));
		}

		@Override
		public synchronized void warn(Throwable throwable, String message) {
			this.out.format("%s [%s] WARN %s - %s%n", getNow(), getThread(), this.name, message);
			if (throwable != null) {
				throwable.printStackTrace(this.out);
			}
		}

		@Override
		public synchronized void warn(Throwable throwable, String message, Object... args) {
			this.out.format("%s [%s] WARN %s - %s%n", getNow(), getThread(), this.name, format(message, args));
			if (throwable != null) {
				throwable.printStackTrace(this.out);
			}
		}

		@Override
		public boolean isWarnEnabled() {
			return true;
		}

		@Override
		public synchronized void warn(String message) {
			this.out.format("%s [%s] WARN %s - %s%n", getNow(), getThread(), this.name, message);
		}

		@Override
		public synchronized void info(String message, Object... args) {
			this.out.format("%s [%s] INFO %s - %s%n", getNow(), getThread(), this.name, format(message, args));
		}

		@Override
		public synchronized void info(Throwable throwable, String message) {
			this.out.format("%s [%s] INFO %s - %s%n", getNow(), getThread(), this.name, message);
			if (throwable != null) {
				throwable.printStackTrace(this.out);
			}
		}

		@Override
		public synchronized void info(Throwable throwable, String message, Object... args) {
			this.out.format("%s [%s] INFO %s - %s%n", getNow(), getThread(), this.name, format(message, args));
			if (throwable != null) {
				throwable.printStackTrace(this.out);
			}
		}

		@Override
		public boolean isInfoEnabled() {
			return true;
		}

		@Override
		public synchronized void info(String message) {
			this.out.format("%s [%s] INFO %s - %s%n", getNow(), getThread(), this.name, message);
		}

		@Override
		public synchronized void debug(String message, Object... args) {
			this.out.format("%s [%s] DEBUG %s - %s%n", getNow(), getThread(), this.name, format(message, args));
		}

		@Override
		public synchronized void debug(Throwable throwable, String message) {
			this.out.format("%s [%s] DEBUG %s - %s%n", getNow(), getThread(), this.name, message);
			if (throwable != null) {
				throwable.printStackTrace(this.out);
			}
		}

		@Override
		public synchronized void debug(Throwable throwable, String message, Object... args) {
			this.out.format("%s [%s] DEBUG %s - %s%n", getNow(), getThread(), this.name, format(message, args));
			if (throwable != null) {
				throwable.printStackTrace(this.out);
			}
		}

		@Override
		public boolean isDebugEnabled() {
			return true;
		}

		@Override
		public synchronized void debug(String message) {
			this.out.format("%s [%s] DEBUG %s - %s%n", getNow(), getThread(), this.name, message);
		}

		@Override
		public synchronized void trace(String message, Object... args) {
			this.out.format("%s [%s] TRACE %s - %s%n", getNow(), getThread(), this.name, format(message, args));
		}

		@Override
		public synchronized void trace(Throwable throwable, String message) {
			this.out.format("%s [%s] TRACE %s - %s%n", getNow(), getThread(), this.name, message);
			if (throwable != null) {
				throwable.printStackTrace(this.out);
			}
		}

		@Override
		public synchronized void trace(Throwable throwable, String message, Object... args) {
			this.out.format("%s [%s] TRACE %s - %s%n", getNow(), getThread(), this.name, format(message, args));
			if (throwable != null) {
				throwable.printStackTrace(this.out);
			}
		}

		@Override
		public boolean isTraceEnabled() {
			return true;
		}

		@Override
		public synchronized void trace(String message) {
			this.out.format("%s [%s] TRACE %s - %s%n", getNow(), getThread(), this.name, message);
		}

		private String getThread() {
			return Thread.currentThread().getName();
		}

		private LocalDateTime getNow() {
			return LocalDateTime.now();
		}

	}

}
