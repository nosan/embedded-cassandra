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

import com.github.nosan.embedded.cassandra.ExecutableConfig;

/**
 * Simple class for executing {@link CassandraProcess}. Note! This class is not a {@code Thread Safe.}
 * <pre>Warning! Only for internal purposes. </pre>
 *
 * @author Dmytro Nosan
 * @see CassandraProcess
 * @see CassandraStarter
 * @see com.github.nosan.embedded.cassandra.Cassandra
 */
public final class CassandraExecutable {

	private final CassandraProcess process;

	private final Distribution distribution;

	private final IRuntimeConfig runtime;

	private final IExtractedFileSet files;


	CassandraExecutable(Distribution distribution, ExecutableConfig executableConfig,
			IRuntimeConfig runtime, IExtractedFileSet files) {
		this.process = new CassandraProcess(distribution, executableConfig, runtime, files);
		this.distribution = distribution;
		this.runtime = runtime;
		this.files = files;
	}

	/**
	 * Start a cassandra process using following steps:
	 *
	 * @throws IOException Cassandra's process has not been started correctly.
	 */
	public void start() throws IOException {
		this.process.start();
	}

	/**
	 * Stop Cassandra's process and cleans resources.
	 */
	public void stop() {
		this.process.stop();
		this.runtime.getArtifactStore().removeFileSet(this.distribution, this.files);
	}

}
