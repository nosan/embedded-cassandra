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

package com.github.nosan.embedded.cassandra.commons;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

/**
 * Resource that abstracts from the actual type of underlying source.
 *
 * @author Dmytro Nosan
 * @see ClassPathResource
 * @see FileSystemResource
 * @see UrlResource
 * @since 4.0.0
 */
public interface Resource {

	/**
	 * Gets the file name of this resource.
	 *
	 * @return the name of this Resource.
	 */
	Optional<String> getFileName();

	/**
	 * Tests whether a resource exists.
	 *
	 * @return {@code true} if the resource exists
	 */
	boolean exists();

	/**
	 * Tests whether a resource is writable.
	 *
	 * @return {@code true} if the resource exists and is writable
	 */
	boolean isWritable();

	/**
	 * Tests whether a resource is readable.
	 *
	 * @return {@code true} if the resource exists and is readable
	 */
	boolean isReadable();

	/**
	 * Open an {@link InputStream} for the underlying resource.
	 *
	 * @return the input stream for the resource
	 * @throws IOException if resource does not exist or an I/O error occurs
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * Open an {@link OutputStream} for the underlying resource.
	 *
	 * @return the output stream for the resource
	 * @throws IOException if resource does not exist or an I/O error occurs
	 */
	OutputStream getOutputStream() throws IOException;

	/**
	 * Gets a {@link URL} to the underlying resource.
	 *
	 * @return the URL to the resource
	 * @throws IOException if resource does not exist, or {@code URL} cannot be built
	 */
	URL toURL() throws IOException;

	/**
	 * Gets a {@link URI} to the underlying resource.
	 *
	 * @return the URI to the resource
	 * @throws IOException if resource does not exist, or {@code URI} cannot be built
	 */
	default URI toURI() throws IOException {
		URL url = toURL();
		try {
			return url.toURI();
		}
		catch (URISyntaxException ex) {
			throw new IOException("URL '" + url + "' is not formatted strictly according to RFC2396"
					+ " and cannot be converted to a URI", ex);
		}
	}

}
