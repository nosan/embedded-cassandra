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

package com.github.nosan.embedded.cassandra;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

class ErrorCollector implements Consumer<String>, Closeable {

	private final List<String> errors = new CopyOnWriteArrayList<>();

	private final CassandraDatabase database;

	ErrorCollector(CassandraDatabase database) {
		this.database = database;
		database.getStdErr().attach(this);
	}

	@Override
	public void accept(String line) {
		this.errors.add(line);
	}

	@Override
	public void close() {
		this.database.getStdErr().detach(this);
	}

	List<String> getErrors() {
		return this.errors;
	}

}
