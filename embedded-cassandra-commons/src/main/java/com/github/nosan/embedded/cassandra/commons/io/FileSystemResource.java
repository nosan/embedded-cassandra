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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.StringJoiner;

import com.github.nosan.embedded.cassandra.annotations.Nullable;

/**
 * {@link Resource} implementation for file system resources.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public class FileSystemResource implements Resource {

	private final Path path;

	/**
	 * Constructs a new {@link FileSystemResource} with the specified {@link Path}.
	 *
	 * @param path the {@link Path}
	 */
	public FileSystemResource(Path path) {
		Objects.requireNonNull(path, "'path' must not be null");
		this.path = path;
	}

	/**
	 * Constructs a new {@link FileSystemResource} with the specified {@link File}.
	 *
	 * @param path the {@link File}
	 */
	public FileSystemResource(File path) {
		Objects.requireNonNull(path, "'file' must not be null");
		this.path = path.toPath();
	}

	/**
	 * Constructs a new {@link FileSystemResource} with the specified path.
	 *
	 * @param path the absolute path
	 */
	public FileSystemResource(String path) {
		Objects.requireNonNull(path, "'path' must not be null");
		this.path = Paths.get(path);
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return Files.newInputStream(this.path);
	}

	@Override
	public URL toURL() throws IOException {
		return this.path.toUri().toURL();
	}

	@Override
	public URI toURI() {
		return this.path.toUri();
	}

	@Override
	public boolean exists() {
		return Files.exists(this.path);
	}

	@Override
	public Path toPath() {
		return this.path;
	}

	@Override
	public String getFileName() {
		return this.path.getFileName().toString();
	}

	@Override
	public File toFile() {
		return this.path.toFile();
	}

	@Override
	public byte[] getBytes() throws IOException {
		return Files.readAllBytes(this.path);
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		FileSystemResource that = (FileSystemResource) other;
		return this.path.equals(that.path);
	}

	@Override
	public int hashCode() {
		return this.path.hashCode();
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", FileSystemResource.class.getSimpleName() + "[", "]").add("path=" + this.path)
				.toString();
	}

}
