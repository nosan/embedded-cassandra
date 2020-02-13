/*
 * Copyright 2020 the original author or authors.
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

package com.github.nosan.embedded.cassandra.commons.httpclient;

import java.net.URI;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents an HTTP request message.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public final class HttpRequest {

	private final URI uri;

	private final String method;

	private final HttpHeaders headers;

	/**
	 * Creates {@link HttpRequest} with provided uri and GET method.
	 *
	 * @param uri uri of the request
	 */
	public HttpRequest(URI uri) {
		this(uri, "GET", new HttpHeaders());
	}

	/**
	 * Creates {@link HttpRequest} with provided uri and method.
	 *
	 * @param uri uri of the request
	 * @param method HTTP method
	 */
	public HttpRequest(URI uri, String method) {
		this(uri, method, new HttpHeaders());
	}

	/**
	 * Creates {@link HttpRequest} with provided uri, method and headers.
	 *
	 * @param uri uri of the request
	 * @param method HTTP method
	 * @param headers HTTP headers
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
	 * Gets the URI of the request.
	 *
	 * @return the URI of the request
	 */
	public URI getUri() {
		return this.uri;
	}

	/**
	 * Gets the HTTP method of the request.
	 *
	 * @return the HTTP method
	 */
	public String getMethod() {
		return this.method;
	}

	/**
	 * Gets the headers of this request.
	 *
	 * @return a corresponding HttpHeaders object
	 */
	public HttpHeaders getHeaders() {
		return this.headers;
	}

	@Override
	public String toString() {
		return "HttpRequest{" + "uri=" + this.uri + ", method='" + this.method + '\'' + '}';
	}

}
