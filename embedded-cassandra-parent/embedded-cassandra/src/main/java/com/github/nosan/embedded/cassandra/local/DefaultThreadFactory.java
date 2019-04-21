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

import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * Default {@link ThreadFactory factory} to create {@link Thread}.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
public final class DefaultThreadFactory implements ThreadFactory {

	private final AtomicLong threadNumber = new AtomicLong();

	private final String prefix;

	/**
	 * Creates {@link ThreadFactory}.
	 *
	 * @param prefix the thread name prefix
	 */
	public DefaultThreadFactory(String prefix) {
		if (!StringUtils.hasText(prefix)) {
			throw new IllegalArgumentException("Prefix must not be empty or null");
		}
		this.prefix = prefix;
	}

	@Override
	public Thread newThread(Runnable runnable) {
		Map<String, String> context = MDC.getCopyOfContextMap();
		Thread thread = new Thread(() -> {
			Optional.ofNullable(context).ifPresent(MDC::setContextMap);
			runnable.run();
		}, String.format("%s-T-%d", this.prefix, this.threadNumber.incrementAndGet()));
		thread.setDaemon(true);
		return thread;
	}

}
