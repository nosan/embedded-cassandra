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

package com.github.nosan.embedded.cassandra.util;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.apiguardian.api.API;

import com.github.nosan.embedded.cassandra.lang.Nullable;

/**
 * {@link Supplier} to generate a thread name.
 *
 * @author Dmytro Nosan
 * @since 1.2.8
 */
@API(since = "1.2.8", status = API.Status.INTERNAL)
public final class ThreadNameSupplier implements Supplier<String> {

	private final AtomicLong threadCounter = new AtomicLong();

	@Nullable
	private final String prefix;

	/**
	 * Creates a {@link ThreadNameSupplier}.
	 *
	 * @param prefix a thread name prefix.
	 */
	public ThreadNameSupplier(@Nullable String prefix) {
		this.prefix = prefix;
	}

	@Override
	public String get() {
		StringBuilder name = new StringBuilder();
		if (StringUtils.hasText(this.prefix)) {
			name.append(this.prefix).append("-");
		}
		return name.append("thread-").append(this.threadCounter.incrementAndGet())
				.toString();
	}

}
