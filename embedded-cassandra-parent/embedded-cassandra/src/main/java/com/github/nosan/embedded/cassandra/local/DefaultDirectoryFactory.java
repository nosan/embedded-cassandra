/*
 * Copyright 2018-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra.local;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.artifact.Artifact;

/**
 * Default factory to create a {@link Directory}.
 *
 * @author Dmytro Nosan
 * @since 1.0.9
 */
class DefaultDirectoryFactory implements DirectoryFactory {

	@Nonnull
	private final Version version;

	@Nonnull
	private final Path directory;

	@Nullable
	private final URL configurationFile;

	@Nullable
	private final URL logbackFile;

	@Nullable
	private final URL rackFile;

	@Nullable
	private final URL topologyFile;


	/**
	 * Creates a {@link DefaultDirectoryFactory}.
	 *
	 * @param directory a working directory
	 * @param version a version
	 * @param configurationFile URL to {@code cassandra.yaml}
	 * @param logbackFile URL to {@code logback.xml}
	 * @param rackFile URL to {@code cassandra-rackdc.properties}
	 * @param topologyFile URL to {@code cassandra-topology.properties}
	 */
	DefaultDirectoryFactory(@Nonnull Version version, @Nonnull Path directory, @Nullable URL configurationFile,
			@Nullable URL logbackFile, @Nullable URL rackFile, @Nullable URL topologyFile) {
		this.version = version;
		this.directory = directory;
		this.configurationFile = configurationFile;
		this.logbackFile = logbackFile;
		this.rackFile = rackFile;
		this.topologyFile = topologyFile;
	}

	@Nonnull
	@Override
	public Directory create(@Nonnull Artifact artifact) {
		List<DirectoryCustomizer> customizers = new ArrayList<>();
		customizers.add(new LogbackFileCustomizer(this.logbackFile));
		customizers.add(new ConfigurationFileCustomizer(this.configurationFile));
		customizers.add(new RackFileCustomizer(this.rackFile));
		customizers.add(new TopologyFileCustomizer(this.topologyFile));
		customizers.add(new PortReplacerCustomizer(this.version));
		return new DefaultDirectory(this.directory, artifact, customizers);
	}
}
