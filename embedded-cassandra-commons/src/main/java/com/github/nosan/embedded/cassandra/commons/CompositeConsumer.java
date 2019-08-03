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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Composite {@link Consumer}.
 *
 * @param <T> the type of the input to the operation
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public final class CompositeConsumer<T> implements Consumer<T> {

	private final List<Consumer<? super T>> consumers = new CopyOnWriteArrayList<>();

	/**
	 * Adds the specified element (if not present) to the end to this consumer.
	 *
	 * @param consumer consumer to be appended to this consumer
	 * @return {@code true} if specified element was added.
	 */
	public boolean add(Consumer<? super T> consumer) {
		Objects.requireNonNull(consumer, "'consumer' must not be null");
		if (!this.consumers.contains(consumer)) {
			return this.consumers.add(consumer);
		}
		return false;
	}

	/**
	 * Removes the specified element from this consumer.
	 *
	 * @param consumer consumer to be removed from this consumer
	 * @return {@code true} if specified element was removed.
	 */
	public boolean remove(Consumer<? super T> consumer) {
		Objects.requireNonNull(consumer, "'consumer' must not be null");
		return this.consumers.remove(consumer);
	}

	@Override
	public void accept(T element) {
		this.consumers.forEach(consumer -> consumer.accept(element));
	}

}
