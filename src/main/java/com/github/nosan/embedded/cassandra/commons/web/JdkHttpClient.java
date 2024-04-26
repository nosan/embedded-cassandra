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
 * {@link HttpURLConnection} based implementation of {@link HttpClient}.
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
	 * Creates {@link JdkHttpClient} with no timeouts and proxy.
	 */
	public JdkHttpClient() {
		this.readTimeout = null;
		this.connectTimeout = null;
		this.proxy = null;
	}

	/**
	 * Creates {@link JdkHttpClient} with no timeouts and provided proxy.
	 *
	 * @param proxy proxy to use
	 */
	public JdkHttpClient(Proxy proxy) {
		this.readTimeout = null;
		this.connectTimeout = null;
		this.proxy = proxy;
	}

	/**
	 * Creates {@link JdkHttpClient} with provided timeouts and no proxy.
	 *
	 * @param connectTimeout connection timeout
	 * @param readTimeout read timeout
	 */
	public JdkHttpClient(Duration connectTimeout, Duration readTimeout) {
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		this.proxy = null;
	}

	/**
	 * Creates {@link JdkHttpClient} with provided timeouts and  proxy.
	 *
	 * @param connectTimeout connection timeout
	 * @param readTimeout read timeout
	 * @param proxy proxy to use
	 */
	public JdkHttpClient(Duration connectTimeout, Duration readTimeout, Proxy proxy) {
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		this.proxy = proxy;
	}

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

	private static final class JdkHttpResponse implements HttpResponse {

		private final HttpURLConnection connection;

		private final HttpHeaders headers;

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
