/*
 * Copyright 2020-2024 the original author or authors.
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

package com.github.nosan.embedded.cassandra;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

interface ProcessWrapper {

	String getName();

	long getPid();

	ProcessWrapper destroy();

	ProcessWrapper destroyForcibly();

	boolean isAlive();

	int waitFor();

	boolean waitFor(int timeout, TimeUnit unit);

	CompletableFuture<? extends ProcessWrapper> onExit();

	Output getStdOut();

	Output getStdErr();

	interface Output {

		void attach(Consumer<? super String> consumer);

		void detach(Consumer<? super String> consumer);

	}

}
