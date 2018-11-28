/*
 * Copyright 2018-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * {@link CqlScript} implementation for {@link Path}.
 *
 * @author Dmytro Nosan
 * @see CqlScript#paths(Path...)
 * @since 1.0.0
 */
public final class PathCqlScript extends AbstractCqlResourceScript {

	@Nonnull
	private final Path location;

	/**
	 * Create a new {@link PathCqlScript} based on a Path.
	 *
	 * @param location a Path
	 */
	public PathCqlScript(@Nonnull Path location) {
		this(location, null);
	}

	/**
	 * Create a new {@link PathCqlScript} based on a Path.
	 *
	 * @param location a path
	 * @param encoding encoding the encoding to use for reading from the resource
	 */
	public PathCqlScript(@Nonnull Path location, @Nullable Charset encoding) {
		super(encoding);
		this.location = Objects.requireNonNull(location, "Location must not be null");
	}

	@Nonnull
	@Override
	protected InputStream getInputStream() throws IOException {
		return Files.newInputStream(this.location);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.location, getEncoding());
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
		return Objects.equals(this.location, that.location)
				&& Objects.equals(getEncoding(), that.getEncoding());
	}

	@Override
	@Nonnull
	public String toString() {
		return String.valueOf(this.location);
	}
}
