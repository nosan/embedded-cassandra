/*
 * Copyright 2020-2021 the original author or authors.
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

package examples;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import com.datastax.oss.driver.api.core.CqlSession;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraBuilder;
import com.github.nosan.embedded.cassandra.DefaultWorkingDirectoryInitializer;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.SimpleSeedProviderConfigurator;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.WebCassandraDirectoryProvider;
import com.github.nosan.embedded.cassandra.WorkingDirectoryCustomizer;
import com.github.nosan.embedded.cassandra.WorkingDirectoryDestroyer;
import com.github.nosan.embedded.cassandra.WorkingDirectoryInitializer;
import com.github.nosan.embedded.cassandra.commons.ClassPathResource;
import com.github.nosan.embedded.cassandra.commons.FileSystemResource;
import com.github.nosan.embedded.cassandra.commons.logging.ConsoleLogger;
import com.github.nosan.embedded.cassandra.commons.logging.Logger;
import com.github.nosan.embedded.cassandra.commons.logging.Slf4jLogger;
import com.github.nosan.embedded.cassandra.commons.web.JdkHttpClient;
import com.github.nosan.embedded.cassandra.cql.CqlDataSet;
import com.github.nosan.embedded.cassandra.cql.CqlScript;

/**
 * Cassandra examples.
 *
 * @author Dmytro Nosan
 */
public class CassandraExamples {

	//tag::start-shared-cassandra[]
	@BeforeAll
	public static void startCassandra() {
		SharedCassandra.start();
	}
	//end::start-shared-cassandra[]

	private void workingDirectoryInitializer() {
		//tag::working-directory-initializer[]
		new CassandraBuilder()
				.workingDirectoryInitializer(new WorkingDirectoryInitializer() {

					@Override
					public void init(Path workingDirectory, Version version) throws IOException {
						//Custom logic
					}
				})
				.build();
		//end::working-directory-initializer[]

		//tag::working-directory-initializer-skip-existing[]
		new CassandraBuilder()
				.workingDirectoryInitializer(new DefaultWorkingDirectoryInitializer(new WebCassandraDirectoryProvider(),
						DefaultWorkingDirectoryInitializer.CopyStrategy.SKIP_EXISTING))
				.build();
		//end::working-directory-initializer-skip-existing[]
	}

	private void workingDirectory() {
		//tag::working-directory[]
		new CassandraBuilder()
				.workingDirectory(() -> Files.createTempDirectory("apache-cassandra-"))
				.build();
		//end::working-directory[]
	}

	private void customClasspath() {
		//tag::custom-classpath[]
		new CassandraBuilder()
				.addWorkingDirectoryResource(new ClassPathResource("lib.jar"), "lib/lib.jar")
				.build();
		//end::custom-classpath[]
	}

	private void quickStart() {
		// tag::quick-start[]
		Cassandra cassandra = new CassandraBuilder().build();
		cassandra.start();
		try {
			Settings settings = cassandra.getSettings();
			try (CqlSession session = CqlSession.builder()
					.addContactPoint(new InetSocketAddress(settings.getAddress(), settings.getPort()))
					.withLocalDatacenter("datacenter1")
					.build()) {
				CqlScript.ofClassPath("schema.cql").forEachStatement(session::execute);
			}
		}
		finally {
			cassandra.stop();
		}
		// end::quick-start[]
	}

	private void version() {
		//tag::version[]
		new CassandraBuilder()
				.version("3.11.11")
				.build();
		//end::version[]
	}

	private void configFile() {
		//tag::config-file[]
		new CassandraBuilder()
				.configFile(new ClassPathResource("cassandra.yaml"))
				.build();
		//end::config-file[]
	}

	private void configProperties() {
		//tag::config-property[]
		new CassandraBuilder()
				.addConfigProperty("native_transport_port", 9000)
				.addConfigProperty("storage_port", 7000)
				.addConfigProperty("client_encryption_options.enabled", true)
				.build();
		//end::config-property[]
	}

	private void systemProperties() {
		//tag::system-property[]
		new CassandraBuilder()
				.addSystemProperty("cassandra.native_transport_port", 9042)
				.addSystemProperty("cassandra.jmx.local.port", 7199)
				.build();
		//end::system-property[]
	}

	private void environmentVariables() {
		//tag::environment-variable[]
		new CassandraBuilder()
				.addEnvironmentVariable("JAVA_HOME", System.getProperty("java.home"))
				.build();
		//end::environment-variable[]
	}

	private void jvmOptions() {
		//tag::jvm-options[]
		new CassandraBuilder()
				.addJvmOptions("-Xmx512m")
				.build();
		//end::jvm-options[]
	}

