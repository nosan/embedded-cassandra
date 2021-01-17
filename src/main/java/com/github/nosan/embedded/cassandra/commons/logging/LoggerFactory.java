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
		return new ConsoleLogger(name);
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

	private enum LogApi {
		SLF4J,
		CONSOLE
	}

}
