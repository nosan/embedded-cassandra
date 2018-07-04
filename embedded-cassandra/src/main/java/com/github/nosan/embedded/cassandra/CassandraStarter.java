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

package com.github.nosan.embedded.cassandra;

import java.util.Objects;

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;
import de.flapdoodle.embed.process.runtime.Starter;
import org.slf4j.Logger;

import com.github.nosan.embedded.cassandra.config.ExecutableConfig;
import com.github.nosan.embedded.cassandra.config.ExecutableConfigBuilder;
import com.github.nosan.embedded.cassandra.config.RuntimeConfigBuilder;

/**
 * {@link Starter} for an embedded cassandra.
 *
 * @author Dmytro Nosan
 * @see RuntimeConfigBuilder
 * @see ExecutableConfigBuilder
 */
public class CassandraStarter
		extends Starter<ExecutableConfig, CassandraExecutable, CassandraProcess> {

	public CassandraStarter(IRuntimeConfig config) {
		super(Objects.requireNonNull(config, "RuntimeConfig must not be null"));
	}

	/**
	 * Create a new cassandra starter with default runtime settings.
	 */
	public CassandraStarter() {
		this(new RuntimeConfigBuilder().build());
	}

	/**
	 * Create a new cassandra starter with default runtime settings.
	 * @param logger logger for process outputs.
	 */
	public CassandraStarter(Logger logger) {
		this(new RuntimeConfigBuilder(logger).build());
	}

	@Override
	protected CassandraExecutable newExecutable(ExecutableConfig config,
			Distribution distribution, IRuntimeConfig runtime, IExtractedFileSet exe) {
		return new CassandraExecutable(distribution, config, runtime, exe);
	}

}
