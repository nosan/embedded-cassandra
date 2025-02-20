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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Represents an HTTP response message.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public interface HttpResponse extends AutoCloseable {

	/**
	 * Returns the URI of the HTTP request that resulted in this response.
	 *
	 * @return the URI of the associated HTTP request
	 */
	URI getUri();

	/**
	 * Returns the HTTP status code of the response.
	 *
	 * @return the HTTP status code
	 */
	int getStatus();

	/**
	 * Returns the headers associated with this HTTP response.
	 *
	 * @return a {@link HttpHeaders} object representing the response headers
	 */
	HttpHeaders getHeaders();

	/**
	 * Returns the body of the HTTP response as an {@link InputStream}.
	 *
	 * <p>The response body is available for reading as raw byte content using this
	 * stream. It is important to ensure this stream is fully read or closed to properly release resources.</p>
	 *
	 * @return an {@link InputStream} containing the response body
	 * @throws IOException if an I/O error occurs while retrieving the body
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * Closes the response and releases any underlying resources.
	 *
	 * <p>It is recommended to use a try-with-resources statement when working with
	 * {@code HttpResponse} to automatically handle resource cleanup:</p>
	 *
	 * <pre>{@code
	 * try (HttpResponse response = client.send(request)) {
	 *     InputStream body = response.getInputStream();
	 *     // Process the response body
	 * }
	 * }</pre>
	 *
	 * @throws IOException if an I/O error occurs while closing the response
	 */
	@Override
	void close() throws IOException;

}
