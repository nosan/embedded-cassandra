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
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

/**
 * {@link Resource} implementation for {@link URL} resources.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public class UrlResource implements Resource {

	private final URL url;

	/**
	 * Constructs a new {@link UrlResource} with the specified URL.
	 *
	 * @param url the {@link URL}
	 */
	public UrlResource(URL url) {
		Objects.requireNonNull(url, "URL must not be null");
		this.url = url;
	}

	@Override
	public Optional<String> getFileName() {
		if (isFile()) {
			try {
				return getFile().getFileName();
			}
			catch (IOException ex) {
				return Optional.empty();
			}
		}
		String file = this.url.getFile();
		if (!StringUtils.hasText(file)) {
			return Optional.empty();
		}
		int lastIndexOf = file.lastIndexOf('/');
		return (lastIndexOf != -1) ? Optional.of(file.substring(lastIndexOf + 1)) : Optional.of(file);
	}

	@Override
	public boolean exists() {
		if (isFile()) {
			try {
				return getFile().exists();
			}
			catch (IOException ex) {
				return false;
			}
		}
		return isReadable() || isWritable();
	}

	@Override
	public boolean isWritable() {
		try {
			if (isFile()) {
				return getFile().isWritable();
			}
			getOutputStream().close();
			return true;
		}
		catch (IOException ex) {
			return false;
		}
	}

	@Override
	public boolean isReadable() {
		try {
			if (isFile()) {
				return getFile().isReadable();
			}
			getInputStream().close();
			return true;
		}
		catch (IOException ex) {
			return false;
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
		if (isFile()) {
			return getFile().getInputStream();
		}
		URLConnection connection = this.url.openConnection();
		connection.setDoInput(true);
		return connection.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		if (isFile()) {
			return getFile().getOutputStream();
		}
		URLConnection connection = this.url.openConnection();
		connection.setDoOutput(true);
		return connection.getOutputStream();
	}

	@Override
	public URL toURL() {
		return this.url;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		UrlResource that = (UrlResource) other;
		return this.url.equals(that.url);
	}

	@Override
	public int hashCode() {
		return this.url.hashCode();
	}

	@Override
	public String toString() {
		return "UrlResource{" + "url=" + this.url + '}';
	}

	private boolean isFile() {
		return "file".equals(this.url.getProtocol());
	}

	private FileSystemResource getFile() throws IOException {
		return new FileSystemResource(Paths.get(toURI()));
	}

}
