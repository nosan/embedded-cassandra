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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Convenience base class for {@link CqlResource} implementations,
 * pre-implementing CQL scripts parsing.
 *
 * @author Dmytro Nosan
 */
public abstract class AbstractCqlResource implements CqlResource {

	private final Charset charset;


	protected AbstractCqlResource() {
		this(null);
	}

	protected AbstractCqlResource(Charset charset) {
		this.charset = (charset != null ? charset : Charset.defaultCharset());
	}

	private static String readAll(InputStream resource, Charset charset) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource, charset))) {
			return reader
					.lines()
					.collect(Collectors.joining(System.lineSeparator()));
		}
	}

	@Override
	public List<String> getStatements() {
		try {
			return CqlScriptsParser.getStatements(readAll(getInputStream(), this.charset));
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public String toString() {
		return String.format("Resource: '%s'. Charset: '%s'", getName(), this.charset);
	}

	protected abstract InputStream getInputStream() throws IOException;

}
