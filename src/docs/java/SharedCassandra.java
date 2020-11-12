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

import java.nio.file.Path;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraBuilder;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;

//tag::shared-cassandra[]
public final class SharedCassandra {

	private static final Object lock = new Object();

	private static volatile Cassandra instance;

	private SharedCassandra() {
	}

	public static void start() {
		getOrCreate().start();
	}

	public static void stop() {
		getOrCreate().stop();
	}

	public static boolean isRunning() {
		return getOrCreate().isRunning();
	}

	public static String getName() {
		return getOrCreate().getName();
	}

	public static Version getVersion() {
		return getOrCreate().getVersion();
	}

	public static Path getWorkingDirectory() {
		return getOrCreate().getWorkingDirectory();
	}

	public static Settings getSettings() {
		return getOrCreate().getSettings();
	}

	private static Cassandra getOrCreate() {
		Cassandra cassandra = instance;
		if (cassandra == null) {
			synchronized (lock) {
				cassandra = instance;
				if (cassandra == null) {
					cassandra = new CassandraBuilder()
							.registerShutdownHook(true) //<1>
							.build();
					instance = cassandra;
				}
			}
		}
		return cassandra;
	}

}
//end::shared-cassandra[]

