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
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.StringJoiner;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.commons.io.Resource;

/**
 * {@link CqlScript} for {@link Resource Resources}.
 *
 * @author Dmytro Nosan
 */
final class ResourceCqlScript implements CqlScript {

	private final Charset charset;

	private final Resource resource;

	/**
	 * Constructs a new {@link ResourceCqlScript} with the specified {@link Resource} and {@link Charset}.
	 *
	 * @param resource the resource
	 * @param charset the charset
	 */
	ResourceCqlScript(Charset charset, Resource resource) {
		this.charset = charset;
		this.resource = resource;
	}

	@Override
	public List<String> getStatements() {
		return new Parser(getScript(this.resource, this.charset)).getStatements();
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", ResourceCqlScript.class.getSimpleName() + "[", "]").add("charset=" + this.charset)
				.add("resource=" + this.resource).toString();
	}

	@Override
	public boolean equals(@Nullable Object other) {
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

	private static String getScript(Resource resource, Charset charset) {
		try {
			return new String(resource.getBytes(), charset);
		}
		catch (IOException ex) {
			throw new UncheckedIOException(String.format("Cannot open a stream for '%s'", resource), ex);
		}
	}

}
