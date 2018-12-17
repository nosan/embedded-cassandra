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

import com.github.nosan.embedded.cassandra.util.OS;

/**
 * {@link DirectoryCustomizer} to set 'executable mode' to {@code cassandra} file.
 *
 * @author Dmytro Nosan
 * @since 1.2.5
 */
class ExecutableCustomizer implements DirectoryCustomizer {

	@Override
	public void customize(@Nonnull Path directory) {
		if (OS.get() == OS.WINDOWS) {
			setExecutable(directory.resolve("bin/cassandra.ps1"));
			setExecutable(directory.resolve("bin/stop-server.ps1"));
		}
		else {
			setExecutable(directory.resolve("bin/cassandra"));
		}
	}

	private static boolean setExecutable(Path path) {
		try {
			if (!Files.exists(path)) {
				return false;
			}
			if (Files.isExecutable(path)) {
				return true;
			}
			File file = path.toFile();
			return file.setExecutable(true) || file.setExecutable(true, false);
		}
		catch (Exception ex) {
			return false;
		}
	}
}
