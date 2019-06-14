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

import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.CassandraRunner;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;

/**
 * Tests for {@link CqlSessionFactory}.
 *
 * @author Dmytro Nosan
 */
class CqlSessionFactoryTests {

	@Test
	void configureCqlSession() throws URISyntaxException {
		LocalCassandraFactory cassandraFactory = new LocalCassandraFactory();
		Path keystoreFile = Paths.get(getClass().getResource("/cert/keystore.node0").toURI());
		Path truststoreFile = Paths.get(getClass().getResource("/cert/truststore.node0").toURI());
		cassandraFactory.setConfigurationFile(getClass().getResource("/cassandra-secure.yaml"));
		cassandraFactory.getWorkingDirectoryCustomizers().add((workDir, v) -> {
			Files.copy(keystoreFile, workDir.resolve("conf").resolve("keystore.node0"));
			Files.copy(truststoreFile, workDir.resolve("conf").resolve("truststore.node0"));
		});
		cassandraFactory.getJvmOptions().add("-Dcassandra.superuser_setup_delay_ms=0");

		TestCassandra testCassandra = new TestCassandra(cassandraFactory, CqlScript.classpath("init.cql")) {

			@Override
			protected Connection createConnection() {
				CqlSessionFactory sessionFactory = new CqlSessionFactory();
				sessionFactory.setSslEnabled(true);
				sessionFactory.setUsername("cassandra");
				sessionFactory.setPassword("cassandra");
				sessionFactory.setKeystorePath(keystoreFile);
				sessionFactory.setTruststorePath(truststoreFile);
				sessionFactory.setHostnameValidation(false);
				sessionFactory.setTruststorePassword("cassandra");
				sessionFactory.setKeystorePassword("cassandra");
				return new CqlSessionConnection(sessionFactory.create(getSettings()));
			}
		};
		new CassandraRunner(testCassandra).run();
	}

}
