/*
 * Copyright 2020-2025 the original author or authors.
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
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.Objects;

import com.github.nosan.embedded.cassandra.commons.Resource;
import com.github.nosan.embedded.cassandra.commons.StreamUtils;

/**
 * An implementation of {@link CqlScript} that reads CQL statements from a {@link Resource}.
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * ResourceCqlScript script = new ResourceCqlScript(new ClassPathResource("schema.cql"), StandardCharsets.UTF_8);
 * List<String> statements = script.getStatements();
 * statements.forEach(statement -> System.out.println(statement));
 * }</pre>
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public class ResourceCqlScript extends AbstractCqlScript {

	private final Resource resource;

	private final Charset charset;

	/**
	 * Creates a new {@link ResourceCqlScript} with the given {@link Resource} and the default platform charset.
	 *
	 * @param resource the resource containing CQL statements (must not be {@code null})
	 * @throws NullPointerException if {@code resource} is {@code null}
	 */
	public ResourceCqlScript(Resource resource) {
		this(resource, Charset.defaultCharset());
	}

	/**
	 * Creates a new {@link ResourceCqlScript} with the given {@link Resource} and a specified {@link Charset}.
	 *
	 * @param resource the resource containing CQL statements (must not be {@code null})
	 * @param charset the character encoding to use when reading the resource (must not be {@code null})
	 * @throws NullPointerException if {@code resource} or {@code charset} is {@code null}
	 */
	public ResourceCqlScript(Resource resource, Charset charset) {
		Objects.requireNonNull(resource, "Resource must not be null");
		Objects.requireNonNull(charset, "Charset must not be null");
		this.charset = charset;
		this.resource = resource;
	}

	/**
	 * Reads and returns the entire content of the resource as a single CQL script.
	 *
	 * <p>The resource's input stream is converted to a string using the specified character encoding,
	 * and any {@link IOException} encountered will be wrapped in an {@link UncheckedIOException}.</p>
	 *
	 * @return the content of the resource as a string
	 * @throws UncheckedIOException if the resource cannot be read or a stream cannot be opened
	 */
	@Override
	protected String getScript() {
		try (InputStream is = this.resource.getInputStream()) {
			return StreamUtils.toString(is, this.charset);
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Could not open a stream for " + this.resource, ex);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}

		ResourceCqlScript that = (ResourceCqlScript) other;

		if (!this.charset.equals(that.charset)) {
			return false;
		}
		return this.resource.equals(that.resource);
	}

	@Override
	public int hashCode() {
		int result = this.charset.hashCode();
		result = 31 * result + this.resource.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "ResourceCqlScript{" + "resource=" + this.resource + ", charset=" + this.charset + '}';
	}

}
