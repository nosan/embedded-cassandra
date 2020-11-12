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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link Slf4jLogger}.
 *
 * @author Dmytro Nosan
 */
class Slf4jLoggerTests {

	private final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);

	private final Logger logger = new Slf4jLogger(this.slf4jLogger);

	private final RuntimeException exception = new RuntimeException("EXCEPTION");

	@BeforeEach
	void setUp() {
		when(this.slf4jLogger.getName()).thenReturn("ROOT");
		when(this.slf4jLogger.isErrorEnabled()).thenReturn(true);
		when(this.slf4jLogger.isWarnEnabled()).thenReturn(true);
		when(this.slf4jLogger.isInfoEnabled()).thenReturn(true);
		when(this.slf4jLogger.isDebugEnabled()).thenReturn(true);
		when(this.slf4jLogger.isTraceEnabled()).thenReturn(true);
	}

	@Test
	void getName() {
		assertThat(this.logger.getName()).isEqualTo("ROOT");
	}

	@Test
	void isErrorEnabled() {
		assertThat(this.logger.isErrorEnabled()).isTrue();
	}

	@Test
	void error() {
		this.logger.error("Text");
		verify(this.slf4jLogger).error("Text");
	}

	@Test
	void errorNullMessage() {
		this.logger.error(null);
		verify(this.slf4jLogger).error(null);
	}

	@Test
	void errorNullTemplate() {
		this.logger.error(null, new Object[0]);
		verify(this.slf4jLogger).error(null);
	}

	@Test
	void errorNullArguments() {
		this.logger.error("{0}", (Object[]) null);
		verify(this.slf4jLogger).error("{0}");
	}

	@Test
	void errorEmptyArguments() {
		this.logger.error("{0}", new Object[0]);
		verify(this.slf4jLogger).error("{0}");
	}

	@Test
	void testError() {
		this.logger.error("{0}", "Text");
		verify(this.slf4jLogger).error("Text");
	}

	@Test
	void testError1() {
		this.logger.error(this.exception, "Text");
		verify(this.slf4jLogger).error("Text", this.exception);
	}

	@Test
	void testError2() {
		this.logger.error(this.exception, "{0}", "Text");
		verify(this.slf4jLogger).error("Text", this.exception);
	}

	@Test
	void isWarnEnabled() {
		assertThat(this.logger.isWarnEnabled()).isTrue();
	}

	@Test
	void warn() {
		this.logger.warn("Text");
		verify(this.slf4jLogger).warn("Text");
	}

	@Test
	void warnNullMessage() {
		this.logger.warn(null);
		verify(this.slf4jLogger).warn(null);
	}

	@Test
	void warnNullTemplate() {
		this.logger.warn(null, new Object[0]);
		verify(this.slf4jLogger).warn(null);
	}

	@Test
	void warnNullArguments() {
		this.logger.warn("{0}", (Object[]) null);
		verify(this.slf4jLogger).warn("{0}");
	}

	@Test
	void warnEmptyArguments() {
		this.logger.warn("{0}", new Object[0]);
		verify(this.slf4jLogger).warn("{0}");
	}

	@Test
	void testWarn() {
		this.logger.warn("{0}", "Text");
		verify(this.slf4jLogger).warn("Text");
	}

	@Test
	void testWarn1() {
		this.logger.warn(this.exception, "Text");
		verify(this.slf4jLogger).warn("Text", this.exception);
	}

	@Test
	void testWarn2() {
		this.logger.warn(this.exception, "{0}", "Text");
		verify(this.slf4jLogger).warn("Text", this.exception);
	}

	@Test
	void isInfoEnabled() {
		assertThat(this.logger.isInfoEnabled()).isTrue();
	}

	@Test
	void info() {
		this.logger.info("Text");
		verify(this.slf4jLogger).info("Text");
	}

	@Test
	void infoNullMessage() {
		this.logger.info(null);
		verify(this.slf4jLogger).info(null);
	}

	@Test
	void infoNullTemplate() {
		this.logger.info(null, new Object[0]);
		verify(this.slf4jLogger).info(null);
	}

	@Test
	void infoNullArguments() {
		this.logger.info("{0}", (Object[]) null);
		verify(this.slf4jLogger).info("{0}");
	}

	@Test
	void infoEmptyArguments() {
		this.logger.info("{0}", new Object[0]);
		verify(this.slf4jLogger).info("{0}");
	}

	@Test
	void testInfo() {
		this.logger.info("{0}", "Text");
		verify(this.slf4jLogger).info("Text");
	}

	@Test
	void testInfo1() {
		this.logger.info(this.exception, "Text");
		verify(this.slf4jLogger).info("Text", this.exception);
	}

	@Test
	void testInfo2() {
		this.logger.info(this.exception, "{0}", "Text");
		verify(this.slf4jLogger).info("Text", this.exception);
	}

	@Test
	void isDebugEnabled() {
		assertThat(this.logger.isDebugEnabled()).isTrue();
	}

	@Test
	void debug() {
		this.logger.debug("Text");
		verify(this.slf4jLogger).debug("Text");
	}

	@Test
	void debugNullMessage() {
		this.logger.debug(null);
		verify(this.slf4jLogger).debug(null);
	}

	@Test
	void debugNullTemplate() {
		this.logger.debug(null, new Object[0]);
		verify(this.slf4jLogger).debug(null);
	}

	@Test
	void debugNullArguments() {
		this.logger.debug("{0}", (Object[]) null);
		verify(this.slf4jLogger).debug("{0}");
	}

	@Test
	void debugEmptyArguments() {
		this.logger.debug("{0}", new Object[0]);
		verify(this.slf4jLogger).debug("{0}");
	}

	@Test
	void testDebug() {
		this.logger.debug("{0}", "Text");
		verify(this.slf4jLogger).debug("Text");
	}

	@Test
	void testDebug1() {
		this.logger.debug(this.exception, "Text");
		verify(this.slf4jLogger).debug("Text", this.exception);
	}

	@Test
	void testDebug2() {
		this.logger.debug(this.exception, "{0}", "Text");
		verify(this.slf4jLogger).debug("Text", this.exception);
	}

	@Test
	void trace() {
		this.logger.trace("Text");
		verify(this.slf4jLogger).trace("Text");
	}

	@Test
	void traceNullMessage() {
		this.logger.trace(null);
		verify(this.slf4jLogger).trace(null);
	}

	@Test
	void traceNullTemplate() {
		this.logger.trace(null, new Object[0]);
		verify(this.slf4jLogger).trace(null);
	}

	@Test
	void traceNullArguments() {
		this.logger.trace("{0}", (Object[]) null);
		verify(this.slf4jLogger).trace("{0}");
	}

	@Test
	void traceEmptyArguments() {
		this.logger.trace("{0}", new Object[0]);
		verify(this.slf4jLogger).trace("{0}");
	}

	@Test
	void testTrace() {
		this.logger.trace("{0}", "Text");
		verify(this.slf4jLogger).trace("Text");
	}

	@Test
	void testTrace1() {
		this.logger.trace(this.exception, "Text");
		verify(this.slf4jLogger).trace("Text", this.exception);
	}

	@Test
	void testTrace2() {
		this.logger.trace(this.exception, "{0}", "Text");
		verify(this.slf4jLogger).trace("Text", this.exception);
	}

	@Test
	void isTraceEnabled() {
		assertThat(this.logger.isTraceEnabled()).isTrue();
	}

}
