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

import com.github.nosan.embedded.cassandra.config.CassandraProcessConfig;
import com.github.nosan.embedded.cassandra.config.CassandraRuntimeConfigBuilder;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;
import de.flapdoodle.embed.process.runtime.Starter;
import org.slf4j.Logger;

/**
 * {@link Starter} for an embedded cassandra.
 *
 * @author Dmytro Nosan
 */
public class CassandraServerStarter extends
		Starter<CassandraProcessConfig, CassandraServerExecutable, CassandraServerProcess> {

	public CassandraServerStarter(IRuntimeConfig config) {
		super(config);
	}

	/**
	 * Create a new cassandra starter with
	 * {@link CassandraRuntimeConfigBuilder#defaults()} settings.
	 */
	public CassandraServerStarter() {
		this(new CassandraRuntimeConfigBuilder().defaults().build());
	}

	/**
	 * Create a new cassandra starter with
	 * {@link CassandraRuntimeConfigBuilder#defaults(Logger)} settings.
	 * @param logger logger for process outputs.
	 */
	public CassandraServerStarter(Logger logger) {
		this(new CassandraRuntimeConfigBuilder().defaults(logger).build());
	}

	@Override
	protected CassandraServerExecutable newExecutable(CassandraProcessConfig config,
			Distribution distribution, IRuntimeConfig runtime, IExtractedFileSet exe) {
		return new CassandraServerExecutable(distribution, config, runtime, exe);
	}

}
