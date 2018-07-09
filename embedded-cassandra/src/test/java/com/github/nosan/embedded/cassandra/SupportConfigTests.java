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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SupportConfig}.
 *
 * @author Dmytro Nosan
 */
public class SupportConfigTests {

	private final SupportConfig supportConfig = new SupportConfig();

	@Test
	public void getName() {
		assertThat(this.supportConfig.getName()).isEqualTo("Embedded Cassandra");
	}

	@Test
	public void getSupportUrl() {
		assertThat(this.supportConfig.getSupportUrl())
				.isEqualTo("https://github.com/nosan/embedded-cassandra");
	}

	@Test
	public void messageOnException() {
		assertThat(this.supportConfig.messageOnException(getClass(),
				new RuntimeException("ex"))).isEqualTo(
				"If you feel this is a bug, please open a new issue. "
						+ "Follow this link: https://github.com/nosan/embedded-cassandra\n"
						+ "Thank you! :)");
	}

}
