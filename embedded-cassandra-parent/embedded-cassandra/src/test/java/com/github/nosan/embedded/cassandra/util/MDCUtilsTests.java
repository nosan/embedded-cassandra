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

package com.github.nosan.embedded.cassandra.util;

import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MDCUtils}.
 *
 * @author Dmytro Nosan
 */
public class MDCUtilsTests {

	@Before
	public void setUp() {
		MDC.clear();
	}

	@Test
	public void getContext() {
		Map<String, String> context = MDCUtils.getContext();
		assertThat(context).isEmpty();
		MDC.put("test", "test");
		assertThat(MDCUtils.getContext()).containsEntry("test", "test");
	}

	@Test
	public void setContext() {
		MDCUtils.setContext(Collections.singletonMap("test", "test"));
		assertThat(MDCUtils.getContext()).containsEntry("test", "test");
		MDCUtils.setContext(null);
		assertThat(MDCUtils.getContext()).isEmpty();

	}

}
