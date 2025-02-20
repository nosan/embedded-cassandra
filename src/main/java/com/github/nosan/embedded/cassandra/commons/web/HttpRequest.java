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

import java.net.URI;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents an HTTP request message.
 *
 * <p>This class encapsulates the important components of an HTTP request,
 * including the URI, HTTP method, and headers, to provide a structured way to create and use HTTP requests.</p>
 *
 * <p>By default, if not specified, the HTTP method is set to {@code GET}
 * and no headers are included.</p>
 *
 * <p>The {@link HttpHeaders} object is used to manage request headers,
 * supporting case-insensitive header names and convenient methods for adding or retrieving header values.</p>
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public class HttpRequest {

	private final URI uri;

	private final String method;

	private final HttpHeaders headers;

	/**
	 * Creates a new {@link HttpRequest} with the specified URI and the default HTTP {@code GET} method.
	 *
	 * @param uri the URI of the request
	 * @throws NullPointerException if {@code uri} is {@code null}
	 */
	public HttpRequest(URI uri) {
		this(uri, "GET", new HttpHeaders());
	}

	/**
	 * Creates a new {@link HttpRequest} with the specified URI and HTTP method.
	 *
	 * @param uri the URI of the request
	 * @param method the HTTP method of the request (e.g., {@code GET}, {@code POST})
	 * @throws NullPointerException if {@code uri} or {@code method} is {@code null}
	 */
	public HttpRequest(URI uri, String method) {
		this(uri, method, new HttpHeaders());
	}

	/**
	 * Creates a new {@link HttpRequest} with the specified URI, HTTP method, and headers.
	 *
	 * @param uri the URI of the request
	 * @param method the HTTP method of the request (e.g., {@code GET}, {@code POST})
	 * @param headers the HTTP headers of the request
	 * @throws NullPointerException if {@code uri}, {@code method}, or {@code headers} is {@code null}
	 */
	public HttpRequest(URI uri, String method, HttpHeaders headers) {
		Objects.requireNonNull(uri, "URI must not be null");
		Objects.requireNonNull(method, "HTTP Method must not be null");
		Objects.requireNonNull(headers, "HTTP Headers must not be null");
		this.uri = uri;
		this.method = method.toUpperCase(Locale.ENGLISH);
		this.headers = headers;
	}

	/**
	 * Returns the URI of the request.
	 *
	 * @return the URI of the request
	 */
	public URI getUri() {
		return this.uri;
	}

	/**
	 * Returns the HTTP method of the request.
	 *
	 * <p>The method is always returned in uppercase to ensure compatibility
	 * with standard HTTP conventions.</p>
	 *
	 * @return the HTTP method of the request
	 */
	public String getMethod() {
		return this.method;
	}

	/**
	 * Returns the HTTP headers associated with this request.
	 *
	 * <p>The headers are represented as a {@link HttpHeaders} object, allowing
	 * for easy access and modification of individual headers.</p>
	 *
	 * @return the HTTP headers of the request
	 */
	public HttpHeaders getHeaders() {
		return this.headers;
	}

	/**
	 * Returns a string representation of the HTTP request.
	 *
	 * <p>The string representation includes the URI and HTTP method of the
	 * request. Headers are not included in this output.</p>
	 *
	 * @return a string representation of the HTTP request
	 */
	@Override
	public String toString() {
		return "HttpRequest{" + "uri=" + this.uri + ", method='" + this.method + '\'' + '}';
	}

}
