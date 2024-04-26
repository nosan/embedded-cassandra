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

package com.github.nosan.embedded.cassandra.cql;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.Objects;

import com.github.nosan.embedded.cassandra.commons.Resource;
import com.github.nosan.embedded.cassandra.commons.StreamUtils;

/**
 * {@link CqlScript} implementation for resources.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public class ResourceCqlScript extends AbstractCqlScript {

	private final Resource resource;

	private final Charset charset;

	/**
	 * Creates a new {@link ResourceCqlScript} with provided resource and default charset.
	 *
	 * @param resource the resource that contains CQL statements
	 */
	public ResourceCqlScript(Resource resource) {
		this(resource, Charset.defaultCharset());
	}

	/**
	 * Creates a new {@link ResourceCqlScript} with provided resource and charset.
	 *
	 * @param resource the resource that contains CQL statements
	 * @param charset the encoding to use of the resource
	 */
	public ResourceCqlScript(Resource resource, Charset charset) {
		Objects.requireNonNull(resource, "Resource must not be null");
		Objects.requireNonNull(charset, "Charset must not be null");
		this.charset = charset;
		this.resource = resource;
	}

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
