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

package com.github.nosan.embedded.cassandra.local;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.artifact.Artifact;
import com.github.nosan.embedded.cassandra.local.artifact.ArtifactFactory;

/**
 * Basic implementation of the {@link Initializer}.
 *
 * @author Dmytro Nosan
 * @since 1.3.0
 */
class DefaultInitializer implements Initializer {

	@Nonnull
	private final Path workingDirectory;

	@Nonnull
	private final Version version;

	@Nonnull
	private final ArtifactFactory artifactFactory;

	@Nonnull
	private final Path artifactDirectory;

	@Nullable
	private final URL configurationFile;

	@Nullable
	private final URL logbackFile;

	@Nullable
	private final URL rackFile;

	@Nullable
	private final URL topologyFile;

	@Nullable
	private final URL commitLogArchivingFile;

	/**
	 * Creates a new {@link DefaultInitializer}.
	 *
	 * @param version a version
	 * @param artifactFactory a factory to create {@link Artifact}
	 * @param workingDirectory a directory to keep data/logs/etc... (must be writable)
	 * @param artifactDirectory a directory to extract an {@link Artifact} (must be writable)
	 * @param configurationFile URL to {@code cassandra.yaml}
	 * @param logbackFile URL to {@code logback.xml}
	 * @param rackFile URL to {@code cassandra-rackdc.properties}
	 * @param topologyFile URL to {@code cassandra-topology.properties}
	 * @param commitLogArchivingFile URL to {@code commitlog_archiving.properties}
	 */
	DefaultInitializer(@Nonnull Path workingDirectory, @Nonnull Version version,
			@Nonnull ArtifactFactory artifactFactory, @Nonnull Path artifactDirectory,
			@Nullable URL configurationFile, @Nullable URL logbackFile,
			@Nullable URL rackFile, @Nullable URL topologyFile, @Nullable URL commitLogArchivingFile) {
		this.workingDirectory = workingDirectory;
		this.version = version;
		this.artifactFactory = artifactFactory;
		this.artifactDirectory = artifactDirectory;
		this.configurationFile = configurationFile;
		this.logbackFile = logbackFile;
		this.rackFile = rackFile;
		this.topologyFile = topologyFile;
		this.commitLogArchivingFile = commitLogArchivingFile;
	}

	@Override
	public void initialize() throws IOException {
		List<DirectoryCustomizer> customizers = new ArrayList<>();
		customizers.add(new ArtifactCustomizer(this.artifactFactory, this.artifactDirectory));
		customizers.add(new ExecutableFileCustomizer());
		customizers.add(new LogbackFileCustomizer(this.logbackFile));
		customizers.add(new ConfigurationFileCustomizer(this.configurationFile));
		customizers.add(new RackFileCustomizer(this.rackFile));
		customizers.add(new TopologyFileCustomizer(this.topologyFile));
		customizers.add(new CommitLogArchivingFileCustomizer(this.commitLogArchivingFile));
		customizers.add(new RandomPortConfigurationFileCustomizer());
		for (DirectoryCustomizer customizer : customizers) {
			customizer.customize(this.workingDirectory, this.version);
		}
	}
}
