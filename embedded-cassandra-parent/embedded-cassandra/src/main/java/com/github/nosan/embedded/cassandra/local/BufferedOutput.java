/*
 * Copyright 2018-2019 the original author or authors.
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

package com.github.nosan.embedded.cassandra.local;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * {@link RunProcess.Output} to keep N lines.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
class BufferedOutput implements RunProcess.Output {

	private final ConcurrentLinkedDeque<String> lines = new ConcurrentLinkedDeque<>();

	private final int count;

	/**
	 * Creates a new {@link BufferedOutput}.
	 *
	 * @param count lines limit
	 */
	BufferedOutput(int count) {
		this.count = count;
	}

	@Override
	public void accept(@Nonnull String line) {
		if (this.lines.size() >= this.count) {
			this.lines.removeFirst();
		}
		this.lines.addLast(line);
	}

	/**
	 * Returns true if this buffer doesn't have lines.
	 *
	 * @return {@code true} if this buffer doesn't have lines, otherwise {@code false}
	 */
	public boolean isEmpty() {
		return this.lines.isEmpty();
	}

	@Override
	@Nonnull
	public String toString() {
		return this.lines.stream()
				.collect(Collectors.joining(System.lineSeparator()));
	}

	/**
	 * Return the lines.
	 *
	 * @return the lines
	 */
	@Nonnull
	Collection<String> lines() {
		return Collections.unmodifiableCollection(this.lines);
	}
}
