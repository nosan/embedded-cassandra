/*
 * Copyright 2020-2024 the original author or authors.
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

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraBuilder;
import com.github.nosan.embedded.cassandra.Settings;

/**
 * Global Cassandra Example.
 *
 * @author Dmytro Nosan
 */
//tag::shared-cassandra[]
public final class SharedCassandra {

	private static final Cassandra CASSANDRA = new CassandraBuilder()
			//			... additional configuration
			.registerShutdownHook(true) //<1>
			.build();

	private SharedCassandra() {
	}

	public static synchronized void start() {
		CASSANDRA.start();
	}

	public static synchronized void stop() {
		CASSANDRA.stop();
	}

	public static synchronized boolean isRunning() {
		return CASSANDRA.isRunning();
	}

	public static synchronized Settings getSettings() {
		return CASSANDRA.getSettings();
	}

}
//end::shared-cassandra[]

