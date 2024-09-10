/*
 * Copyright 2020-2024 the original author or authors.
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
 * Tests for {@link ErrorCollector}.
 *
 * @author Dmytro Nosan
 */
class ErrorCollectorTests {

	private final ProcessWrapper.Output stderr = Mockito.mock(ProcessWrapper.Output.class);

	private final CassandraDatabase database = Mockito.mock(CassandraDatabase.class);

	@Test
	void shouldCollectError() {
		when(this.database.getStdErr()).thenReturn(this.stderr);
		ErrorCollector collector = new ErrorCollector(this.database);
		verify(this.stderr).attach(collector);
		for (int i = 0; i < 6; i++) {
			collector.accept(Integer.toString(i));
		}
		assertThat(collector.getErrors()).containsExactly("0", "1", "2", "3", "4", "5");
		collector.close();
		verify(this.stderr).detach(collector);
	}

}
