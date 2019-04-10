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
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Objects;

import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * {@link CqlScript} implementation for {@link URL}.
 *
 * @author Dmytro Nosan
 * @see CqlScript#urls(URL...)
 * @since 1.0.0
 */
public final class UrlCqlScript extends AbstractCqlResourceScript {

	private final URL url;

	/**
	 * Create a new {@link UrlCqlScript} based on a URL path.
	 *
	 * @param url a URL path
	 */
	public UrlCqlScript(URL url) {
		this(url, null);
	}

	/**
	 * Create a new {@link UrlCqlScript} based on a URL path.
	 *
	 * @param url a URL path
	 * @param encoding encoding the encoding to use for reading from the resource
	 */
	public UrlCqlScript(URL url, @Nullable Charset encoding) {
		super(encoding);
		this.url = Objects.requireNonNull(url, "URL must not be null");
	}

	@Override
	protected InputStream getInputStream() throws IOException {
		return this.url.openStream();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.url, getEncoding());
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		UrlCqlScript that = (UrlCqlScript) other;
		return Objects.equals(this.url, that.url) && Objects.equals(getEncoding(), that.getEncoding());
	}

	@Override
	public String toString() {
		return String.valueOf(this.url);
	}

}
