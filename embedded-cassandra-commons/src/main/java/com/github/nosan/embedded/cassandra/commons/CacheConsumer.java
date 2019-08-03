/*
 * Copyright 2018-2019 the original author or authors.
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

package com.github.nosan.embedded.cassandra.commons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Cache {@link Consumer}. This consumer caches the specified number of elements. Use {@link #get()} to get cached
 * result.
 *
 * @param <T> the type of the input to the operation
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public final class CacheConsumer<T> implements Consumer<T>, Supplier<List<T>> {

	private final Deque<T> elements;

	private final long count;

	/**
	 * Constructs a new {@link CacheConsumer} with the specified count.
	 *
	 * @param count how many elements should be cached.
	 */
	public CacheConsumer(long count) {
		if (count <= 0) {
			throw new IllegalArgumentException(String.format("Count '%d' must be positive", count));
		}
		this.count = count;
		this.elements = new ConcurrentLinkedDeque<>();
	}

	@Override
	public synchronized void accept(T element) {
		while (this.elements.size() >= this.count) {
			this.elements.removeFirst();
		}
		this.elements.addLast(element);
	}

	@Override
	public List<T> get() {
		return Collections.unmodifiableList(new ArrayList<>(this.elements));
	}

}
