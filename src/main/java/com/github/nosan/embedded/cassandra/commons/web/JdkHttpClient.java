/*
 * Copyright 2020-2025 the original author or authors.
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

package com.github.nosan.embedded.cassandra.commons.web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.util.Objects;

import com.github.nosan.embedded.cassandra.commons.StreamUtils;
import com.github.nosan.embedded.cassandra.commons.StringUtils;
import com.github.nosan.embedded.cassandra.commons.function.IOSupplier;

/**
 * A {@link HttpClient} implementation based on {@link HttpURLConnection}.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public class JdkHttpClient implements HttpClient {

	private static final String COOKIE = "Cookie";

	private final Duration connectTimeout;

	private final Duration readTimeout;

	private final Proxy proxy;

	/**
	 * Creates a {@link JdkHttpClient} with no connection or read timeouts and no proxy.
	 */
	public JdkHttpClient() {
		this.readTimeout = null;
		this.connectTimeout = null;
		this.proxy = null;
	}

	/**
	 * Creates a {@link JdkHttpClient} with no connection or read timeouts and a specific proxy.
	 *
	 * @param proxy the proxy to use for HTTP requests (can be {@code null})
	 */
	public JdkHttpClient(Proxy proxy) {
		this.readTimeout = null;
		this.connectTimeout = null;
		this.proxy = proxy;
	}

	/**
	 * Creates a {@link JdkHttpClient} with specified connection and read timeouts, but no proxy.
	 *
	 * @param connectTimeout the duration to wait for establishing a connection (can be {@code null})
	 * @param readTimeout the duration to wait for reading data (can be {@code null})
	 */
	public JdkHttpClient(Duration connectTimeout, Duration readTimeout) {
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		this.proxy = null;
	}

	/**
	 * Creates a {@link JdkHttpClient} with specified connection and read timeouts and a proxy.
	 *
	 * @param connectTimeout the duration to wait for establishing a connection (can be {@code null})
	 * @param readTimeout the duration to wait for reading data (can be {@code null})
	 * @param proxy the proxy to use for HTTP requests (can be {@code null})
	 */
	public JdkHttpClient(Duration connectTimeout, Duration readTimeout, Proxy proxy) {
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		this.proxy = proxy;
	}

	/**
	 * Sends an HTTP request using the {@link HttpURLConnection} API.
	 *
	 * @param httpRequest the HTTP request to send (must not be {@code null})
	 * @param bodySupplier an optional supplier for the request body (can be {@code null})
	 * @return the HTTP response representing the server's reply (never {@code null})
	 * @throws IOException if an I/O error occurs while sending the request or reading the response
	 * @throws IllegalStateException if the connection cannot be established correctly
	 */
	@Override
	public final HttpResponse send(HttpRequest httpRequest, IOSupplier<? extends InputStream> bodySupplier)
			throws IOException {
		HttpURLConnection connection = open(httpRequest.getUri().toURL(), this.proxy);
		try {
			connect(connection, httpRequest, bodySupplier);
			return new JdkHttpResponse(connection);
		}
		catch (Exception ex) {
			try {
				connection.disconnect();
			}
			catch (Exception suppressed) {
				ex.addSuppressed(suppressed);
			}
			throw ex;
		}
	}

	/**
	 * Configures and opens the {@link HttpURLConnection}.
	 *
	 * @param url the target URL for the HTTP request (must not be {@code null})
	 * @param proxy the proxy to use for the connection (can be {@code null})
	 * @return the configured {@link HttpURLConnection}
	 * @throws IOException if an error occurs while opening the connection
	 * @throws IllegalStateException if the connection does not use {@link HttpURLConnection}
	 */
	protected HttpURLConnection open(URL url, Proxy proxy) throws IOException {
		URLConnection urlConnection = (proxy != null) ? url.openConnection(proxy) : url.openConnection();
		if (!(urlConnection instanceof HttpURLConnection)) {
			throw new IllegalStateException("HttpURLConnection required for [" + url + "] but got: " + urlConnection);
		}
		return (HttpURLConnection) urlConnection;
	}

	private void connect(HttpURLConnection connection, HttpRequest httpRequest,
			IOSupplier<? extends InputStream> bodySupplier) throws IOException {
		String method = httpRequest.getMethod();
		if (this.connectTimeout != null) {
			connection.setConnectTimeout(Math.toIntExact(this.connectTimeout.toMillis()));
		}
		if (this.readTimeout != null) {
			connection.setReadTimeout(Math.toIntExact(this.readTimeout.toMillis()));
		}
		connection.setDoInput(true);
		connection.setInstanceFollowRedirects("GET".equals(method));
		connection.setDoOutput("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method));
		connection.setRequestMethod(method);

		HttpHeaders headers = httpRequest.getHeaders();
		headers.forEach((name, values) -> {
			if (COOKIE.equalsIgnoreCase(name)) {
				connection.setRequestProperty(name, (values != null) ? String.join("; ", values) : "");
			}
			else if (values != null) {
				for (String value : values) {
					connection.addRequestProperty(name, Objects.toString(value, ""));
				}
			}
			else {
				connection.addRequestProperty(name, "");
			}
		});

		if (bodySupplier != null && connection.getDoOutput()) {
			try (InputStream is = bodySupplier.get(); OutputStream os = connection.getOutputStream()) {
				StreamUtils.copy(is, os);
			}
		}

		connection.connect();
	}

	/**
	 * Represents an HTTP response based on {@link HttpURLConnection}.
	 */
	private static final class JdkHttpResponse implements HttpResponse {

		private final HttpURLConnection connection;

		private final HttpHeaders headers;

		/**
		 * Creates a new {@code JdkHttpResponse} for the given {@link HttpURLConnection}.
		 *
		 * @param connection the HTTP connection (must not be {@code null})
		 */
		JdkHttpResponse(HttpURLConnection connection) {
			this.connection = connection;
			this.headers = HttpHeaders.readOnly(connection.getHeaderFields());
		}

		@Override
		public URI getUri() {
			try {
				return this.connection.getURL().toURI();
			}
			catch (URISyntaxException ex) {
				throw new IllegalStateException(
						"URL '" + this.connection.getURL() + "' is not formatted strictly according to RFC2396"
								+ " and cannot be converted to a URI", ex);
			}
		}

		@Override
		public int getStatus() {
			try {
				return this.connection.getResponseCode();
			}
			catch (FileNotFoundException ex) {
				return 404;
			}
			catch (IOException ex) {
				return 500;
			}
		}

		@Override
		public InputStream getInputStream() throws IOException {
			if (getStatus() >= 400) {
				InputStream errorStream = this.connection.getErrorStream();
				if (errorStream != null) {
					return errorStream;
				}
			}
			return this.connection.getInputStream();
		}

		@Override
		public HttpHeaders getHeaders() {
			return this.headers;
		}

		@Override
		public void close() {
			this.connection.disconnect();
		}

		@Override
		public String toString() {
			return "JdkHttpResponse{" + "uri='" + getUri() + "', status='" + getStatusMessage() + "'}";
		}

		private String getStatusMessage() {
			try {
				String message = this.connection.getResponseMessage();
				if (StringUtils.hasText(message)) {
					return getStatus() + " " + message;
				}
				return Integer.toString(getStatus());
			}
			catch (Exception ex) {
				return Integer.toString(getStatus());
			}
		}

	}

}
