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

package com.github.nosan.embedded.cassandra.util;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.MDC;

import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * The {@link MDC} aware {@link ThreadFactory} to create a new {@link Thread}. <b>Only for internal
 * purposes.</b>
 *
 * @author Dmytro Nosan
 * @since 2.0.2
 */
public class MDCThreadFactory implements ThreadFactory {

	private final AtomicLong threadNumber = new AtomicLong();

	@Nullable
	private final String prefix;

	private final boolean daemon;

	/**
	 * Creates {@link MDCThreadFactory}.
	 */
	public MDCThreadFactory() {
		this(null, true);
	}

	/**
	 * Creates {@link MDCThreadFactory}.
	 *
	 * @param prefix the thread name prefix
	 */
	public MDCThreadFactory(@Nullable String prefix) {
		this(prefix, true);
	}

	/**
	 * Creates {@link MDCThreadFactory}.
	 *
	 * @param daemon marks thread as a daemon
	 */
	public MDCThreadFactory(boolean daemon) {
		this(null, daemon);
	}

	/**
	 * Creates {@link MDCThreadFactory}.
	 *
	 * @param prefix prefix for a thread name
	 * @param daemon marks thread as a daemon
	 */
	public MDCThreadFactory(@Nullable String prefix, boolean daemon) {
		this.prefix = prefix;
		this.daemon = daemon;
	}

	@Override
	public Thread newThread(Runnable runnable) {
		Objects.requireNonNull(runnable, "Runnable must not be null");
		Map<String, String> context = MDC.getCopyOfContextMap();
		Thread thread = new Thread(() -> {
			Optional.ofNullable(context).ifPresent(MDC::setContextMap);
			runnable.run();
		});
		thread.setName(getName());
		thread.setDaemon(this.daemon);
		return thread;
	}

	private String getName() {
		if (StringUtils.hasText(this.prefix)) {
			return String.format("%s-T-%d", this.prefix, this.threadNumber.incrementAndGet());
		}
		return String.format("T-%d", this.threadNumber.incrementAndGet());
	}

}
