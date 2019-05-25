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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.List;

import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * Abstract {@link CqlScript} implementation, that pre-implemented {@link #getStatements()}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
public abstract class AbstractCqlResourceScript implements CqlScript {

	private final Charset encoding;

	/**
	 * Creates {@link AbstractCqlResourceScript} with an encoding.
	 *
	 * @param encoding the encoding to use for reading from the resource
	 */
	protected AbstractCqlResourceScript(@Nullable Charset encoding) {
		this.encoding = (encoding != null) ? encoding : Charset.defaultCharset();
	}

	@Override
	public List<String> getStatements() {
		return CqlScriptParser.parse(getScript());
	}

	/**
	 * Return an {@link InputStream} for the content of an underlying resource.
	 * <p>It is expected that each call creates a <i>fresh</i> stream.
	 *
	 * @return the input stream for the underlying resource
	 * @throws IOException if the content stream can not be opened
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

	private static byte[] toByteArray(InputStream inputStream) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] buf = new byte[8192];
		int read;
		while ((read = inputStream.read(buf)) > 0) {
			outputStream.write(buf, 0, read);
		}
		return outputStream.toByteArray();
	}

	private String getScript() {
		try (InputStream is = new BufferedInputStream(getInputStream())) {
			return new String(toByteArray(is), getEncoding());
		}
		catch (IOException ex) {
			throw new UncheckedIOException(String.format("Can not open a stream for CQL Script '%s'", toString()),
					ex);
		}
	}

}
