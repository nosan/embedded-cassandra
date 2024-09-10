/*
 * Copyright 2020-2024 the original author or authors.
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

package com.github.nosan.embedded.cassandra;

import java.io.Closeable;
import java.util.function.Consumer;
import java.util.regex.Pattern;

class StartupParser implements Consumer<String>, Closeable {

	private static final Pattern STARTUP_COMPLETE = Pattern.compile("Startup complete$", Pattern.CASE_INSENSITIVE);

	private final CassandraDatabase database;

	private volatile boolean complete;

	StartupParser(CassandraDatabase database) {
		this.database = database;
		database.getStdOut().attach(this);
	}

	@Override
	public void accept(String line) {
		if (STARTUP_COMPLETE.matcher(line).find()) {
			this.complete = true;
		}
	}

	@Override
	public void close() {
		this.database.getStdOut().detach(this);
	}

	boolean isComplete() {
		return this.complete;
	}

}
