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

import java.io.IOException;

import com.github.nosan.embedded.cassandra.config.CassandraProcessConfig;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;
import de.flapdoodle.embed.process.runtime.Executable;

/**
 * {@link Executable} for embedded cassandra server.
 *
 * @author Dmytro Nosan
 */
public class CassandraServerExecutable
		extends Executable<CassandraProcessConfig, CassandraServerProcess> {

	public CassandraServerExecutable(Distribution distribution,
			CassandraProcessConfig config, IRuntimeConfig runtimeConfig,
			IExtractedFileSet executable) {
		super(distribution, config, runtimeConfig, executable);
	}

	@Override
	protected CassandraServerProcess start(Distribution distribution,
			CassandraProcessConfig config, IRuntimeConfig runtime) throws IOException {
		return new CassandraServerProcess(distribution, config, runtime, this);
	}

}
