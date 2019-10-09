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

import java.net.InetAddress;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.CassandraException;
import com.github.nosan.embedded.cassandra.api.CassandraInterruptedException;
import com.github.nosan.embedded.cassandra.api.Version;

/**
 * Embedded {@link Cassandra}.
 *
 * @author Dmytro Nosan
 */
class EmbeddedCassandra implements Cassandra {

	private static final Logger log = LoggerFactory.getLogger(EmbeddedCassandra.class);

	private final String name;

	private final Version version;

	private final Database database;

	private volatile boolean started = false;

	private volatile boolean running = false;

	EmbeddedCassandra(String name, Version version, Database database) {
		this.name = name;
		this.version = version;
		this.database = database;
	}

	@Override
	public synchronized void start() {
		if (this.started) {
			return;
		}
		try {
			this.started = true;
			log.info("Starts {}", toString());
			doStart();
			this.running = true;
			log.info("{} has been started and ready for connections!", toString());
		}
		catch (CassandraException ex) {
			try {
				doStop();
				this.started = false;
			}
			catch (CassandraException swallow) {
				ex.addSuppressed(swallow);
			}
			throw ex;
		}
	}

	@Override
	public synchronized void stop() {
		if (!this.started) {
			return;
		}
		log.info("Stops {}", toString());
		doStop();
		log.info("{} has been stopped", toString());
		this.started = false;
		this.running = false;
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
		if (this.running) {
			return this.database.getAddress();
		}
		return null;
	}

	@Override
	public int getPort() {
		if (this.running) {
			return this.database.getPort();
		}
		return -1;
	}

	@Override
	public int getSslPort() {
		if (this.running) {
			return this.database.getSslPort();
		}
		return -1;
	}

	@Override
	public int getRpcPort() {
		if (this.running) {
			return this.database.getRpcPort();
		}
		return -1;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", EmbeddedCassandra.class.getSimpleName() + "[", "]")
				.add("name='" + this.name + "'")
				.add("version=" + this.version)
				.toString();
	}

	private void doStart() {
		try {
			this.database.start();
		}
		catch (InterruptedException ex) {
			throw new CassandraInterruptedException("Cassandra start interrupted", ex);
		}
		catch (Exception ex) {
			throw new CassandraException("Unable to start " + toString(), ex);
		}
	}

	private void doStop() {
		try {
			this.database.stop();
		}
		catch (InterruptedException ex) {
			throw new CassandraInterruptedException("Cassandra stop interrupted", ex);
		}
		catch (Exception ex) {
			throw new CassandraException("Unable to stop " + toString(), ex);
		}
	}

}
