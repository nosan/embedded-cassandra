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

package com.github.nosan.embedded.cassandra.local;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.MDC;

/**
 * Default {@link ThreadFactory}.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
class DefaultThreadFactory implements ThreadFactory {

	private final AtomicLong threadNumber = new AtomicLong();

	private final long id;

	private final String prefix;

	DefaultThreadFactory(String prefix, long id) {
		this.prefix = prefix;
		this.id = id;
	}

	@Override
	public Thread newThread(Runnable runnable) {
		Map<String, String> context = MDC.getCopyOfContextMap();
		Thread thread = new Thread(() -> {
			Optional.ofNullable(context).ifPresent(MDC::setContextMap);
			runnable.run();
		}, String.format("%s-%d-th-%d", this.prefix, this.id, this.threadNumber.incrementAndGet()));
		thread.setDaemon(true);
		return thread;
	}

}
