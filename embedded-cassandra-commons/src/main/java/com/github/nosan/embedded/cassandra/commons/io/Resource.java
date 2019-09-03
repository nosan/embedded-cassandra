/*
 * Copyright 2018-2019 the original author or authors.
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

package com.github.nosan.embedded.cassandra.commons.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.nosan.embedded.cassandra.annotations.Nullable;

/**
 * Resource that abstracts from the actual type of underlying source.
 *
 * @author Dmytro Nosan
 * @see ClassPathResource
 * @see UrlResource
 * @see FileSystemResource
 * @since 3.0.0
 */
public interface Resource {

	/**
	 * Gets the file name of this Resource.
	 *
	 * @return the file name of this Resource, or {@code null}.
	 */
	@Nullable
	String getFileName();

	/**
	 * Returns {@code true} if the underlying resource exists.
	 *
	 * @return {@code true} if resource exists, otherwise {@code false}
	 */
	boolean exists();

	/**
	 * Returns an {@link URL} to the underlying resource.
	 *
	 * @return the URL to the resource
	 * @throws IOException if resource does not exist, or {@code URL} cannot be built
	 */
	URL toURL() throws IOException;

	/**
	 * Returns an {@link InputStream} for the underlying resource.
	 *
	 * @return the input stream for the resource
	 * @throws IOException if resource does not exist
	 */
	default InputStream getInputStream() throws IOException {
		return toURL().openStream();
	}

	/**
	 * Returns an {@link URI} to the underlying resource.
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
			throw new IOException("URL '" + url + "' is not formatted strictly according to RFC2396", ex);
		}
	}

	/**
	 * Returns a {@link Path} to the underlying resource.
	 *
	 * @return the Path to the resource
	 * @throws IOException if resource does not exist, or {@code Path} cannot be built
	 * @throws FileSystemNotFoundException The file system does not exist and cannot be created automatically.
	 */
	default Path toPath() throws IOException, FileSystemNotFoundException {
		return Paths.get(toURI());
	}

	/**
	 * Returns a {@link File} to the underlying resource.
	 *
	 * @return the File to the resource
	 * @throws IOException if resource does not exist, or {@code File} cannot be built
	 */
	default File toFile() throws IOException {
		return new File(toURI());
	}

	/**
	 * Reads all the bytes from a resource. It is not intended for reading large files.
	 *
	 * @return a byte array containing the bytes read from the resource
	 * @throws IOException if resource does not exist
	 */
	default byte[] getBytes() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (InputStream is = getInputStream()) {
			byte[] buffer = new byte[4096];
			int read;
			while ((read = is.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			return out.toByteArray();
		}
	}

}
