/*
 * Copyright 2020 the original author or authors.
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

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
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
import com.github.nosan.embedded.cassandra.commons.UrlResource;
import com.github.nosan.embedded.cassandra.commons.logging.ConsoleLogger;
import com.github.nosan.embedded.cassandra.commons.logging.Logger;
import com.github.nosan.embedded.cassandra.commons.logging.Slf4jLogger;
import com.github.nosan.embedded.cassandra.commons.web.JdkHttpClient;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.cql.ResourceCqlScript;
import com.github.nosan.embedded.cassandra.cql.StringCqlScript;

/**
 * Cassandra examples.
 *
 * @author Dmytro Nosan
 */
public class CassandraExamples {

	private void examples() throws Exception {
		// tag::quick-start[]
		Cassandra cassandra = new CassandraBuilder().build();
		cassandra.start();
		try {
			Settings settings = cassandra.getSettings();

			try (Cluster cluster = Cluster.builder().addContactPoints(settings.getAddress())
					.withPort(settings.getPort()).build()) {
				Session session = cluster.connect();
				CqlScript.ofClassPath("schema.cql").forEachStatement(session::execute);
			}
		}
		finally {
			cassandra.stop();
		}
		// end::quick-start[]

		//tag::version[]
		new CassandraBuilder()
				.version("3.11.9")
				.build();
		//end::version[]

		//tag::config-file-system-property[]
		new CassandraBuilder()
				.addSystemProperty("cassandra.config", new ClassPathResource("cassandra.yaml"))
				.build();
		//end::config-file-system-property[]

		//tag::config-property[]
		new CassandraBuilder()
				.addConfigProperty("native_transport_port", 9042)
				.addConfigProperty("storage_port", 7000)
				.build();
		//end::config-property[]

		//tag::system-property[]
		new CassandraBuilder()
				.addSystemProperty("cassandra.native_transport_port", 9042)
				.addSystemProperty("cassandra.jmx.local.port", 7199)
				.build();
		//end::system-property[]

		//tag::environment-variable[]
		new CassandraBuilder()
				.addEnvironmentVariable("JAVA_HOME", System.getProperty("java.home"))
				.addEnvironmentVariable("EXTRA_CLASSPATH", new ClassPathResource("lib.jar"))
				.build();
		//end::environment-variable[]

		//tag::jvm-options[]
		new CassandraBuilder()
				.addJvmOptions("-Xmx512m")
				.build();
		//end::jvm-options[]
		//tag::client-encryption-options[]
		ClassPathResource keystore = new ClassPathResource("keystore.node0");
		ClassPathResource truststore = new ClassPathResource("truststore.node0");
		new CassandraBuilder()
				.addConfigProperty("native_transport_port_ssl", 9142)
				.addWorkingDirectoryCustomizers(WorkingDirectoryCustomizer.copy(keystore, "conf/.keystore"))
				.addWorkingDirectoryCustomizers(WorkingDirectoryCustomizer.copy(truststore, "conf/.truststore"))
				.addConfigProperty("client_encryption_options.enabled", true)
				.addConfigProperty("client_encryption_options.require_client_auth", true)
				.addConfigProperty("client_encryption_options.optional", false)
				.addConfigProperty("client_encryption_options.keystore", "conf/.keystore")
				.addConfigProperty("client_encryption_options.truststore", "conf/.truststore")
				.addConfigProperty("client_encryption_options.keystore_password", "cassandra")
				.addConfigProperty("client_encryption_options.truststore_password", "cassandra")
				// Use a dedicated SSL port if necessary
				.addConfigProperty("native_transport_port_ssl", 9142)
				.build();
		//end::client-encryption-options[]

		//tag::authenticator[]
		new CassandraBuilder()
				.addConfigProperty("authenticator", "PasswordAuthenticator")
				.addConfigProperty("authorizer", "CassandraAuthorizer")
				.addSystemProperty("cassandra.superuser_setup_delay_ms", 0) //<1>
				.build();
		//end::authenticator[]

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

		//tag::configure-seeds[]
		new CassandraBuilder()
				.configure(new SimpleSeedProviderConfigurator()
						.addSeeds("localhost", "127.0.0.1")
						//for Cassandra 4.x.x
						.addSeed("localhost", 7199)
						.addSeed("localhost", 0)) //<1>
				.build();
		//end::configure-seeds[]
		//tag::startup-timeout[]
		new CassandraBuilder()
				.startupTimeout(Duration.ofMinutes(1))
				.build();
		//end::startup-timeout[]
		//tag::proxy[]
		new CassandraBuilder()
				.workingDirectoryInitializer(
						new DefaultWorkingDirectoryInitializer(new WebCassandraDirectoryProvider(
								new JdkHttpClient(Proxy.NO_PROXY))))
				.build();
		//end::proxy[]

		//tag::cql[]
		CqlScript.ofClassPath("schema.cql");
		new ResourceCqlScript(new UrlResource(new URL("")));
		new StringCqlScript("CQL SCRIPT");
		//end::cql[]

		//tag::shutdown-hook[]
		new CassandraBuilder()
				.registerShutdownHook(true)
				.build();
		//end::shutdown-hook[]

		//tag::logger[]
		new CassandraBuilder()
				//Automatically detected logging implementation.
				.logger(Logger.get("Cassandra"))
				//Use SLF4J Logger implementation
				.logger(new Slf4jLogger(LoggerFactory.getLogger("Cassandra")))
				//Use Console implementation.
				.logger(new ConsoleLogger("Cassandra"))
				.build();
		//end::logger[]

		//tag::working-directory-customizer[]
		new CassandraBuilder()
				.addWorkingDirectoryCustomizers(new WorkingDirectoryCustomizer() {

					@Override
					public void customize(Path workingDirectory, Version version) throws IOException {
						//Custom logic
					}
				}).build();
		//end::working-directory-customizer[]
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

		//tag::working-directory[]
		new CassandraBuilder()
				//Use a temporary directory
				.workingDirectory(() -> Files.createTempDirectory("apache-cassandra-"))
				//Use the same directory
				.workingDirectory(() -> Paths.get("target/cassandra"))
				.build();

		//end::working-directory[]

	}

}
