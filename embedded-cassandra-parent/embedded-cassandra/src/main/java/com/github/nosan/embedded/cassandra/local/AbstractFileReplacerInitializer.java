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

package com.github.nosan.embedded.cassandra.local;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.BiFunction;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.util.annotation.Nullable;

/**
 * Abstract file {@link Initializer} to replace a file within a working directory.
 *
 * @author Dmytro Nosan
 * @since 1.4.2
 */
abstract class AbstractFileReplacerInitializer extends AbstractFileInitializer {

	@Nullable
	private final URL url;

	/**
	 * Creates a {@link AbstractFileReplacerInitializer}.
	 *
	 * @param url the URL to the resource for the file replacement
	 * @param fileMapper the function to resolve a file within a directory
	 */
	AbstractFileReplacerInitializer(@Nullable URL url, BiFunction<Path, Version, Path> fileMapper) {
		super(fileMapper);
		this.url = url;
	}

	@Override
	protected void initialize(Path file, Path workingDirectory, Version version) throws IOException {
		URL url = this.url;
		if (url != null) {
			if (this.log.isDebugEnabled()) {
				this.log.debug("Replace '{}' with a '{}'", file, url);
			}
			try (InputStream is = url.openStream()) {
				Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);
			}
			catch (ClosedByInterruptException ex) {
				throw ex;
			}
			catch (IOException ex) {
				throw new IOException(String.format("Can not replace '%s' with a '%s'", file, url), ex);
			}
		}
	}

}
