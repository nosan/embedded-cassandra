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

import com.datastax.oss.driver.api.core.CqlSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.CassandraRunner;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CqlSessionFactory}.
 *
 * @author Dmytro Nosan
 */
class CqlSessionConnectionTests {

	private final LocalCassandraFactory cassandraFactory = new LocalCassandraFactory();

	private final CqlSessionFactory sessionFactory = new CqlSessionFactory();

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
		this.sessionFactory.setUsername("cassandra");
		this.sessionFactory.setPassword("cassandra");
		this.sessionFactory.setSslEnabled(true);
		this.sessionFactory.setKeystorePath(keystoreFile);
		this.sessionFactory.setTruststorePath(truststoreFile);
		this.sessionFactory.setTruststorePassword("cassandra");
		this.sessionFactory.setKeystorePassword("cassandra");
	}

	@Test
	void configureCqlSession() {
		TestCassandra testCassandra = new TestCassandra(this.cassandraFactory,
				new CqlSessionConnectionFactory(this.sessionFactory), CqlScript.classpath("init.cql"));
		AtomicReference<CqlSession> session = new AtomicReference<>();
		new CassandraRunner(testCassandra)
				.run(cassandra -> session.set(((TestCassandra) cassandra).getNativeConnection(CqlSession.class)));
		assertThat(session.get().isClosed()).isTrue();
	}

}
