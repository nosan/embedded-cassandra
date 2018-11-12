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

import com.github.nosan.embedded.cassandra.Version;

/**
 * {@link DirectoryCustomizer} to initialize {@code cassandra.yaml}.
 *
 * @author Dmytro Nosan
 * @since 1.0.9
 */
class ConfigurationFileCustomizer implements DirectoryCustomizer {

	private static final Logger log = LoggerFactory.getLogger(ConfigurationFileCustomizer.class);

	@Nullable
	private final URL configurationFile;

	@Nonnull
	private final Version version;


	/**
	 * Creates a {@link ConfigurationFileCustomizer}.
	 *
	 * @param configurationFile URL to {@code cassandra.yaml}
	 * @param version a version
	 */
	ConfigurationFileCustomizer(@Nullable URL configurationFile, @Nonnull Version version) {
		this.configurationFile = configurationFile;
		this.version = version;
	}

	@Override
	public void customize(@Nonnull Path directory) throws IOException {
		URL source = this.configurationFile;
		if (source == null) {
			if (this.version.getMajor() >= 4) {
				source = ClassLoader.getSystemResource("com/github/nosan/embedded" +
						"/cassandra/local/4.x.x/cassandra.yaml");
			}
			else {
				source = ClassLoader.getSystemResource("com/github/nosan/embedded" +
						"/cassandra/local/cassandra.yaml");
			}
		}
		Path target = directory.resolve("conf/cassandra.yaml");
		log.debug("Replace ({}) with ({})", target, source);
		try (InputStream is = source.openStream()) {
			Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException ex) {
			throw new IOException(String.format("Configuration file : (%s) could not be saved", source), ex);
		}
	}

}
