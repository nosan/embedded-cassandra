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

package com.github.nosan.embedded.cassandra.process;

import java.util.Arrays;
import java.util.List;

import de.flapdoodle.embed.process.io.NullProcessor;
import org.junit.Test;

import com.github.nosan.embedded.cassandra.Config;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CassandraProcess.LogWatch}.
 *
 * @author Dmytro Nosan
 */
public class LogWatchTests {


	@Test
	public void cqlListening() {
		CassandraProcess.LogWatch logWatch = new CassandraProcess.LogWatch(new Config(), new NullProcessor());
		logWatch.process("Starting listening for CQL");
		assertThat(logWatch.isInitWithSuccess()).isTrue();
	}

	@Test
	public void rpcListening() {
		Config config = new Config();
		config.setStartNativeTransport(false);
		config.setStartRpc(true);
		CassandraProcess.LogWatch logWatch = new CassandraProcess.LogWatch(config, new NullProcessor());
		logWatch.process("Listening for thrift clients");
		assertThat(logWatch.isInitWithSuccess()).isTrue();
	}

	@Test
	public void messagingListening() {
		Config config = new Config();
		config.setStartNativeTransport(false);
		config.setStartRpc(false);
		CassandraProcess.LogWatch logWatch = new CassandraProcess.LogWatch(config, new NullProcessor());
		logWatch.process("Starting Messaging Service");
		assertThat(logWatch.isInitWithSuccess()).isTrue();
	}

	@Test
	public void errors() {
		List<String> errors = Arrays.asList("encountered during startup",
				"Missing required", "Address already in use", "Port already in use",
				"ConfigurationException", "syntax error near unexpected",
				"Error occurred during initialization",
				"Cassandra 3.0 and later require Java");

		for (String error : errors) {
			CassandraProcess.LogWatch logWatch = new CassandraProcess.LogWatch(new Config(), new NullProcessor());
			logWatch.process(error);
			assertThat(logWatch.getFailureFound()).isEqualTo(error);

		}

	}
}
