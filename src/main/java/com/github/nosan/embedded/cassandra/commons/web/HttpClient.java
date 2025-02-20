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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Objects;

import com.github.nosan.embedded.cassandra.commons.function.IOSupplier;

/**
 * A simple interface for sending HTTP requests.
 *
 * <p>This interface is designed to abstract the implementation of HTTP clients,
 * allowing for sending requests and receiving responses. It provides methods for sending a request either with or
 * without a body.</p>
 *
 * @author Dmytro Nosan
 * @see JdkHttpClient
 * @since 4.0.0
 */
public interface HttpClient {

	/**
	 * Sends an HTTP request.
	 *
	 * @param httpRequest The request to be sent
	 * @return The HTTP response
	 * @throws IOException If an I/O error occurs
	 */
	default HttpResponse send(HttpRequest httpRequest) throws IOException {
		return send(httpRequest, null);
	}

	/**
	 * Sends an HTTP request with an optional body.
	 *
	 * @param httpRequest The request to be sent
	 * @param bodySupplier A supplier for the request body, or {@code null} if no body is needed
	 * @return The HTTP response
	 * @throws IOException If an I/O error occurs
	 * @see BodySuppliers
	 */
	HttpResponse send(HttpRequest httpRequest, IOSupplier<? extends InputStream> bodySupplier)
			throws IOException;

	/**
	 * A utility class for creating different kinds of body suppliers.
	 *
	 * <p>This class contains convenient factory methods for generating
	 * {@link IOSupplier}s that provide the body content as an input stream.</p>
	 */
	final class BodySuppliers {

		private BodySuppliers() {
		}

		/**
		 * Creates a body supplier from the given string.
		 *
		 * @param body The string to be used as the body
		 * @return An {@link IOSupplier} that provides the string as an input stream
		 * @throws NullPointerException If the body is {@code null}
		 */
		public static IOSupplier<? extends InputStream> ofString(String body) {
			Objects.requireNonNull(body, "Body must not be null");
			return ofString(body, Charset.defaultCharset());
		}

		/**
		 * Creates a body supplier from the given string and charset.
		 *
		 * @param body The string to be used as the body
		 * @param charset The charset to encode the string
		 * @return An {@link IOSupplier} that provides the encoded string as an input stream
		 * @throws NullPointerException If the body or charset is {@code null}
		 */
		public static IOSupplier<? extends InputStream> ofString(String body, Charset charset) {
			Objects.requireNonNull(body, "Body must not be null");
			Objects.requireNonNull(charset, "Charset must not be null");
			return ofBytes(body.getBytes(charset));
		}

		/**
		 * Creates a body supplier from the given byte array.
		 *
		 * @param body The byte array to be used as the body
		 * @return An {@link IOSupplier} that provides the byte array as an input stream
		 * @throws NullPointerException If the body is {@code null}
		 */
		public static IOSupplier<? extends InputStream> ofBytes(byte[] body) {
			Objects.requireNonNull(body, "Body must not be null");
			return () -> new ByteArrayInputStream(body);
		}

	}

}
