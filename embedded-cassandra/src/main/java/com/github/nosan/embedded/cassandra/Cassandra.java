/*
 * Copyright 2012-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;
import de.flapdoodle.embed.process.store.IArtifactStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.cql.CqlScriptUtils;
import com.github.nosan.embedded.cassandra.process.CassandraProcess;
import com.github.nosan.embedded.cassandra.support.ExecutableConfigBuilder;
import com.github.nosan.embedded.cassandra.support.RuntimeConfigBuilder;

/**
 * Simple class for running an Embedded Cassandra. <pre>public class CassandraTests {
 *        &#64;Test
 * 	public void test() throws IOException {
 * 		Cassandra cassandra = new Cassandra();
 * 		try {
 * 			cassandra.start();
 * 			CqlScriptUtils.executeScripts(cassandra.getSession(), new ClassPathCqlScript("init.cql"));
 * 			// test me
 *        }
 * 		finally {
 * 			cassandra.stop();
 *        }
 *    }
 * }</pre>
 *
 * @author Dmytro Nosan
 * @see RuntimeConfigBuilder
 * @see ExecutableConfigBuilder
 * @see ClusterFactory
 * @see CqlScriptUtils
 */
public class Cassandra {

	private static final Logger log = LoggerFactory.getLogger(Cassandra.class);


	private final ClusterFactory clusterFactory;

	private final IRuntimeConfig runtimeConfig;

	private final ExecutableConfig executableConfig;

	private volatile CassandraExecutable executable;

	private volatile Cluster cluster;

	private volatile Session session;

	public Cassandra(IRuntimeConfig runtimeConfig, ExecutableConfig executableConfig,
			ClusterFactory clusterFactory) {
		this.runtimeConfig = (runtimeConfig != null ? runtimeConfig : new RuntimeConfigBuilder(log).build());
		this.executableConfig = (executableConfig != null ? executableConfig : new ExecutableConfigBuilder().build());
		this.clusterFactory = (clusterFactory != null ? clusterFactory : new DefaultClusterFactory());
	}


	public Cassandra(IRuntimeConfig runtimeConfig, ExecutableConfig executableConfig) {
		this(runtimeConfig, executableConfig, null);
	}

	public Cassandra(IRuntimeConfig runtimeConfig) {
		this(runtimeConfig, null, null);
	}

	public Cassandra(ExecutableConfig executableConfig) {
		this(null, executableConfig, null);
	}

	public Cassandra(IRuntimeConfig runtimeConfig, ClusterFactory clusterFactory) {
		this(runtimeConfig, null, clusterFactory);
	}

	public Cassandra(ExecutableConfig executableConfig, ClusterFactory clusterFactory) {
		this(null, executableConfig, clusterFactory);
	}

	public Cassandra(ClusterFactory clusterFactory) {
		this(null, null, clusterFactory);
	}

	public Cassandra() {
		this(null, null, null);
	}

	/**
	 * Retrieves {@link ExecutableConfig Executable Config}.
	 *
	 * @return Executable Config.
	 */
	public ExecutableConfig getExecutableConfig() {
		return this.executableConfig;
	}

	/**
	 * Retrieves {@link IRuntimeConfig Runtime Config}.
	 *
	 * @return Runtime Config.
	 */
	public IRuntimeConfig getRuntimeConfig() {
		return this.runtimeConfig;
	}

	/**
	 * Retrieves {@link ClusterFactory Cluster Factory}.
	 *
	 * @return Cluster Factory to use.
	 */
	public ClusterFactory getClusterFactory() {
		return this.clusterFactory;
	}

	/**
	 * Lazy initialize Cassandra's {@link Cluster Cluster} using {@link ClusterFactory ClusterFactory}.
	 * Note! This method should be called only after {@link #start()} method.
	 *
	 * @return Cassandra's Cluster.
	 * @see ClusterFactory
	 */
	public Cluster getCluster() {
		if (this.cluster == null) {
			synchronized (this) {
				if (this.cluster == null) {
					ExecutableConfig executableConfig = getExecutableConfig();
					this.cluster = getClusterFactory()
							.getCluster(executableConfig.getConfig(), executableConfig.getVersion());
				}
			}
		}
		return this.cluster;
	}

