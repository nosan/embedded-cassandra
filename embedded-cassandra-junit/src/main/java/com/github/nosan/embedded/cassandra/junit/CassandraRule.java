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
import com.github.nosan.embedded.cassandra.CassandraConfig;
import com.github.nosan.embedded.cassandra.support.CassandraConfigBuilder;
import com.github.nosan.embedded.cassandra.support.RuntimeConfigBuilder;

/**
 * JUnit {@link TestRule TestRule} for running an Embedded Cassandra. Cassandra will be
 * started on the random ports. <pre>
 *     public class CassandraTests {
 * 	 &#64;ClassRule
 * 	public static CassandraRule cassandra = new CassandraRule();
 *
 * 	 &#64;Before
 * 	public void setUp() throws Exception {
 * 	 //before actions
 *    }
 * 	 &#64;After
 * 	public void tearDown() throws Exception {
 * 	 //after actions
 *    }
 * 	 &#64;Test
 * 	public void test() {
 * 	//test me
 *    }
 *    }
 * }</pre>
 *
 * @author Dmytro Nosan
 * @see RuntimeConfigBuilder
 * @see CassandraConfigBuilder
 * @see Cassandra
 */
public class CassandraRule extends Cassandra implements TestRule {

	public CassandraRule(IRuntimeConfig runtimeConfig, CassandraConfig cassandraConfig) {
		super(runtimeConfig, cassandraConfig);
	}

	public CassandraRule(IRuntimeConfig runtimeConfig) {
		super(runtimeConfig, new CassandraConfigBuilder().useRandomPorts(true).build());
	}

	public CassandraRule(CassandraConfig cassandraConfig) {
		super(cassandraConfig);
	}

	public CassandraRule() {
		this(new CassandraConfigBuilder().useRandomPorts(true).build());
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				start();
				try {
					base.evaluate();
				}
				finally {
					stop();
				}
			}
		};
	}

}
