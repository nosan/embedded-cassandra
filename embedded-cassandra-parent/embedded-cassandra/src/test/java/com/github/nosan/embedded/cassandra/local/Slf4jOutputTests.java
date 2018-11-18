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

import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.test.support.CaptureOutput;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Slf4jOutput}.
 *
 * @author Dmytro Nosan
 */
public class Slf4jOutputTests {

	private static final Logger log = LoggerFactory.getLogger(Slf4jOutputTests.class);

	@Rule
	public final CaptureOutput output = new CaptureOutput();

	private final Slf4jOutput slf4jOutput = new Slf4jOutput(log);

	@Test
	public void shouldLogErrorOnException() {
		this.slf4jOutput.accept("BindException");
		assertThat(this.output.toString()).contains(" ERROR c.g.n.e.c.local.Slf4jOutputTests");
	}

	@Test
	public void shouldLogErrorOnError() {
		this.slf4jOutput.accept("Fatal Configuration Error ");
		assertThat(this.output.toString()).contains(" ERROR c.g.n.e.c.local.Slf4jOutputTests");
	}

	@Test
	public void shouldLogInfo() {
		this.slf4jOutput.accept("-XX:+HeapDumpOnOutOfMemoryError");
		assertThat(this.output.toString()).contains(" INFO  c.g.n.e.c.local.Slf4jOutputTests");

	}
}
