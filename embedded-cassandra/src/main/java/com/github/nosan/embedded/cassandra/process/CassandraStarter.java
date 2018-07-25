/*
 * Copyright 2012-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra.process;

import java.io.IOException;

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;
import de.flapdoodle.embed.process.store.IArtifactStore;

import com.github.nosan.embedded.cassandra.ExecutableConfig;

/**
 * Simple class for starting {@link CassandraExecutable}. Note! This class is not a {@code Thread Safe.}
 * <pre>Warning! Only for internal purposes.</pre>
 *
 * @author Dmytro Nosan
 * @see CassandraProcess
 * @see CassandraExecutable
 * @see com.github.nosan.embedded.cassandra.Cassandra
 */
public final class CassandraStarter {

	private final IRuntimeConfig runtimeConfig;

	private final ExecutableConfig executableConfig;

	public CassandraStarter(IRuntimeConfig runtimeConfig, ExecutableConfig executableConfig) {
		this.runtimeConfig = runtimeConfig;
		this.executableConfig = executableConfig;
	}

	/**
	 * Creating a new {@link CassandraExecutable Executable}.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public CassandraExecutable newExecutable() throws IOException {
		IArtifactStore artifactStore = this.runtimeConfig.getArtifactStore();
		Distribution distribution = Distribution.detectFor(this.executableConfig.version());
		if (artifactStore.checkDistribution(distribution)) {
			IExtractedFileSet files = artifactStore.extractFileSet(distribution);
			return new CassandraExecutable(distribution, this.executableConfig, this.runtimeConfig, files);
		}
		throw new IOException(
				String.format("Could not find a Distribution. Please check Artifact Store: '%s' and " +
						"Distribution: '%s'", artifactStore, distribution));

	}

}
