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

/**
 * A simple implementation of {@link CassandraDatabase}.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
class LocalCassandraDatabase implements CassandraDatabase {

	private static final Logger log = LoggerFactory.getLogger(LocalCassandraDatabase.class);

	private final CassandraNode node;

	private final Path workingDirectory;

	private final boolean deleteWorkingDirectory;

	private final List<WorkingDirectoryCustomizer> workingDirectoryCustomizers;

	LocalCassandraDatabase(Path workingDirectory, boolean deleteWorkingDirectory,
			List<WorkingDirectoryCustomizer> workingDirectoryCustomizers, CassandraNode node) {
		this.node = node;
		this.workingDirectory = workingDirectory;
		this.deleteWorkingDirectory = deleteWorkingDirectory;
		this.workingDirectoryCustomizers = Collections.unmodifiableList(new ArrayList<>(workingDirectoryCustomizers));
	}

	@Override
	public synchronized void start() throws IOException, InterruptedException {
		initialize();
		Version version = getVersion();
		log.info("Start Apache Cassandra '{}'", version);
		long start = System.currentTimeMillis();
		this.node.start();
		long elapsed = System.currentTimeMillis() - start;
		log.info("Apache Cassandra '{}' is started ({} ms)", version, elapsed);
	}

	@Override
	public synchronized void stop() throws IOException, InterruptedException {
		long start = System.currentTimeMillis();
		Version version = getVersion();
		log.info("Stop Apache Cassandra '{}'", version);
		this.node.stop();
		long elapsed = System.currentTimeMillis() - start;
		log.info("Apache Cassandra '{}' is stopped ({} ms)", version, elapsed);
		delete();
	}

	@Override
	public synchronized Settings getSettings() {
		return this.node.getSettings();
	}

	@Override
	public Version getVersion() {
		return this.node.getVersion();
	}

	private void initialize() throws IOException {
		Version version = getVersion();
		log.info("Initialize Apache Cassandra '{}'. It takes a while...", version);
		long start = System.currentTimeMillis();
		for (WorkingDirectoryCustomizer customizer : this.workingDirectoryCustomizers) {
			customizer.customize(this.workingDirectory, version);
		}
		long elapsed = System.currentTimeMillis() - start;
		log.info("Apache Cassandra '{}' is initialized ({} ms)", version, elapsed);
	}

	private void delete() throws IOException {
		if (this.deleteWorkingDirectory && FileUtils.delete(this.workingDirectory)) {
			log.info("The working directory '{}' was deleted.", this.workingDirectory);
		}
	}

}
