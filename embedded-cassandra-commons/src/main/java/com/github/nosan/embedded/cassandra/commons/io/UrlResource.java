/*
 * Copyright 2018-2020 the original author or authors.
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.StringJoiner;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.commons.util.StringUtils;

/**
 * {@link Resource} implementation for {@link URL} resources.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public class UrlResource implements Resource {

	private final URL url;

	/**
	 * Constructs a new {@link UrlResource} with the specified URL.
	 *
	 * @param url the {@link URL}
	 */
	public UrlResource(URL url) {
		Objects.requireNonNull(url, "'url' must not be null");
		this.url = url;
	}

	@Override
	public URL toURL() {
		return this.url;
	}

	@Override
	public String getFileName() {
		String name = this.url.getFile();
		if (!StringUtils.hasText(name)) {
			return null;
		}
		if (name.endsWith("/")) {
			name = name.substring(0, name.length() - 1);
		}
		int index = name.lastIndexOf('/');
		return (index != -1) ? name.substring(index + 1) : name;
	}

	@Override
	public boolean exists() {
		try (InputStream ignored = getInputStream()) {
			return true;
		}
		catch (IOException ex) {
			return false;
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return this.url.openStream();
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		UrlResource that = (UrlResource) other;
		return this.url.equals(that.url);
	}

	@Override
	public int hashCode() {
		return this.url.hashCode();
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", UrlResource.class.getSimpleName() + "[", "]").add("url=" + this.url).toString();
	}

}
