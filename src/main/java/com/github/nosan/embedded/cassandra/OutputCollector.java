/*
 * Copyright 2020-2025 the original author or authors.
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
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

/**
 * A class that collects and manages the output of a Cassandra database process. The collected output consists of lines
 * from the database's standard output stream, with a maximum capacity of 30 lines.
 *
 * @author Dmytro Nosan
 */
class OutputCollector implements Consumer<String>, Closeable {

	private final Deque<String> output = new ConcurrentLinkedDeque<>();

	private final CassandraDatabase database;

	OutputCollector(CassandraDatabase database) {
		this.database = database;
		database.getStdOut().attach(this);
	}

	@Override
	public void accept(String line) {
		while (this.output.size() >= 30) {
			this.output.removeFirst();
		}
		this.output.addLast(line);
	}

	@Override
	public void close() {
		this.database.getStdOut().detach(this);
	}

	Deque<String> getOutput() {
		return this.output;
	}

}
