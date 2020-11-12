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

package com.github.nosan.embedded.cassandra.commons.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Objects;

import com.github.nosan.embedded.cassandra.commons.function.IOSupplier;

/**
 * Simple interface to send HTTP requests.
 *
 * @author Dmytro Nosan
 * @see JdkHttpClient
 * @since 4.0.0
 */
public interface HttpClient {

	/**
	 * Sends HTTP request.
	 *
	 * @param httpRequest the request to be sent
	 * @return HTTP response
	 * @throws IOException an I/O error occurs
	 */
	default HttpResponse send(HttpRequest httpRequest) throws IOException {
		return send(httpRequest, null);
	}

	/**
	 * Sends HTTP request with a provided body.
	 *
	 * @param httpRequest the request to be sent
	 * @param bodySupplier HTTP body to be sent
	 * @return HTTP response
	 * @throws IOException an I/O error occurs
	 * @see BodySuppliers
	 */
	HttpResponse send(HttpRequest httpRequest, IOSupplier<? extends InputStream> bodySupplier)
			throws IOException;

	/**
	 * Utility class for creating different body suppliers.
	 */
	final class BodySuppliers {

		private BodySuppliers() {
		}

		/**
		 * Creates a body supplier of the provided string.
		 *
		 * @param body the string body
		 * @return the supplier
		 */
		public static IOSupplier<? extends InputStream> ofString(String body) {
			Objects.requireNonNull(body, "Body must not be null");
			return ofString(body, Charset.defaultCharset());
		}

		/**
		 * Creates a body supplier of the provided string and charset.
		 *
		 * @param body the string body
		 * @param charset the charset to use
		 * @return the supplier
		 */
		public static IOSupplier<? extends InputStream> ofString(String body, Charset charset) {
			Objects.requireNonNull(body, "Body must not be null");
			Objects.requireNonNull(charset, "Charset must not be null");
			return ofBytes(body.getBytes(charset));
		}

		/**
		 * Creates a body supplier of the provided byte array.
		 *
		 * @param body the byte array
		 * @return the supplier
		 */
		public static IOSupplier<? extends InputStream> ofBytes(byte[] body) {
			Objects.requireNonNull(body, "Body must not be null");
			return () -> new ByteArrayInputStream(body);
		}

	}

}
