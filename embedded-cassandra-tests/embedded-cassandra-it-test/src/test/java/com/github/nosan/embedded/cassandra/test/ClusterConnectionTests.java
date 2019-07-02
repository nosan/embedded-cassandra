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

package com.github.nosan.embedded.cassandra.test;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

import com.datastax.driver.core.Cluster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.CassandraRunner;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ClusterFactory}.
 *
 * @author Dmytro Nosan
 */

class ClusterConnectionTests {

	private final LocalCassandraFactory cassandraFactory = new LocalCassandraFactory();

	private final ClusterFactory clusterFactory = new ClusterFactory();

	@BeforeEach
	void setUp() throws URISyntaxException {
		Path keystoreFile = Paths.get(getClass().getResource("/cert/keystore.node0").toURI());
		Path truststoreFile = Paths.get(getClass().getResource("/cert/truststore.node0").toURI());
		this.cassandraFactory.setConfigurationFile(getClass().getResource("/cassandra-secure.yaml"));
		this.cassandraFactory.getWorkingDirectoryCustomizers().add((workDir, v) -> {
			Files.copy(keystoreFile, workDir.resolve("conf").resolve("keystore.node0"));
			Files.copy(truststoreFile, workDir.resolve("conf").resolve("truststore.node0"));
		});
		this.cassandraFactory.getJvmOptions().add("-Dcassandra.superuser_setup_delay_ms=0");
		this.clusterFactory.setUsername("cassandra");
		this.clusterFactory.setPassword("cassandra");
		this.clusterFactory.setSslEnabled(true);
		this.clusterFactory.setKeystorePath(keystoreFile);
		this.clusterFactory.setTruststorePath(truststoreFile);
		this.clusterFactory.setTruststorePassword("cassandra");
		this.clusterFactory.setKeystorePassword("cassandra");
	}

	@Test
	void configureClusterConnection() {
		TestCassandra testCassandra = new TestCassandra(this.cassandraFactory,
				new ClusterConnectionFactory(this.clusterFactory), CqlScript.classpath("init.cql"));
		AtomicReference<Cluster> cluster = new AtomicReference<>();
		new CassandraRunner(testCassandra).run(
				cassandra -> cluster.set(((TestCassandra) cassandra).getNativeConnection(Cluster.class)));
		assertThat(cluster.get().isClosed()).isTrue();
	}

}
