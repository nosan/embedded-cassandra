/*
 * Copyright 2020-2025 the original author or authors.
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

package com.github.nosan.embedded.cassandra.commons;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * Utility class providing simple methods for working with streams.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public final class StreamUtils {

	private static final int BUFFER_SIZE = 8192;

	private StreamUtils() {
	}

	/**
	 * Converts the contents of an {@link InputStream} to a {@link String} using the specified {@link Charset}.
	 *
	 * <p><b>Note:</b> The provided {@link InputStream} will <strong>not</strong> be closed by this method. It is the
	 * caller's responsibility to close the stream once processing is complete.</p>
	 *
	 * @param inputStream the {@link InputStream} to read from
	 * @param charset the character encoding to use
	 * @return the decoded {@link String} representation of the {@link InputStream}'s contents
	 * @throws IOException if an I/O error occurs while reading the {@link InputStream}
	 * @throws NullPointerException if {@code inputStream} or {@code charset} is {@code null}
	 */
	public static String toString(InputStream inputStream, Charset charset) throws IOException {
		Objects.requireNonNull(inputStream, "InputStream must not be null");
		Objects.requireNonNull(charset, "Charset must not be null");
		StringBuilder out = new StringBuilder();
		try (InputStreamReader reader = new InputStreamReader(inputStream, charset)) {
			char[] buffer = new char[BUFFER_SIZE];
			int read;
			while ((read = reader.read(buffer)) != -1) {
				out.append(buffer, 0, read);
			}
		}
		return out.toString();
	}

	/**
	 * Reads the entire contents of an {@link InputStream} into a byte array.
	 *
	 * <p><b>Note:</b> The provided {@link InputStream} will <strong>not</strong> be closed by this method. It is the
	 * caller's responsibility to close the stream once processing is complete.</p>
	 *
	 * @param inputStream the {@link InputStream} to read from
	 * @return a byte array containing the data read from the {@link InputStream}
	 * @throws IOException if an I/O error occurs while reading the {@link InputStream}
	 * @throws NullPointerException if {@code inputStream} is {@code null}
	 */
	public static byte[] toByteArray(InputStream inputStream) throws IOException {
		Objects.requireNonNull(inputStream, "InputStream must not be null");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		copy(inputStream, out);
		return out.toByteArray();
	}

	/**
	 * Copies the contents of an {@link InputStream} to an {@link OutputStream}.
	 *
	 * <p><b>Note:</b> Neither the {@link InputStream} nor the {@link OutputStream} will be closed by this method. It
	 * is the caller's responsibility to close the streams once processing is complete.</p>
	 *
	 * @param inputStream the {@link InputStream} to read from
	 * @param outputStream the {@link OutputStream} to write to
	 * @throws IOException if an I/O error occurs while reading or writing
	 * @throws NullPointerException if {@code inputStream} or {@code outputStream} is {@code null}
	 */
	public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
		Objects.requireNonNull(inputStream, "InputStream must not be null");
		Objects.requireNonNull(outputStream, "OutputStream must not be null");
		byte[] buffer = new byte[BUFFER_SIZE];
		int read;
		while ((read = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, read);
		}
		outputStream.flush();
	}

}
