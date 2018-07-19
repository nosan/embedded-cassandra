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

package com.github.nosan.embedded.cassandra.junit;

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.ClusterFactory;
import com.github.nosan.embedded.cassandra.ExecutableConfig;
import com.github.nosan.embedded.cassandra.support.ExecutableConfigBuilder;
import com.github.nosan.embedded.cassandra.support.RuntimeConfigBuilder;

/**
 * JUnit {@link TestRule TestRule} for running an Embedded Cassandra. Cassandra will be
 * started on the random ports. <pre>
 * public class CassandraRuleTests {
 * &#64;ClassRule
 * public static CassandraRule cassandra = new CassandraRule();
 * &#64;Before
 * public void setUp() {
 * 		 CqlScriptUtils.executeScripts(cassandra.getSession(), new ClassPathCqlScript("init.cql"));
 * }
 * &#64;Test
 * public void select() {
 * assertThat(cassandra.getSession().execute(...).wasApplied())
 * .isTrue();
 * }
 * }
 * </pre>
 *
 * @author Dmytro Nosan
 * @see RuntimeConfigBuilder
 * @see ExecutableConfigBuilder
 * @see Cassandra
 */
public class CassandraRule extends Cassandra implements TestRule {
	public CassandraRule(IRuntimeConfig runtimeConfig,
			ExecutableConfig executableConfig,
			ClusterFactory clusterFactory) {
		super(runtimeConfig, executableConfig, clusterFactory);
	}

	public CassandraRule(IRuntimeConfig runtimeConfig,
			ExecutableConfig executableConfig) {
		super(runtimeConfig, executableConfig);
	}

	public CassandraRule(IRuntimeConfig runtimeConfig) {
		super(runtimeConfig);
	}

	public CassandraRule(ExecutableConfig executableConfig) {
		super(executableConfig);
	}

	public CassandraRule(IRuntimeConfig runtimeConfig,
			ClusterFactory clusterFactory) {
		super(runtimeConfig, clusterFactory);
	}

	public CassandraRule(ExecutableConfig executableConfig,
			ClusterFactory clusterFactory) {
		super(executableConfig, clusterFactory);
	}

	public CassandraRule(ClusterFactory clusterFactory) {
		super(clusterFactory);
	}

	public CassandraRule() {
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				try {
					start();
					base.evaluate();
				}
				finally {
					stop();
				}
			}
		};
	}

}
