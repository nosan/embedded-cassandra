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
 * {@link DirectoryInitializer} to initialize {@code logback.xml}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
class LogbackFileInitializer implements DirectoryInitializer {

	private static final Logger log = LoggerFactory.getLogger(LogbackFileInitializer.class);

	@Nullable
	private final URL logbackFile;


	/**
	 * Creates an initializer.
	 *
	 * @param logbackFile URL to {@code logback.xml}
	 */
	LogbackFileInitializer(@Nullable URL logbackFile) {
		this.logbackFile = logbackFile;
	}

	@Override
	public void initialize(@Nonnull Path directory, @Nonnull Version version) throws Exception {
		URL logbackFile = this.logbackFile;
		if (logbackFile == null) {
			logbackFile = ClassLoader.getSystemResource("com/github/nosan/embedded/cassandra/local/logback.xml");
		}
		Path target = directory.resolve("conf/logback.xml");
		log.debug("Replace ({}) with ({})", target, logbackFile);
		try (InputStream is = logbackFile.openStream()) {
			Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException ex) {
			throw new IOException(String.format("Logback file : (%s) could not be saved", logbackFile), ex);
		}

	}
}
