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

import com.github.nosan.embedded.cassandra.ExecutableConfig;

/**
 * Simple holder for Cassandra Process.
 *
 * @author Dmytro Nosan
 */
class Context {

	private final Distribution distribution;

	private final IRuntimeConfig runtimeConfig;

	private final ExecutableConfig executableConfig;

	private final IExtractedFileSet extractedFileSet;

	Context(Distribution distribution, IRuntimeConfig runtimeConfig,
			ExecutableConfig executableConfig, IExtractedFileSet extractedFileSet) {
		this.distribution = Objects.requireNonNull(distribution,
				"Distribution must not be null");
		this.runtimeConfig = Objects.requireNonNull(runtimeConfig,
				"Runtime Config must not be null");
		this.executableConfig = Objects.requireNonNull(executableConfig,
				"Executable Config must not be null");
		this.extractedFileSet = Objects.requireNonNull(extractedFileSet,
				"File Set must not be null");
	}

	Distribution getDistribution() {
		return this.distribution;
	}

	IRuntimeConfig getRuntimeConfig() {
		return this.runtimeConfig;
	}

	ExecutableConfig getExecutableConfig() {
		return this.executableConfig;
	}

	IExtractedFileSet getExtractedFileSet() {
		return this.extractedFileSet;
	}

}
