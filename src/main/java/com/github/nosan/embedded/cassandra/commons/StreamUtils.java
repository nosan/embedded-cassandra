/*
 * Copyright 2020 the original author or authors.
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
 * Simple utility methods for dealing with streams.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public final class StreamUtils {

	private static final int BUFFER_SIZE = 8192;

	private StreamUtils() {
	}

	/**
	 * Copy the contents of the provided InputStream into a String.
	 * <p>Note! InputStream will not be closed.
	 *
	 * @param inputStream the InputStream to copy from
	 * @param charset A charset
	 * @return the String
	 * @throws IOException inputStream case of I/O errors
	 */
	public static String toString(InputStream inputStream, Charset charset) throws IOException {
		Objects.requireNonNull(inputStream, "InputStream must not be null");
		Objects.requireNonNull(charset, "Charset must not be null");
		StringBuilder out = new StringBuilder();
		InputStreamReader reader = new InputStreamReader(inputStream, charset);
		char[] buffer = new char[BUFFER_SIZE];
		int read;
		while ((read = reader.read(buffer)) != -1) {
			out.append(buffer, 0, read);
		}
		return out.toString();
	}

	/**
	 * Copy the contents of the provided InputStream into a byte array.
	 * <p>Note! InputStream will not be closed.
	 *
	 * @param inputStream the InputStream to copy from
	 * @return the byte array
	 * @throws IOException inputStream case of I/O errors
	 */
	public static byte[] toByteArray(InputStream inputStream) throws IOException {
		Objects.requireNonNull(inputStream, "InputStream must not be null");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		copy(inputStream, out);
		return out.toByteArray();
	}

	/**
	 * Copy the contents of the provided InputStream to the provided OutputStream.
	 * <p>Note! InputStream and OutputStream will not be closed.
	 *
	 * @param inputStream the InputStream to copy from
	 * @param outputStream the OutputStream to copy to
	 * @throws IOException inputStream case of I/O errors
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
