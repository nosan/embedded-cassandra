/*
 * Copyright 2012-2018 the original author or authors.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Convenience base class for {@link CqlScript} implementations,
 * pre-implementing CQL scripts parsing.
 *
 * @author Dmytro Nosan
 */
public abstract class AbstractCqlScript implements CqlScript {

	private final Charset encoding;


	protected AbstractCqlScript() {
		this(null);
	}

	protected AbstractCqlScript(Charset encoding) {
		this.encoding = (encoding != null ? encoding : Charset.defaultCharset());
	}

	@Override
	public Collection<String> getStatements() {
		try {
			return CqlScriptParser.getStatements(readAll(getInputStream(), this.encoding));
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public String toString() {
		return String.format("Script: '%s'. Encoding: '%s'", getName(), this.encoding);
	}

	/**
	 * Return an {@link InputStream} for the content of an underlying resource.
	 * <p>It is expected that each call creates a <i>fresh</i> stream.
	 *
	 * @return the input stream for the underlying resource
	 * @throws IOException if the content stream could not be opened
	 */
	public abstract InputStream getInputStream() throws IOException;

	/**
	 * Return the encoding to use for reading from the {@linkplain #getInputStream() Stream}.
	 *
	 * @return encoding to use.
	 */
	public Charset getEncoding() {
		return this.encoding;
	}


	private static String readAll(InputStream resource, Charset charset) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource, charset))) {
			return reader
					.lines()
					.collect(Collectors.joining(System.lineSeparator()));
		}
	}

}
