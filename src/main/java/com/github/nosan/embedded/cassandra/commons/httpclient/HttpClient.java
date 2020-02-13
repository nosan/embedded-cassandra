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

import java.io.IOException;
import java.io.InputStream;

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
	 * Send HTTP request.
	 *
	 * @param httpRequest the request to be sent
	 * @return HTTP response
	 * @throws IOException an I/O error occurs
	 */
	default HttpResponse send(HttpRequest httpRequest) throws IOException {
		return send(httpRequest, null);
	}

	/**
	 * Send HTTP request with a provided body.
	 *
	 * @param httpRequest the request to be sent
	 * @param bodySupplier HTTP body to be sent
	 * @return HTTP response
	 * @throws IOException an I/O error occurs
	 */
	HttpResponse send(HttpRequest httpRequest, IOSupplier<? extends InputStream> bodySupplier)
			throws IOException;

}