	/**
	 * Lazy initialize Cassandra's {@link Session Session} using {@link #getCluster() Cluster}.
	 * Note! This method should be called only after {@link #start()} method.
	 *
	 * @return Cassandra's Session
	 * @see #getCluster()
	 */
	public Session getSession() {
		if (this.session == null) {
			synchronized (this) {
				if (this.session == null) {
					this.session = getCluster().connect();
				}
			}
		}
		return this.session;
	}


	/**
	 * Starts the Cassandra. {@code Session} and {@code Cluster} will not be initialized.
	 * This method registers a shutdown hook if {@link IRuntimeConfig#isDaemonProcess()} was set as a {@code true}.
	 *
	 * @throws UncheckedIOException Cassandra's process has not been started correctly.
	 * @throws IllegalStateException Cassandra has already been initialized.
	 */
	public void start() {
		if (this.executable == null) {
			synchronized (this) {
				if (this.executable == null) {
					IRuntimeConfig runtimeConfig = getRuntimeConfig();
					ExecutableConfig executableConfig = getExecutableConfig();
					if (runtimeConfig.isDaemonProcess()) {
						Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
					}
					try {
						CassandraStarter cassandraStarter = new CassandraStarter(runtimeConfig, executableConfig);
						this.executable = cassandraStarter.newExecutable();
						this.executable.start();
					}
					catch (IOException ex) {
						throw new UncheckedIOException(ex);
					}
				}
				else {
					throw new IllegalStateException("Cassandra has already been initialized");
				}
			}
		}
		else {
			throw new IllegalStateException("Cassandra has already been initialized");
		}
	}

	/**
	 * Stops the Cassandra and cleans/closes all related resources.
	 */
	public void stop() {
		synchronized (this) {
			close(this.session);
			close(this.cluster);
			close(this.executable);
			this.session = null;
			this.cluster = null;
			this.executable = null;
		}
	}

	private static void close(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		}
		catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}


	/**
	 * Simple class for executing {@link CassandraProcess}.
	 *
	 * @author Dmytro Nosan
	 * @see CassandraProcess
	 * @see CassandraStarter
	 * @see com.github.nosan.embedded.cassandra.Cassandra
	 */
	private static final class CassandraExecutable implements Closeable {

		private final CassandraProcess process;

		private final Distribution distribution;

		private final IRuntimeConfig runtime;

		private final IExtractedFileSet files;


		CassandraExecutable(Distribution distribution, ExecutableConfig executableConfig,
				IRuntimeConfig runtime, IExtractedFileSet files) {
			this.process = new CassandraProcess(distribution, executableConfig, runtime, files);
			this.distribution = distribution;
			this.runtime = runtime;
			this.files = files;
		}

		/**
		 * Start a cassandra process.
		 *
		 * @throws IOException Cassandra's process has not been started correctly.
		 */
		void start() throws IOException {
			this.process.start();
		}

		/**
		 * Stop Cassandra's process and cleans resources.
		 */
		@Override
		public void close() {
			this.process.stop();
			this.runtime.getArtifactStore().removeFileSet(this.distribution, this.files);
		}

	}

	/**
	 * Simple class for starting {@link CassandraExecutable}.
	 *
	 * @author Dmytro Nosan
	 * @see com.github.nosan.embedded.cassandra.process.CassandraProcess
	 * @see CassandraExecutable
	 * @see com.github.nosan.embedded.cassandra.Cassandra
	 */
	private static final class CassandraStarter {

		private final IRuntimeConfig runtimeConfig;

		private final ExecutableConfig executableConfig;

		CassandraStarter(IRuntimeConfig runtimeConfig, ExecutableConfig executableConfig) {
			this.runtimeConfig = runtimeConfig;
			this.executableConfig = executableConfig;
		}

		/**
		 * Creating a new {@link CassandraExecutable Executable}.
		 *
		 * @return {@code Executable} to execute a cassandra process.
		 * @throws IOException if an I/O error occurs.
		 */
		CassandraExecutable newExecutable() throws IOException {
			IArtifactStore artifactStore = this.runtimeConfig.getArtifactStore();
			Distribution distribution = Distribution.detectFor(this.executableConfig.version());
			if (artifactStore.checkDistribution(distribution)) {
				IExtractedFileSet files = artifactStore.extractFileSet(distribution);
				return new CassandraExecutable(distribution, this.executableConfig, this.runtimeConfig, files);
			}
			throw new IOException(
					String.format("Could not find a Distribution. Please check Artifact Store: '%s' and " +
							"Distribution: '%s'", artifactStore, distribution));

		}

	}
}
