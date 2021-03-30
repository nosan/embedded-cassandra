/*
 * Copyright 2020-2021 the original author or authors.
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

import java.text.MessageFormat;
import java.util.Objects;

import org.slf4j.LoggerFactory;

/**
 * Slf4j delegate {@link Logger} implementation.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public class Slf4jLogger implements Logger {

	private final org.slf4j.Logger logger;

	/**
	 * Creates {@link Slf4jLogger}.
	 *
	 * @param name the slf4j logger name to use
	 * @since 4.0.2
	 */
	public Slf4jLogger(String name) {
		this(LoggerFactory.getLogger(name));
	}

	/**
	 * Creates {@link Slf4jLogger}.
	 *
	 * @param logger the slf4j logger to use
	 */
	public Slf4jLogger(org.slf4j.Logger logger) {
		Objects.requireNonNull(logger, "Logger must not be null");
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

	private static String format(String message, Object... args) {
		if (message == null) {
			return null;
		}
		if (args == null || args.length == 0) {
			return message;
		}
		return MessageFormat.format(message, args);
	}

}
