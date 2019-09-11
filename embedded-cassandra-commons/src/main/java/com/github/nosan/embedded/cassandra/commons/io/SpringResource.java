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
import java.util.Objects;
import java.util.StringJoiner;

import com.github.nosan.embedded.cassandra.annotations.Nullable;

/**
 * {@link Resource} implementation for {@link org.springframework.core.io.Resource Spring Resource}.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public class SpringResource implements Resource {

	private final org.springframework.core.io.Resource resource;

	/**
	 * Constructs a new {@link SpringResource} with the specified {@link org.springframework.core.io.Resource
	 * Resource}.
	 *
	 * @param resource the Spring Resource
	 */
	public SpringResource(org.springframework.core.io.Resource resource) {
		this.resource = Objects.requireNonNull(resource, "'resource' must not be null");
	}

	/**
	 * Returns the underlying {@link org.springframework.core.io.Resource Spring Resource}.
	 *
	 * @return the resource
	 */
	public org.springframework.core.io.Resource getResource() {
		return this.resource;
	}

	@Override
	public String getFileName() {
		return this.resource.getFilename();
	}

	@Override
	public boolean exists() {
		return this.resource.exists();
	}

	@Override
	public URL toURL() throws IOException {
		return this.resource.getURL();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return this.resource.getInputStream();
	}

	@Override
	public URI toURI() throws IOException {
		return this.resource.getURI();
	}

	@Override
	public File toFile() throws IOException {
		return this.resource.getFile();
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		SpringResource that = (SpringResource) other;
		return this.resource.equals(that.resource);
	}

	@Override
	public int hashCode() {
		return this.resource.hashCode();
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", SpringResource.class.getSimpleName() + "[", "]").add(
				"resource=" + this.resource.getDescription()).toString();
	}

}
