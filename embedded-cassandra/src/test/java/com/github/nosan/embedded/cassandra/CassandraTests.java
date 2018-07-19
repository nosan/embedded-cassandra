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

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.nosan.embedded.cassandra.cql.ClassPathCqlScript;
import com.github.nosan.embedded.cassandra.cql.CqlScriptUtils;
import com.github.nosan.embedded.cassandra.support.ExecutableConfigBuilder;

/**
 * Tests for {@link Cassandra}.
 *
 * @author Dmytro Nosan
 */
public class CassandraTests {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void shouldStartCassandraUsingDefaultConfiguration() throws Exception {
		Cassandra cassandra =
				new Cassandra(new ExecutableConfigBuilder().jvmOptions(new JvmOptions("-Xmx256")).build());
		invoke(cassandra,
				() -> CqlScriptUtils.executeScripts(cassandra.getSession(), new ClassPathCqlScript("init.cql")));
	}

	@Test
	public void shouldNotStartCassandraIfCassandraHasBeenAlreadyStarted()
			throws Exception {
		this.expectedException.expect(IOException.class);
		this.expectedException.expectMessage("Cassandra has already been started");
		Cassandra cassandra =
				new Cassandra(new ExecutableConfigBuilder().jvmOptions(new JvmOptions("-Xmx256")).build());
		invoke(cassandra, cassandra::start);
	}

	@Test
	public void shouldBeAbleToRestartCassandra() throws Exception {
		Cassandra cassandra =
				new Cassandra(new ExecutableConfigBuilder().jvmOptions(new JvmOptions("-Xmx256")).build());
		invoke(cassandra);
		invoke(cassandra);
	}


	private static void invoke(Cassandra cassandra) throws Exception {
		try {
			cassandra.start();
		}
		finally {
			cassandra.stop();
		}
	}

	private static void invoke(Cassandra cassandra, Callback callback) throws Exception {
		try {
			cassandra.start();
			callback.run();
		}
		finally {
			cassandra.stop();
		}
	}

	interface Callback {

		void run() throws Exception;

	}

}
