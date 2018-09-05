/*
 * Copyright 2018-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.compress.utils.IOUtils;

/**
 * Utility methods for dealing with streams.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
public abstract class StreamUtils {

	/**
	 * Copy the contents of the given InputStream into a String.
	 * Closes the stream when done.
	 *
	 * @param stream the InputStream to copy from
	 * @param charset the Charset to use
	 * @return the String that has been copied to (possibly empty)
	 * @throws IOException in case of I/O errors
	 */
	@Nonnull
	public static String toString(@Nullable InputStream stream, @Nonnull Charset charset) throws IOException {
		if (stream == null) {
			return "";
		}
		return toString(new InputStreamReader(stream, charset));
	}

	/**
	 * Copy the contents of the given Reader into a String.
	 * Closes the reader when done.
	 *
	 * @param reader the Reader to copy from
	 * @return the String that has been copied to (possibly empty)
	 * @throws IOException in case of I/O errors
	 */
	@Nonnull
	public static String toString(@Nullable Reader reader) throws IOException {
		if (reader == null) {
			return "";
		}
		StringBuilder out = new StringBuilder();
		try {
			char[] buffer = new char[4096];
			int b;
			while ((b = reader.read(buffer)) != -1) {
				out.append(buffer, 0, b);
			}
		}
		finally {
			IOUtils.closeQuietly(reader);
		}
		return out.toString();
	}
}
