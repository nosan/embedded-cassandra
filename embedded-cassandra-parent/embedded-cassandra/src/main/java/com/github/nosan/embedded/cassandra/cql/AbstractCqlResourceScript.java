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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;

import org.apache.commons.compress.utils.IOUtils;
import org.apiguardian.api.API;

import com.github.nosan.embedded.cassandra.lang.Nullable;

/**
 * Base class for {@link AbstractCqlScript} implementations,
 * pre-implementing {@link #getScript()} method.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
@API(since = "1.0.0", status = API.Status.STABLE)
public abstract class AbstractCqlResourceScript extends AbstractCqlScript {

	private final Charset encoding;

	/**
	 * Creates {@link AbstractCqlResourceScript} with an encoding.
	 *
	 * @param encoding the encoding to use for reading from the resource
	 */
	protected AbstractCqlResourceScript(@Nullable Charset encoding) {
		this.encoding = (encoding != null) ? encoding : Charset.defaultCharset();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws UncheckedIOException if an I/O error occurs
	 */
	@Override
	protected final String getScript() {
		try (InputStream is = getInputStream()) {
			return new String(IOUtils.toByteArray(is), getEncoding());
		}
		catch (IOException ex) {
			throw new UncheckedIOException(String.format("Could not open a stream for CQL Script (%s)", toString()),
					ex);
		}
	}

	/**
	 * Return an {@link InputStream} for the content of an underlying resource.
	 * <p>It is expected that each call creates a <i>fresh</i> stream.
	 *
	 * @return the input stream for the underlying resource
	 * @throws IOException if the content stream could not be opened
	 */
	protected abstract InputStream getInputStream() throws IOException;

	/**
	 * Return the encoding to use for reading from the {@link #getInputStream() Stream}.
	 *
	 * @return encoding to use.
	 */
	protected final Charset getEncoding() {
		return this.encoding;
	}

}
