/*
 * Copyright 2018-2018 the original author or authors.
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

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link RunProcess}.
 *
 * @author Dmytro Nosan
 */
public class RunProcessTests {


	@Test
	public void shouldRunAndWait() throws Exception {
		OutputCapture bufferOutput = new OutputCapture(Integer.MAX_VALUE);
		int exit = new RunProcess(Arrays.asList("bash", "-c", "echo 'Hello World' > 1.txt; cat 1.txt"))
				.runAndWait(bufferOutput);
		assertThat(bufferOutput.toString()).isEqualTo("Hello World");
		assertThat(exit).isEqualTo(0);
	}

	@Test
	public void shouldRun() throws Exception {
		Process process = new RunProcess(Arrays.asList("sleep", "2")).run();
		assertThat(process.waitFor(1, TimeUnit.SECONDS)).isFalse();
		assertThat(process.waitFor(2, TimeUnit.SECONDS)).isTrue();
	}
}
