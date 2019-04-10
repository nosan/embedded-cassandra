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

package com.github.nosan.embedded.cassandra.cql;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * {@link CqlScript} implementation for {@link Path}.
 *
 * @author Dmytro Nosan
 * @see CqlScript#paths(Path...)
 * @since 1.0.0
 */
public final class PathCqlScript extends AbstractCqlResourceScript {

	private final Path path;

	/**
	 * Create a new {@link PathCqlScript} based on a Path.
	 *
	 * @param path a Path
	 */
	public PathCqlScript(Path path) {
		this(path, null);
	}

	/**
	 * Create a new {@link PathCqlScript} based on a Path.
	 *
	 * @param path a path
	 * @param encoding encoding the encoding to use for reading from the resource
	 */
	public PathCqlScript(Path path, @Nullable Charset encoding) {
		super(encoding);
		this.path = Objects.requireNonNull(path, "Path must not be null");
	}

	@Override
	protected InputStream getInputStream() throws IOException {
		return Files.newInputStream(this.path);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.path, getEncoding());
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		PathCqlScript that = (PathCqlScript) other;
		return Objects.equals(this.path, that.path) && Objects.equals(getEncoding(), that.getEncoding());
	}

	@Override
	public String toString() {
		return String.valueOf(this.path);
	}

}
