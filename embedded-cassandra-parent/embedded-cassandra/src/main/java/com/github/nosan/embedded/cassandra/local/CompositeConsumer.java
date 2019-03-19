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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Composite {@link Consumer} implementation that iterates over a given list of {@link Consumer} instances.
 *
 * @param <T> the type
 * @author Dmytro Nosan
 * @since 1.4.2
 */
class CompositeConsumer<T> implements Consumer<T> {

	private final List<Consumer<? super T>> consumers = new CopyOnWriteArrayList<>();

	@Override
	public void accept(T source) {
		this.consumers.forEach(consumer -> consumer.accept(source));
	}

	/**
	 * Adds a consumer.
	 *
	 * @param consumer the consumer
	 */
	void add(Consumer<? super T> consumer) {
		this.consumers.add(consumer);
	}

	/**
	 * Removes a consumer.
	 *
	 * @param consumer the consumer
	 */
	void remove(Consumer<? super T> consumer) {
		this.consumers.remove(consumer);
	}

}
