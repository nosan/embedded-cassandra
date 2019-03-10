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

package com.github.nosan.embedded.cassandra.local.artifact;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.nosan.embedded.cassandra.Version;

/**
 * {@link Artifact} which always returns an empty {@code zip} archive.
 *
 * @author Dmytro Nosan
 * @since 1.2.8
 */
class EmptyArtifact implements Artifact {

	private final Version version;

	EmptyArtifact(Version version) {
		this.version = version;
	}

	@Override
	public Path get() throws IOException {
		Path tempFile = Files.createTempFile(null, String.format("-apache-cassandra-%s.zip", this.version));
		try {
			tempFile.toFile().deleteOnExit();
		}
		catch (UnsupportedOperationException ignore) {
		}
		return tempFile;
	}

}
