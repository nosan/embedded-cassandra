/*
 * Copyright 2018-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra.test.support;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.sun.net.httpserver.HttpServer;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;

/**
 * {@link TestRule} to start/stop HttpServer.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
public final class WebServer extends ExternalResource implements Supplier<HttpServer> {

	@Nullable
	private HttpServer server;

	@Override
	protected void before() throws Throwable {
		start();
	}

	@Override
	protected void after() {
		stop();
	}

	/**
	 * Stops HTTP server.
	 */
	public void stop() {
		HttpServer server = this.server;
		if (server != null) {
			server.stop(0);
		}
	}

	/**
	 * Starts HTTP server.
	 *
	 * @throws IOException in case of I/O errors
	 */
	public void start() throws IOException {
		if (this.server == null) {
			HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
			server.setExecutor(Executors.newCachedThreadPool());
			server.start();
			this.server = server;
		}
	}

	/**
	 * Get HTTP server.
	 *
	 * @return a server
	 */
	@Nonnull
	@Override
	public HttpServer get() {
		return Objects.requireNonNull(this.server, "Http Server is not initialized.");
	}
}
