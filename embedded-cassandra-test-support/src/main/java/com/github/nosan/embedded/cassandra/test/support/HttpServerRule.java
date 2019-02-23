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

package com.github.nosan.embedded.cassandra.test.support;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.sun.net.httpserver.HttpServer;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;

/**
 * {@link TestRule} to start/stop HttpServer.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
public final class HttpServerRule extends ExternalResource implements Supplier<HttpServer> {

	@Nonnull
	private final HttpServer httpServer;

	public HttpServerRule() {
		try {
			this.httpServer = HttpServer.create();
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	@Override
	protected void before() throws IOException {
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
		this.httpServer.stop(0);
	}

	/**
	 * Starts HTTP server.
	 *
	 * @throws IOException in case of I/O errors
	 */
	public void start() throws IOException {
		this.httpServer.setExecutor(Executors.newCachedThreadPool());
		this.httpServer.bind(new InetSocketAddress("localhost", 0), 0);
		this.httpServer.start();
	}

	/**
	 * Get HTTP server.
	 *
	 * @return a server
	 */
	@Nonnull
	@Override
	public HttpServer get() {
		return this.httpServer;
	}
}
