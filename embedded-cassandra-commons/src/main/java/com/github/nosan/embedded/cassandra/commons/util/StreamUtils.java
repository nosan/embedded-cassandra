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

package com.github.nosan.embedded.cassandra.commons.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Simple utility methods for dealing with streams.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public final class StreamUtils {

	private StreamUtils() {
	}

	/**
	 * Read all lines from a {@code InputStream} to the given {@link Consumer}. This method ensures that the {@code
	 * InputStream} is closed when all lines have been read or an exception is thrown. Note! Skipping empty lines.
	 *
	 * @param inputStream the stream
	 * @param charset the charset to use
	 * @param lineConsumer line consumer
	 * @throws UncheckedIOException if an I/O error occurs
	 */
	public static void lines(InputStream inputStream, Charset charset, Consumer<? super String> lineConsumer) {
		Objects.requireNonNull(inputStream, "'inputStream' must not be null");
		Objects.requireNonNull(charset, "'charset' must not be null");
		Objects.requireNonNull(lineConsumer, "'lineConsumer' must not be null");
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
			try {
				reader.lines().filter(StringUtils::hasText).forEach(lineConsumer);
			}
			catch (UncheckedIOException ex) {
				if (!ex.getMessage().contains("Stream closed")) {
					throw ex;
				}
			}
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

}
