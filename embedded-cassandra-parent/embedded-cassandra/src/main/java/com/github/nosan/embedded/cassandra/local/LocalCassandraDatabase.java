/*
 * Copyright 2018-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nosan.embedded.cassandra.local;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.artifact.ArtifactFactory;
import com.github.nosan.embedded.cassandra.util.FileUtils;

/**
 * A simple implementation of {@link CassandraDatabase}.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
class LocalCassandraDatabase implements CassandraDatabase {

	private static final Logger log = LoggerFactory.getLogger(LocalCassandraDatabase.class);

	private final Path workingDirectory;

	private final CassandraNode node;

	private final Path artifactDirectory;

	private final ArtifactFactory artifactFactory;

	private final boolean deleteWorkingDirectory;

	private final List<WorkingDirectoryCustomizer> workingDirectoryCustomizers;

	LocalCassandraDatabase(CassandraNode node, Path workingDirectory, Path artifactDirectory,
			ArtifactFactory artifactFactory, boolean deleteWorkingDirectory,
			List<WorkingDirectoryCustomizer> workingDirectoryCustomizers) {
		this.node = node;
		this.workingDirectory = workingDirectory;
		this.artifactDirectory = artifactDirectory;
		this.artifactFactory = artifactFactory;
		this.deleteWorkingDirectory = deleteWorkingDirectory;
		this.workingDirectoryCustomizers = Collections.unmodifiableList(new ArrayList<>(workingDirectoryCustomizers));
	}

	@Override
	public void start() throws IOException, InterruptedException {
		initialize();
		log.info("Start Apache Cassandra '{}'", getVersion());
		long start = System.currentTimeMillis();
		this.node.start();
		long elapsed = System.currentTimeMillis() - start;
		log.info("Apache Cassandra '{}' is started ({} ms)", getVersion(), elapsed);
	}

	@Override
	public void stop() throws IOException, InterruptedException {
		long start = System.currentTimeMillis();
		log.info("Stop Apache Cassandra '{}'", getVersion());
		this.node.stop();
		delete();
		long elapsed = System.currentTimeMillis() - start;
		log.info("Apache Cassandra '{}' is stopped ({} ms)", getVersion(), elapsed);
	}

	@Override
	public Version getVersion() {
		return this.node.getVersion();
	}

	@Override
	public Settings getSettings() {
		return this.node.getSettings();
	}

	private void initialize() throws IOException {
		log.info("Initialize Apache Cassandra '{}'. It takes a while...", getVersion());
		long start = System.currentTimeMillis();
		Path workingDirectory = this.workingDirectory;
		ArtifactFactory artifactFactory = this.artifactFactory;
		Path artifactDirectory = this.artifactDirectory;
		ArtifactWorkingDirectoryInitializer artifactWorkingDirectoryInitializer =
				new ArtifactWorkingDirectoryInitializer(artifactFactory, artifactDirectory);
		artifactWorkingDirectoryInitializer.initialize(workingDirectory, getVersion());
		for (WorkingDirectoryCustomizer workingDirectoryCustomizer : this.workingDirectoryCustomizers) {
			workingDirectoryCustomizer.customize(workingDirectory, getVersion());
		}
		long elapsed = System.currentTimeMillis() - start;
		log.info("Apache Cassandra '{}' is initialized ({} ms)", getVersion(), elapsed);
	}

	private void delete() throws IOException {
		if (this.deleteWorkingDirectory && FileUtils.delete(this.workingDirectory)) {
			log.info("The '{}' directory is deleted.", this.workingDirectory);
		}
	}

}
