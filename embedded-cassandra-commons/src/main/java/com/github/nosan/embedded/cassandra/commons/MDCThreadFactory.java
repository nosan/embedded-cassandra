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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;

import org.slf4j.MDC;

/**
 * Factory that can be used to create and configure a {@code MDC} aware {@link Thread}. Sets the parent MDC {@link
 * MDC#getCopyOfContextMap() context} to the newly created {@link Thread}.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public class MDCThreadFactory implements ThreadFactory {

	@Override
	public Thread newThread(Runnable runnable) {
		Objects.requireNonNull(runnable, "'runnable' must not be null");
		Map<String, String> context = MDC.getCopyOfContextMap();
		return new Thread(() -> {
			if (context != null) {
				MDC.setContextMap(context);
			}
			runnable.run();
		});
	}

}
