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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CompositeConsumer}.
 *
 * @author Dmytro Nosan
 */
class CompositeConsumerTests {

	private final CompositeConsumer<String> compositeConsumer = new CompositeConsumer<>();

	private final CacheConsumer<String> cacheConsumer1 = new CacheConsumer<>(1);

	private final CacheConsumer<String> cacheConsumer2 = new CacheConsumer<>(1);

	@Test
	void testAdd() {
		assertThat(this.compositeConsumer.add(this.cacheConsumer1)).isTrue();
		assertThat(this.compositeConsumer.add(this.cacheConsumer1)).isFalse();
	}

	@Test
	void testRemove() {
		assertThat(this.compositeConsumer.add(this.cacheConsumer1)).isTrue();
		assertThat(this.compositeConsumer.remove(this.cacheConsumer1)).isTrue();
		assertThat(this.compositeConsumer.add(this.cacheConsumer1)).isTrue();
	}

	@Test
	void testAccept() {
		assertThat(this.compositeConsumer.add(this.cacheConsumer1)).isTrue();
		assertThat(this.compositeConsumer.add(this.cacheConsumer2)).isTrue();
		this.compositeConsumer.accept("text");
		assertThat(this.cacheConsumer1.get()).containsExactly("text");
		assertThat(this.cacheConsumer2.get()).containsExactly("text");
	}

}
