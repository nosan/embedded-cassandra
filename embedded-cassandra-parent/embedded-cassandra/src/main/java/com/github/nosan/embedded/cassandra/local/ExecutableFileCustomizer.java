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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.Version;

/**
 * {@link DirectoryCustomizer} to set 'executable permission' to {@code bin/cassandra} file.
 *
 * @author Dmytro Nosan
 * @since 1.2.5
 */
class ExecutableFileCustomizer implements DirectoryCustomizer {

	private static final Logger log = LoggerFactory.getLogger(ExecutableFileCustomizer.class);

	@Override
	public void customize(@Nonnull Path directory, @Nonnull Version version) {
		setExecutable(directory.resolve("bin/cassandra"));
	}

	private static void setExecutable(Path path) {
		try {
			if (!Files.exists(path) || Files.isExecutable(path)) {
				return;
			}
			File file = path.toFile();
			if (!file.setExecutable(true) || file.setExecutable(true, false)) {
				log.debug("'executable' permission has been set to ({})", file);
			}
		}
		catch (Exception ex) {
			if (log.isDebugEnabled()) {
				log.error(String.format("Could not set 'executable' permission to (%s)", path), ex);
			}
		}
	}
}
