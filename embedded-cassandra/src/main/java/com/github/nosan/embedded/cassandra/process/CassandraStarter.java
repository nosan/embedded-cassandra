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

import java.util.Objects;

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;
import de.flapdoodle.embed.process.runtime.Starter;

import com.github.nosan.embedded.cassandra.ExecutableConfig;

/**
 * A simple implementation of {@link Starter Starter} for start a
 * {@link CassandraExecutable Cassandra Executable}.
 *
 * @author Dmytro Nosan
 * @see CassandraExecutable
 * @see CassandraProcess
 */
public final class CassandraStarter
		extends Starter<ExecutableConfig, CassandraExecutable, CassandraProcess> {

	public CassandraStarter(IRuntimeConfig config) {
		super(Objects.requireNonNull(config, "Runtime Config must not be null"));
	}

	@Override
	protected CassandraExecutable newExecutable(ExecutableConfig executableConfig,
			Distribution distribution, IRuntimeConfig runtime, IExtractedFileSet exe) {
		return new CassandraExecutable(distribution, executableConfig, runtime, exe);
	}

}
