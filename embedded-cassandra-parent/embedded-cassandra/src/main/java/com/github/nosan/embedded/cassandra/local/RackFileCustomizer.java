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

package com.github.nosan.embedded.cassandra.local;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link DirectoryCustomizer} to initialize {@code cassandra-rackdc.properties}.
 *
 * @author Dmytro Nosan
 * @since 1.0.9
 */
class RackFileCustomizer implements DirectoryCustomizer {

	private static final Logger log = LoggerFactory.getLogger(RackFileCustomizer.class);

	@Nullable
	private final URL rackFile;

	/**
	 * Creates a {@link RackFileCustomizer}.
	 *
	 * @param rackFile URL to {@code cassandra-rackdc.properties}
	 */
	RackFileCustomizer(@Nullable URL rackFile) {
		this.rackFile = rackFile;
	}

	@Override
	public void customize(@Nonnull Path directory) throws IOException {
		if (this.rackFile != null) {
			Path target = directory.resolve("conf/cassandra-rackdc.properties");
			if (log.isDebugEnabled()) {
				log.debug("Replace ({}) with ({})", target, this.rackFile);
			}

			try (InputStream is = this.rackFile.openStream()) {
				Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
			}
			catch (IOException ex) {
				throw new IOException(String.format("Rack Properties : (%s) could not be saved",
						this.rackFile), ex);
			}
		}

	}
}

