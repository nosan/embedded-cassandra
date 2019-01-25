/*
 * Copyright 2018-2019 the original author or authors.
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

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apiguardian.api.API;

/**
 * {@link CqlScript} implementation for {@link InputStream}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
@API(since = "1.0.0", status = API.Status.STABLE)
public final class InputStreamCqlScript extends AbstractCqlResourceScript {

	@Nonnull
	private final InputStream stream;

	/**
	 * Create a new {@link InputStreamCqlScript} based on a InputStream.
	 *
	 * @param stream a InputStream
	 */
	public InputStreamCqlScript(@Nonnull InputStream stream) {
		this(stream, null);
	}

	/**
	 * Create a new {@link InputStreamCqlScript} based on a InputStream.
	 *
	 * @param stream a InputStream
	 * @param encoding encoding the encoding to use for reading from the resource
	 */
	public InputStreamCqlScript(@Nonnull InputStream stream, @Nullable Charset encoding) {
		super(encoding);
		this.stream = Objects.requireNonNull(stream, "Location must not be null");
	}

	@Nonnull
	@Override
	protected InputStream getInputStream() {
		return this.stream;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.stream, getEncoding());
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		InputStreamCqlScript that = (InputStreamCqlScript) other;
		return Objects.equals(this.stream, that.stream)
				&& Objects.equals(getEncoding(), that.getEncoding());
	}

	@Override
	@Nonnull
	public String toString() {
		return "InputStream CQL Statements";
	}
}
