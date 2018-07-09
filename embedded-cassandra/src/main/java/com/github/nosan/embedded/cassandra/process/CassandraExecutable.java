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
import de.flapdoodle.embed.process.runtime.Executable;

import com.github.nosan.embedded.cassandra.ExecutableConfig;

/**
 * A simple implementation of {@link Executable Executable} for starting
 * {@link CassandraProcess Cassandra Process}.
 *
 * @author Dmytro Nosan
 * @see CassandraProcess
 * @see CassandraStarter
 * @see ExecutableConfig
 */
public final class CassandraExecutable
		extends Executable<ExecutableConfig, CassandraProcess> {

	CassandraExecutable(Distribution distribution, ExecutableConfig executableConfig,
			IRuntimeConfig runtimeConfig, IExtractedFileSet executable) {
		super(distribution, executableConfig, runtimeConfig, executable);
	}

	@Override
	protected CassandraProcess start(Distribution distribution,
			ExecutableConfig executableConfig, IRuntimeConfig runtime)
			throws IOException {
		return new CassandraProcess(distribution, executableConfig, runtime, this);
	}

}
