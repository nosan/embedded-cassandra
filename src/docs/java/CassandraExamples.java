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
import com.github.nosan.embedded.cassandra.CassandraDirectoryProvider;
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

		//tag::config-file[]
		ClassPathResource configFile = new ClassPathResource("cassandra.yaml");

		//Configure via system property
		new CassandraBuilder()
				.addSystemProperty("cassandra.config", configFile)
				.build();

		//Configure via WorkingDirectoryCustomizer
		new CassandraBuilder()
				.addWorkingDirectoryCustomizers(WorkingDirectoryCustomizer.copy(configFile, "conf/cassandra.yaml"))
				.build();
		//end::config-file[]

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
				.build();
		//end::environment-variable[]

		//tag::custom-classpath[]
		//With environment variable
		ClassPathResource lib = new ClassPathResource("lib.jar");
		new CassandraBuilder()
				.addEnvironmentVariable("EXTRA_CLASSPATH", lib)
				.build();
		//With WorkingDirectoryCustomizer
		new CassandraBuilder()
				.addWorkingDirectoryCustomizers(WorkingDirectoryCustomizer.copy(lib, "lib/lib.jar"))
				.build();
		//end::custom-classpath[]

		//tag::java-home[]
		new CassandraBuilder()
				//use current java
				.addEnvironmentVariable("JAVA_HOME", System.getProperty("java.home"))
				//use java from JAVA_HOME
				.addEnvironmentVariable("JAVA_HOME", System.getenv("JAVA_HOME"))
				//use a custom path
				.addEnvironmentVariable("JAVA_HOME", Paths.get("path to java home"))
				.build();
		//end::java-home[]

		//tag::jvm-options[]
		new CassandraBuilder()
				.addJvmOptions("-Xmx512m")
				.build();
		//end::jvm-options[]
		//tag::client-encryption-options[]
		//Configure keystore and truststore with ClassPathResources (conveniently for tests)
		new CassandraBuilder()
				.addConfigProperty("client_encryption_options.enabled", true)
				.addConfigProperty("client_encryption_options.require_client_auth", true)
				.addConfigProperty("client_encryption_options.optional", false)
				.addConfigProperty("client_encryption_options.keystore", new ClassPathResource("keystore.node0"))
				.addConfigProperty("client_encryption_options.keystore_password", "cassandra")
				.addConfigProperty("client_encryption_options.truststore", new ClassPathResource("truststore.node0"))
				.addConfigProperty("client_encryption_options.truststore_password", "cassandra")
				// use a dedicated ssl port if necessary
				.addConfigProperty("native_transport_port_ssl", 9142)
				.build();
		//Configure keystore and truststore with WorkingDirectoryCustomizers
		WorkingDirectoryCustomizer keystoreCustomizer = WorkingDirectoryCustomizer
				.copy(new ClassPathResource("keystore.node0"), "conf/.keystore");
		WorkingDirectoryCustomizer truststoreCustomizer = WorkingDirectoryCustomizer
				.copy(new ClassPathResource("truststore.node0"), "conf/.truststore");
		new CassandraBuilder()
				.addConfigProperty("client_encryption_options.enabled", true)
				.addConfigProperty("client_encryption_options.require_client_auth", true)
				.addConfigProperty("client_encryption_options.optional", false)
				.addConfigProperty("client_encryption_options.keystore_password", "cassandra")
				.addConfigProperty("client_encryption_options.truststore_password", "cassandra")
				.addWorkingDirectoryCustomizers(keystoreCustomizer, truststoreCustomizer)
				// use a dedicated ssl port if necessary
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
				//automatically detected logging implementation
				.logger(Logger.get("Cassandra"))
				//use slf4j logger
				.logger(new Slf4jLogger(LoggerFactory.getLogger("Cassandra")))
				//use console logger
				.logger(new ConsoleLogger("Cassandra"))
				.build();
		//end::logger[]

		//tag::working-directory-customizer[]
		new CassandraBuilder()
				.addWorkingDirectoryCustomizers(new WorkingDirectoryCustomizer() {

					@Override
					public void customize(Path workingDirectory, Version version) throws IOException {
						//do whatever you want with a working directory
					}
				}).build();
		//end::working-directory-customizer[]
		//tag::working-directory-destroyer[]
		new CassandraBuilder()
				//completely deletes working directory on shutdown
				.workingDirectoryDestroyer(WorkingDirectoryDestroyer.deleteAll())
				//Deletes only 'lib' and 'pylib' folders on shutdown
				.workingDirectoryDestroyer(WorkingDirectoryDestroyer.deleteOnly("lib", "pylib"))
				//Deletes nothing
				.workingDirectoryDestroyer(WorkingDirectoryDestroyer.doNothing())
				//Custom implementation
				.workingDirectoryDestroyer(new WorkingDirectoryDestroyer() {

					@Override
					public void destroy(Path workingDirectory, Version version) throws IOException {
						//custom logic
					}
				})
				.build();
		//end::working-directory-destroyer[]

		//tag::working-directory-initializer[]
		new CassandraBuilder()
				//Use default initializer which gets Cassandra directory from a given provider
				.workingDirectoryInitializer(
						new DefaultWorkingDirectoryInitializer(new WebCassandraDirectoryProvider()))
				//Use a custom directory provider
				.workingDirectoryInitializer(new DefaultWorkingDirectoryInitializer(new CassandraDirectoryProvider() {

					@Override
					public Path getDirectory(Version version) throws IOException {
						//returns a path to Cassandra Directory. This directory will never be modified
						//it is only used for initializing working directory
						return Paths.get("Path to Cassandra Directory");
					}
				}))
				.workingDirectoryInitializer(new WorkingDirectoryInitializer() {

					@Override
					public void init(Path workingDirectory, Version version) throws IOException {
						//custom logic for working directory initializing.
						//for example here, we can initialize working directory from an archive or other sources.
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
