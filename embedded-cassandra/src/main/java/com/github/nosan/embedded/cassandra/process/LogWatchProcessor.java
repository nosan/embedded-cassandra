/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nosan.embedded.cassandra.process;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import de.flapdoodle.embed.process.io.IStreamProcessor;
import de.flapdoodle.embed.process.io.LogWatchStreamProcessor;

import com.github.nosan.embedded.cassandra.Config;

/**
 * Utility class for watching cassandra log's. Class has already had success/error messages depends on {@code Config}.
 *
 * @author Dmytro Nosan
 */
class LogWatchProcessor extends LogWatchStreamProcessor {

	private final IStreamProcessor delegate;

	LogWatchProcessor(Config config, IStreamProcessor delegate) {
		super(getSuccess(config), getFailures(), delegate);
		this.delegate = delegate;
	}

	@Override
	public void process(String block) {
		if (isInitWithSuccess() || getFailureFound() != null) {
			this.delegate.process(block);
		}
		else {
			super.process(block);
		}
	}

	@Override
	public void onProcessed() {
		super.onProcessed();
		this.delegate.onProcessed();
	}

	private static String getSuccess(Config config) {

		if (config.isStartNativeTransport()) {
			return "Starting listening for CQL";
		}

		if (config.isStartRpc()) {
			return "Listening for thrift clients";
		}

		return "Starting Messaging Service";
	}

	private static Set<String> getFailures() {
		return new LinkedHashSet<>(Arrays.asList("encountered during startup",
				"Missing required", "Address already in use", "Port already in use",
				"ConfigurationException", "syntax error near unexpected",
				"Error occurred during initialization",
				"Cassandra 3.0 and later require Java", "You must set the CASSANDRA_CONF and CLASSPATH vars"));
	}
}