	private void clientEncryptionOptions() {
		//tag::client-encryption-options[]
		ClassPathResource keystore = new ClassPathResource("server.keystore");
		ClassPathResource truststore = new ClassPathResource("server.truststore");
		new CassandraBuilder()
				.addWorkingDirectoryResource(keystore, "conf/server.keystore")
				.addWorkingDirectoryResource(truststore, "conf/server.truststore")
				.addConfigProperty("client_encryption_options.enabled", true)
				.addConfigProperty("client_encryption_options.require_client_auth", true)
				.addConfigProperty("client_encryption_options.optional", false)
				.addConfigProperty("client_encryption_options.keystore", "conf/server.keystore")
				.addConfigProperty("client_encryption_options.truststore", "conf/server.truststore")
				.addConfigProperty("client_encryption_options.keystore_password", "123456")
				.addConfigProperty("client_encryption_options.truststore_password", "123456")
				// Use a dedicated SSL port if necessary
				.addConfigProperty("native_transport_port_ssl", 9142)
				.build();
		//end::client-encryption-options[]
	}

	private void authenticator() {
		//tag::authenticator[]
		new CassandraBuilder()
				.addConfigProperty("authenticator", "PasswordAuthenticator")
				.addConfigProperty("authorizer", "CassandraAuthorizer")
				.addSystemProperty("cassandra.superuser_setup_delay_ms", 0) //<1>
				.build();
		//end::authenticator[]
	}

	private void randomPorts() {
		//tag::random-ports[]
		new CassandraBuilder()
				.addSystemProperty("cassandra.native_transport_port", 0)
				.addSystemProperty("cassandra.rpc_port", 0)
				.addSystemProperty("cassandra.storage_port", 0)
				.addSystemProperty("cassandra.jmx.local.port", 0)
				//for Cassandra 4.x.x
				.configure(new SimpleSeedProviderConfigurator("localhost:0"))
				.build();
		//end::random-ports[]
	}

	private void seeds() {
		//tag::configure-seeds[]
		new CassandraBuilder()
				.configure(new SimpleSeedProviderConfigurator()
						.addSeeds("localhost", "127.0.0.1")
						//for Cassandra 4.x.x
						.addSeed("localhost", 7199)
						.addSeed("localhost", 0)) //<1>
				.build();
		//end::configure-seeds[]
	}

	private void startupTimeout() {
		//tag::startup-timeout[]
		new CassandraBuilder()
				.startupTimeout(Duration.ofMinutes(1))
				.build();
		//end::startup-timeout[]
	}

	private void proxy() {
		//tag::proxy[]
		new CassandraBuilder()
				.workingDirectoryInitializer(
						new DefaultWorkingDirectoryInitializer(new WebCassandraDirectoryProvider(
								new JdkHttpClient(Proxy.NO_PROXY))))
				.build();
		//end::proxy[]
	}

	private void shutdownHook() {
		//tag::shutdown-hook[]
		new CassandraBuilder()
				.registerShutdownHook(true)
				.build();
		//end::shutdown-hook[]
	}

	private void logger() {
		//tag::logger[]
		new CassandraBuilder()
				//Automatically detects logging implementation. Either slf4j or console.
				.logger(Logger.get("Cassandra"))
				//Use SLF4J Logger implementation
				.logger(new Slf4jLogger(LoggerFactory.getLogger("Cassandra")))
				//Use Console implementation.
				.logger(new ConsoleLogger("Cassandra"))
				.build();
		//end::logger[]
	}

	private void workingDirectoryCustomizer() {
		//tag::working-directory-customizer[]
		new CassandraBuilder()
				.addWorkingDirectoryCustomizers(new WorkingDirectoryCustomizer() {

					@Override
					public void customize(Path workingDirectory, Version version) throws IOException {
						//Custom logic
					}
				}).build();
		//end::working-directory-customizer[]
	}

	private void workingDirectoryDestroyer() {
		//tag::working-directory-destroyer[]
		new CassandraBuilder()
				.workingDirectoryDestroyer(new WorkingDirectoryDestroyer() {

					@Override
					public void destroy(Path workingDirectory, Version version) throws IOException {
						//Custom logic
					}
				})
				.build();
		//end::working-directory-destroyer[]

		//tag::working-directory-destroyer-nothing[]
		new CassandraBuilder()
				.workingDirectoryDestroyer(WorkingDirectoryDestroyer.doNothing())
				.build();
		//end::working-directory-destroyer-nothing[]

		//tag::working-directory-destroyer-all[]
		new CassandraBuilder()
				.workingDirectoryDestroyer(WorkingDirectoryDestroyer.deleteAll())
				.build();
		//end::working-directory-destroyer-all[]
	}

	private void addWorkingDirectoryResource() {
		//tag::add-resource[]
		new CassandraBuilder()
				.addWorkingDirectoryResource(new ClassPathResource("cassandra-rackdc.properties"),
						"conf/cassandra-rackdc.properties");
		//end::add-resource[]
	}

	private void cqlStatements() {
		CqlSession session = null;
		//tag::cql[]

		CqlScript.ofClassPath("schema.cql").forEachStatement(session::execute);

		CqlDataSet.ofClassPaths("schema.cql", "V1__table.cql", "V2__table.cql").forEachStatement(session::execute);

		CqlScript.ofResource(new FileSystemResource(new File("schema.cql"))).forEachStatement(session::execute);

		CqlDataSet.ofResources(new FileSystemResource(new File("schema.cql")),
				new FileSystemResource(new File("V1__table.cql"))).forEachStatement(session::execute);

		//end::cql[]
	}

}
