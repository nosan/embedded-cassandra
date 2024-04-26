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

package com.github.nosan.embedded.cassandra.commons;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * {@link Resource} implementation for file system resources.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public class FileSystemResource implements Resource {

	private final Path file;

	/**
	 * Constructs a new {@link FileSystemResource} with the specified {@link Path}.
	 *
	 * @param file the {@link Path}
	 */
	public FileSystemResource(Path file) {
		Objects.requireNonNull(file, "Path must not be null");
		this.file = file;
	}

	/**
	 * Constructs a new {@link FileSystemResource} with the specified {@link File}.
	 *
	 * @param file the {@link File}
	 */
	public FileSystemResource(File file) {
		Objects.requireNonNull(file, "File must not be null");
		this.file = file.toPath();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return Files.newInputStream(this.file);
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return Files.newOutputStream(this.file);
	}

	@Override
	public URL toURL() throws MalformedURLException {
		return this.file.toUri().toURL();
	}

	@Override
	public URI toURI() {
		return this.file.toUri();
	}

	@Override
	public Optional<String> getFileName() {
		return Optional.ofNullable(this.file.getFileName()).map(Object::toString).filter(StringUtils::hasText);
	}

	@Override
	public boolean exists() {
		return Files.exists(this.file);
	}

	@Override
	public boolean isWritable() {
		return Files.isWritable(this.file) && !Files.isDirectory(this.file);
	}

	@Override
	public boolean isReadable() {
		return Files.isReadable(this.file) && !Files.isDirectory(this.file);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		FileSystemResource that = (FileSystemResource) other;
		return this.file.equals(that.file);
	}

	/**
	 * Gets the file.
	 *
	 * @return the file
	 */
	public Path getFile() {
		return this.file;
	}

	@Override
	public int hashCode() {
		return this.file.hashCode();
	}

	@Override
	public String toString() {
		return "FileSystemResource{" + "file=" + this.file + '}';
	}

}
