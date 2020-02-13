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

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Console delegate {@link Logger} implementation.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public class ConsoleLogger implements Logger {

	private final String name;

	/**
	 * Creates {@link ConsoleLogger} with provided args.
	 *
	 * @param name the logger name
	 */
	public ConsoleLogger(String name) {
		Objects.requireNonNull(name, "Logger name must not be null");
		this.name = name;
	}

	@Override
	public synchronized void error(String message, Object... args) {
		System.err.format("%s [%s] ERROR %s - %s%n", getNow(), getThread(), this.name, format(message, args));
	}

	@Override
	public synchronized void error(Throwable throwable, String message) {
		System.err.format("%s [%s] ERROR %s - %s%n", getNow(), getThread(), this.name, message);
		if (throwable != null) {
			throwable.printStackTrace(System.err);
		}
	}

	@Override
	public synchronized void error(Throwable throwable, String message, Object... args) {
		System.err.format("%s [%s] ERROR %s - %s%n", getNow(), getThread(), this.name, format(message, args));
		if (throwable != null) {
			throwable.printStackTrace(System.err);
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
		System.err.format("%s [%s] ERROR %s - %s%n", getNow(), getThread(), this.name, message);
	}

	@Override
	public synchronized void warn(String message, Object... args) {
		System.out.format("%s [%s] WARN %s - %s%n", getNow(), getThread(), this.name, format(message, args));
	}

	@Override
	public synchronized void warn(Throwable throwable, String message) {
		System.out.format("%s [%s] WARN %s - %s%n", getNow(), getThread(), this.name, message);
		if (throwable != null) {
			throwable.printStackTrace(System.out);
		}
	}

	@Override
	public synchronized void warn(Throwable throwable, String message, Object... args) {
		System.out.format("%s [%s] WARN %s - %s%n", getNow(), getThread(), this.name, format(message, args));
		if (throwable != null) {
			throwable.printStackTrace(System.out);
		}
	}

	@Override
	public boolean isWarnEnabled() {
		return true;
	}

	@Override
	public synchronized void warn(String message) {
		System.out.format("%s [%s] WARN %s - %s%n", getNow(), getThread(), this.name, message);
	}

	@Override
	public synchronized void info(String message, Object... args) {
		System.out.format("%s [%s] INFO %s - %s%n", getNow(), getThread(), this.name, format(message, args));
	}

	@Override
	public synchronized void info(Throwable throwable, String message) {
		System.out.format("%s [%s] INFO %s - %s%n", getNow(), getThread(), this.name, message);
		if (throwable != null) {
			throwable.printStackTrace(System.out);
		}
	}

	@Override
	public synchronized void info(Throwable throwable, String message, Object... args) {
		System.out.format("%s [%s] INFO %s - %s%n", getNow(), getThread(), this.name, format(message, args));
		if (throwable != null) {
			throwable.printStackTrace(System.out);
		}
	}

	@Override
	public boolean isInfoEnabled() {
		return true;
	}

	@Override
	public synchronized void info(String message) {
		System.out.format("%s [%s] INFO %s - %s%n", getNow(), getThread(), this.name, message);
	}

	@Override
	public synchronized void debug(String message, Object... args) {
		System.out.format("%s [%s] DEBUG %s - %s%n", getNow(), getThread(), this.name, format(message, args));
	}

	@Override
	public synchronized void debug(Throwable throwable, String message) {
		System.out.format("%s [%s] DEBUG %s - %s%n", getNow(), getThread(), this.name, message);
		if (throwable != null) {
			throwable.printStackTrace(System.out);
		}
	}

	@Override
	public synchronized void debug(Throwable throwable, String message, Object... args) {
		System.out.format("%s [%s] DEBUG %s - %s%n", getNow(), getThread(), this.name, format(message, args));
		if (throwable != null) {
			throwable.printStackTrace(System.out);
		}
	}

	@Override
	public boolean isDebugEnabled() {
		return true;
	}

	@Override
	public synchronized void debug(String message) {
		System.out.format("%s [%s] DEBUG %s - %s%n", getNow(), getThread(), this.name, message);
	}

	@Override
	public synchronized void trace(String message, Object... args) {
		System.out.format("%s [%s] TRACE %s - %s%n", getNow(), getThread(), this.name, format(message, args));
	}

	@Override
	public synchronized void trace(Throwable throwable, String message) {
		System.out.format("%s [%s] TRACE %s - %s%n", getNow(), getThread(), this.name, message);
		if (throwable != null) {
			throwable.printStackTrace(System.out);
		}
	}

	@Override
	public synchronized void trace(Throwable throwable, String message, Object... args) {
		System.out.format("%s [%s] TRACE %s - %s%n", getNow(), getThread(), this.name, format(message, args));
		if (throwable != null) {
			throwable.printStackTrace(System.out);
		}
	}

	@Override
	public boolean isTraceEnabled() {
		return true;
	}

	@Override
	public synchronized void trace(String message) {
		System.out.format("%s [%s] TRACE %s - %s%n", getNow(), getThread(), this.name, message);
	}

	private String getThread() {
		return Thread.currentThread().getName();
	}

	private LocalDateTime getNow() {
		return LocalDateTime.now();
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
