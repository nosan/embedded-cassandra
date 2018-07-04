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

package com.github.nosan.embedded.cassandra.jupiter;

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.config.ExecutableConfig;
import com.github.nosan.embedded.cassandra.config.ExecutableConfigBuilder;
import com.github.nosan.embedded.cassandra.cql.CqlScriptUtils;

/**
 * JUnit {@link org.junit.jupiter.api.extension.RegisterExtension RegisterExtension} for
 * running an Embedded Cassandra. Cassandra will be started on the random ports.
 * <pre> public class CassandraTests {
 * 	&#64;RegisterExtension
 * 	public static CassandraExtension cassandra = new CassandraExtension();
 * 	&#64;BeforeEach
 * 	public void setUp() throws Exception {
 * 		CqlScriptUtils.executeScripts(cassandra.getSession(), "init.cql");
 *    }
 * 	&#64;AfterEach
 * 	public void tearDown() throws Exception {
 * 		CqlScriptUtils.executeScripts(cassandra.getSession(), "drop.cql");
 *    }
 * 	&#64;Test
 * 	public void test() {
 * 		//test me
 *    }
 * }</pre>
 *
 * @author Dmytro Nosan
 * @see com.github.nosan.embedded.cassandra.config.RuntimeConfigBuilder
 * @see ExecutableConfigBuilder
 * @see Cassandra
 * @see CqlScriptUtils
 */
public class CassandraExtension extends Cassandra
		implements BeforeAllCallback, AfterAllCallback {

	public CassandraExtension(IRuntimeConfig runtimeConfig,
			ExecutableConfig executableConfig) {
		super(runtimeConfig, executableConfig);
	}

	public CassandraExtension(IRuntimeConfig runtimeConfig) {
		super(runtimeConfig, new ExecutableConfigBuilder().useRandomPorts(true).build());
	}

	public CassandraExtension(ExecutableConfig executableConfig) {
		super(executableConfig);
	}

	public CassandraExtension() {
		this(new ExecutableConfigBuilder().useRandomPorts(true).build());
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		start();
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		stop();
	}

}
