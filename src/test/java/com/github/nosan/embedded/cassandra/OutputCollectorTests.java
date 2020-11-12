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

package com.github.nosan.embedded.cassandra;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link OutputCollector}.
 *
 * @author Dmytro Nosan
 */
class OutputCollectorTests {

	private final Process.Output stdout = Mockito.mock(Process.Output.class);

	private final CassandraDatabase database = Mockito.mock(CassandraDatabase.class);

	@Test
	void shouldCollectOutput() {
		when(this.database.getStdOut()).thenReturn(this.stdout);
		OutputCollector collector = new OutputCollector(this.database);
		verify(this.stdout).attach(collector);
		for (int i = 0; i < 35; i++) {
			collector.accept(Integer.toString(i));
		}
		assertThat(collector.getOutput()).hasSize(30);
		assertThat(collector.getOutput()).doesNotContain("0", "1", "2", "3", "4");
		for (int i = 5; i < 35; i++) {
			assertThat(collector.getOutput()).contains(Integer.toString(i));
		}
		collector.close();
		verify(this.stdout).detach(collector);
	}

}
