/*
 * Copyright 2018-2019 the original author or authors.
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

package com.github.nosan.embedded.cassandra.local;

import org.junit.Test;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.test.support.DisableIfOS;

/**
 * Tests for {@link LocalCassandra}.
 *
 * @author Dmytro Nosan
 */
public class LocalCassandra_V_2_1_X_Tests extends AbstractLocalCassandraTests {

	public LocalCassandra_V_2_1_X_Tests() {
		super(new Version(2, 1, 21));
	}

	@Override
	@Test
	@DisableIfOS("windows")
	public void shouldRunOnInterfaceIPV4() throws Exception {
		super.shouldRunOnInterfaceIPV4();
	}

	@Override
	@Test
	@DisableIfOS("windows")
	public void shouldRunOnInterfaceIPV6() throws Exception {
		super.shouldRunOnInterfaceIPV6();
	}
}
