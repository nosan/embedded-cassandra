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

package com.github.nosan.embedded.cassandra;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.CassandraException;
import com.github.nosan.embedded.cassandra.api.CassandraInterruptedException;
import com.github.nosan.embedded.cassandra.api.Version;
import com.github.nosan.embedded.cassandra.commons.util.FileUtils;

/**
 * Embedded {@link Cassandra}.
 *
 * @author Dmytro Nosan
 */
class EmbeddedCassandra implements Cassandra {

	private static final Logger log = LoggerFactory.getLogger(EmbeddedCassandra.class);

	private static final String VERSION_PROPERTY = "embedded.cassandra.version";

	private static final String ADDRESS_PROPERTY = "embedded.cassandra.address";

	private static final String PORT_PROPERTY = "embedded.cassandra.port";

	private static final String RPC_PORT_PROPERTY = "embedded.cassandra.rpc-port";

	private static final String SSL_PORT_PROPERTY = "embedded.cassandra.ssl-port";

	private final String name;

	private final boolean exposeProperties;

	private final Path artifactDirectory;

	private final Path workingDirectory;

	private final Version version;

	private final Database database;

	private volatile boolean started = false;

	EmbeddedCassandra(String name, boolean exposeProperties, Path artifactDirectory, Path workingDirectory,
			Version version, Database database) {
		this.name = name;
		this.exposeProperties = exposeProperties;
		this.artifactDirectory = artifactDirectory;
		this.workingDirectory = workingDirectory;
		this.version = version;
		this.database = database;
	}

	@Override
	public synchronized void start() {
		if (this.started) {
			return;
		}
		try {
			doStart();
		}
		catch (CassandraException ex) {
			try {
				doStop();
			}
			catch (CassandraException swallow) {
				ex.addSuppressed(swallow);
			}
			throw ex;
		}
		if (this.exposeProperties) {
			setSystemProperties();
		}
		this.started = true;
	}

	@Override
	public synchronized void stop() {
		if (!this.started) {
			return;
		}
		doStop();
		if (this.exposeProperties) {
			clearSystemProperties();
		}
		this.started = false;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Version getVersion() {
		return this.version;
	}

	@Override
	@Nullable
	public InetAddress getAddress() {
		if (this.started) {
			return this.database.getAddress();
		}
		return null;
	}

	@Override
	public int getPort() {
		if (this.started) {
			return this.database.getPort();
		}
		return -1;
	}

	@Override
	public int getSslPort() {
		if (this.started) {
			return this.database.getSslPort();
		}
		return -1;
	}

	@Override
	public int getRpcPort() {
		if (this.started) {
			return this.database.getRpcPort();
		}
		return -1;
	}

	@Override
	public String toString() {
		return String.format("Embedded Cassandra (name='%s' version='%s')", getName(), getVersion());
	}

	private void doStart() {
		try {
			initialize();
			this.database.start();
		}
		catch (InterruptedException ex) {
			throw new CassandraInterruptedException("Cassandra start interrupted", ex);
		}
		catch (Exception ex) {
			throw new CassandraException("Unable to start " + toString(), ex);
		}
	}

	private void initialize() throws IOException {
		log.info("Initializes {}. It takes a while...", toString());
		Files.createDirectories(this.workingDirectory);
		FileUtils.copy(this.artifactDirectory, this.workingDirectory, (path, attributes) -> {
			if (attributes.isDirectory()) {
				String name = path.getFileName().toString().toLowerCase(Locale.ENGLISH);
				return !name.equals("javadoc") && !name.equals("doc");
			}
			return true;
		});
	}

	private void doStop() {
		try {
			this.database.stop();
			destroy();
		}
		catch (InterruptedException ex) {
			throw new CassandraInterruptedException("Cassandra stop interrupted", ex);
		}
		catch (Exception ex) {
			throw new CassandraException("Unable to stop " + toString(), ex);
		}
	}

	private void destroy() throws IOException {
		if (FileUtils.delete(this.workingDirectory)) {
			log.info("The working directory '{}' has been deleted", this.workingDirectory);
		}
	}

	private void setSystemProperties() {
		setSystemProperty(VERSION_PROPERTY, getVersion());
		InetAddress address = this.database.getAddress();
		if (address != null) {
			setSystemProperty(ADDRESS_PROPERTY, address.getHostAddress());
		}
		int port = this.database.getPort();
		if (port != -1) {
			setSystemProperty(PORT_PROPERTY, port);
		}
		int sslPort = this.database.getSslPort();
		if (sslPort != -1) {
			setSystemProperty(SSL_PORT_PROPERTY, sslPort);
		}
		int rpcPort = this.database.getRpcPort();
		if (rpcPort != -1) {
			setSystemProperty(RPC_PORT_PROPERTY, rpcPort);
		}
	}

	private void clearSystemProperties() {
		clearSystemProperty(VERSION_PROPERTY);
		clearSystemProperty(ADDRESS_PROPERTY);
		clearSystemProperty(PORT_PROPERTY);
		clearSystemProperty(SSL_PORT_PROPERTY);
		clearSystemProperty(RPC_PORT_PROPERTY);
	}

	private static void setSystemProperty(String name, Object value) {
		try {
			System.setProperty(name, value.toString());
		}
		catch (Exception ex) {
			log.error(String.format("System Property '%s' cannot be set", name), ex);
		}
	}

	private static void clearSystemProperty(String name) {
		try {
			System.clearProperty(name);
		}
		catch (Exception ex) {
			log.error(String.format("System Property '%s' cannot be deleted", name), ex);
		}
	}

}
