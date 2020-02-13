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

package com.github.nosan.embedded.cassandra.commons.logging;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConsoleLogger}.
 *
 * @author Dmytro Nosan
 */
class ConsoleLoggerTests {

	private PrintStream stdout;

	private PrintStream stderr;

	private final ByteArrayOutputStream out = new ByteArrayOutputStream();

	private final ByteArrayOutputStream err = new ByteArrayOutputStream();

	private final String name = "ROOT";

	private final Logger logger = new ConsoleLogger(this.name);

	private final RuntimeException exception = new RuntimeException("EXCEPTION");

	@BeforeEach
	void setUp() {
		this.stdout = System.out;
		this.stderr = System.err;
		System.setOut(new PrintStream(this.out));
		System.setErr(new PrintStream(this.err));
	}

	@AfterEach
	void tearDown() {
		System.setOut(this.stdout);
		System.setErr(this.stderr);
	}

	@Test
	void getName() {
		assertThat(this.logger.getName()).isEqualTo(this.name);
	}

	@Test
	void isErrorEnabled() {
		assertThat(this.logger.isErrorEnabled()).isTrue();
	}

	@Test
	void error() {
		this.logger.error("Text");
		assertThat(this.err.toString()).contains("Text")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void errorNullMessage() {
		this.logger.error(null);
		assertThat(this.err.toString()).contains("null")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void errorNullTemplate() {
		this.logger.error(null, new Object[0]);
		assertThat(this.err.toString()).contains("null")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void errorNullArguments() {
		this.logger.error("{0}", (Object[]) null);
		assertThat(this.err.toString()).contains("{0}")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void errorEmptyArguments() {
		this.logger.error("{0}", new Object[0]);
		assertThat(this.err.toString()).contains("{0}")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testError() {
		this.logger.error("{0}", "Text");
		assertThat(this.err.toString()).contains("Text")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testError1() {
		this.logger.error(this.exception, "Text");
		assertThat(this.err.toString()).contains("Text").contains("EXCEPTION")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testError2() {
		this.logger.error(this.exception, "{0}", "Text");
		assertThat(this.err.toString()).contains("Text").contains("EXCEPTION")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testErrorNullThrowable() {
		this.logger.error(null, "Text");
		assertThat(this.err.toString()).contains("Text").doesNotContain("EXCEPTION")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testErrorNullThrowableTemplate() {
		this.logger.error(((Throwable) null), "{0}", "Text");
		assertThat(this.err.toString()).contains("Text").doesNotContain("EXCEPTION")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void isWarnEnabled() {
		assertThat(this.logger.isWarnEnabled()).isTrue();
	}

	@Test
	void warn() {
		this.logger.warn("Text");
		assertThat(this.out.toString()).contains("Text")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void warnNullMessage() {
		this.logger.warn(null);
		assertThat(this.out.toString()).contains("null")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void warnNullTemplate() {
		this.logger.warn(null, new Object[0]);
		assertThat(this.out.toString()).contains("null")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void warnNullArguments() {
		this.logger.warn("{0}", (Object[]) null);
		assertThat(this.out.toString()).contains("{0}")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void warnEmptyArguments() {
		this.logger.warn("{0}", new Object[0]);
		assertThat(this.out.toString()).contains("{0}")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testWarn() {
		this.logger.warn("{0}", "Text");
		assertThat(this.out.toString()).contains("Text")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testWarn1() {
		this.logger.warn(this.exception, "Text");
		assertThat(this.out.toString()).contains("Text").contains("EXCEPTION")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testWarn2() {
		this.logger.warn(this.exception, "{0}", "Text");
		assertThat(this.out.toString()).contains("Text").contains("EXCEPTION")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testWarnNullThrowable() {
		this.logger.warn(null, "Text");
		assertThat(this.out.toString()).contains("Text").doesNotContain("EXCEPTION")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testWarnNullThrowableTemplate() {
		this.logger.warn(((Throwable) null), "{0}", "Text");
		assertThat(this.out.toString()).contains("Text").doesNotContain("EXCEPTION")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void isInfoEnabled() {
		assertThat(this.logger.isInfoEnabled()).isTrue();
	}

	@Test
	void info() {
		this.logger.info("Text");
		assertThat(this.out.toString()).contains("Text")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void infoNullMessage() {
		this.logger.info(null);
		assertThat(this.out.toString()).contains("null")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void infoNullTemplate() {
		this.logger.info(null, new Object[0]);
		assertThat(this.out.toString()).contains("null")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void infoNullArguments() {
		this.logger.info("{0}", (Object[]) null);
		assertThat(this.out.toString()).contains("{0}")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void infoEmptyArguments() {
		this.logger.info("{0}", new Object[0]);
		assertThat(this.out.toString()).contains("{0}")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testInfo() {
		this.logger.info("{0}", "Text");
		assertThat(this.out.toString()).contains("Text")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testInfo1() {
		this.logger.info(this.exception, "Text");
		assertThat(this.out.toString()).contains("Text").contains("EXCEPTION")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testInfo2() {
		this.logger.info(this.exception, "{0}", "Text");
		assertThat(this.out.toString()).contains("Text").contains("EXCEPTION")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testInfoNullThrowable() {
		this.logger.info(null, "Text");
		assertThat(this.out.toString()).contains("Text").doesNotContain("EXCEPTION")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testInfoNullThrowableTemplate() {
		this.logger.info(((Throwable) null), "{0}", "Text");
		assertThat(this.out.toString()).contains("Text").doesNotContain("EXCEPTION")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void isDebugEnabled() {
		assertThat(this.logger.isDebugEnabled()).isTrue();
	}

	@Test
	void debug() {
		this.logger.debug("Text");
		assertThat(this.out.toString()).contains("Text")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void debugNullMessage() {
		this.logger.debug(null);
		assertThat(this.out.toString()).contains("null")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void debugNullTemplate() {
		this.logger.debug(null, new Object[0]);
		assertThat(this.out.toString()).contains("null")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void debugNullArguments() {
		this.logger.debug("{0}", (Object[]) null);
		assertThat(this.out.toString()).contains("{0}")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void debugEmptyArguments() {
		this.logger.debug("{0}", new Object[0]);
		assertThat(this.out.toString()).contains("{0}")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testDebugNullThrowable() {
		this.logger.debug(null, "Text");
		assertThat(this.out.toString()).contains("Text").doesNotContain("EXCEPTION")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testDebugNullThrowableTemplate() {
		this.logger.debug(((Throwable) null), "{0}", "Text");
		assertThat(this.out.toString()).contains("Text").doesNotContain("EXCEPTION")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testDebug() {
		this.logger.debug("{0}", "Text");
		assertThat(this.out.toString()).contains("Text")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testDebug1() {
		this.logger.debug(this.exception, "Text");
		assertThat(this.out.toString()).contains("Text").contains("EXCEPTION")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testDebug2() {
		this.logger.debug(this.exception, "{0}", "Text");
		assertThat(this.out.toString()).contains("Text").contains("EXCEPTION")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void isTraceEnabled() {
		assertThat(this.logger.isTraceEnabled()).isTrue();
	}

	@Test
	void trace() {
		this.logger.trace("Text");
		assertThat(this.out.toString()).contains("Text")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void traceNullMessage() {
		this.logger.trace(null);
		assertThat(this.out.toString()).contains("null")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void traceNullTemplate() {
		this.logger.trace(null, new Object[0]);
		assertThat(this.out.toString()).contains("null")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void traceNullArguments() {
		this.logger.trace("{0}", (Object[]) null);
		assertThat(this.out.toString()).contains("{0}")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void traceEmptyArguments() {
		this.logger.trace("{0}", new Object[0]);
		assertThat(this.out.toString()).contains("{0}")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testTrace() {
		this.logger.trace("{0}", "Text");
		assertThat(this.out.toString()).contains("Text")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testTrace1() {
		this.logger.trace(this.exception, "Text");
		assertThat(this.out.toString()).contains("Text").contains("EXCEPTION")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testTrace2() {
		this.logger.trace(this.exception, "{0}", "Text");
		assertThat(this.out.toString()).contains("Text").contains("EXCEPTION")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testTraceNullThrowable() {
		this.logger.trace(null, "Text");
		assertThat(this.out.toString()).contains("Text").doesNotContain("EXCEPTION")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

	@Test
	void testTraceNullThrowableTemplate() {
		this.logger.trace(((Throwable) null), "{0}", "Text");
		assertThat(this.out.toString()).contains("Text").doesNotContain("EXCEPTION")
				.contains(Thread.currentThread().getName())
				.contains(this.name);
	}

}
