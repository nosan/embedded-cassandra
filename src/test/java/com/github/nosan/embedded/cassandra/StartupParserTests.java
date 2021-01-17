/*
 * Copyright 2020-2021 the original author or authors.
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StartupParserTests {

	private final Process.Output stdout = Mockito.mock(Process.Output.class);

	private final CassandraDatabase database = Mockito.mock(CassandraDatabase.class);

	@BeforeEach
	void setUp() {
		when(this.database.getStdOut()).thenReturn(this.stdout);
	}

	@Test
	void shouldBeCompleted() {
		when(this.database.getVersion()).thenReturn(CassandraBuilder.DEFAULT_VERSION);
		StartupParser parser = new StartupParser(this.database);
		verify(this.stdout).attach(parser);
		assertThat(parser.isComplete()).isFalse();
		parser.accept("INFO Startup complete");
		assertThat(parser.isComplete()).isTrue();
		parser.close();
		verify(this.stdout).detach(parser);
	}

	@Test
	void shouldBeCompletedLower4() {
		when(this.database.getVersion()).thenReturn(Version.parse("3.11.8"));
		StartupParser parser = new StartupParser(this.database);
		verify(this.stdout).attach(parser);
		assertThat(parser.isComplete()).isTrue();
		parser.close();
		verify(this.stdout).detach(parser);
	}

	@Test
	void shouldBeCompletedUnsupported() {
		when(this.database.getVersion()).thenReturn(Version.parse("4.0-beta2"));
		StartupParser parser = new StartupParser(this.database);
		verify(this.stdout).attach(parser);
		assertThat(parser.isComplete()).isTrue();
		parser.close();
		verify(this.stdout).detach(parser);
	}

}
