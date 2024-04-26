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
	 * Gets the URI of the response.
	 *
	 * @return the URI of the response
	 */
	URI getUri();

	/**
	 * Gets the HTTP response status.
	 *
	 * @return the status
	 */
	int getStatus();

	/**
	 * Gets the headers of this response.
	 *
	 * @return a corresponding HttpHeaders object
	 */
	HttpHeaders getHeaders();

	/**
	 * The HTTP response body.
	 *
	 * @return the body
	 * @throws IOException an I/O error occurs
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * Close the current response.
	 *
	 * @throws IOException an I/O error occurs
	 */
	@Override
	void close() throws IOException;

}
