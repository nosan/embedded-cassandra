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

package com.github.nosan.embedded.cassandra.local;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.github.nosan.embedded.cassandra.Version;

/**
 * {@link WorkingDirectoryCustomizer} that replaces {@code cassandra-rackdc.properties} file.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
class RackFileCustomizer implements WorkingDirectoryCustomizer {

	private final URL rackFile;

	RackFileCustomizer(URL rackFile) {
		this.rackFile = rackFile;
	}

	@Override
	public void customize(Path workingDirectory, Version version) throws IOException {
		try (InputStream inputStream = new BufferedInputStream(this.rackFile.openStream())) {
			Files.copy(inputStream, workingDirectory.resolve("conf/cassandra-rackdc.properties"),
					StandardCopyOption.REPLACE_EXISTING);
		}
	}

}
