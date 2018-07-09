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

package com.github.nosan.embedded.cassandra.process;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.JavaVersion;
import org.junit.ClassRule;
import org.junit.Test;

import com.github.nosan.embedded.cassandra.JavaVersionRule;
import com.github.nosan.embedded.cassandra.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CassandraProcess#getCustomizers()}.
 *
 * @author Dmytro Nosan
 */
public class CassandraProcessCustomizerTests {

	@ClassRule
	public static JavaVersionRule javaVersionRule = new JavaVersionRule(
			JavaVersion.JAVA_1_8, false);

	@Test
	public void getDefaultCustomizers()
			throws NoSuchFieldException, IllegalAccessException {
		List<ContextCustomizer> customizers = CassandraProcess.getCustomizers();
		assertThat(customizers).hasSize(2);
		ContextCustomizer fileCustomizers = customizers.get(1);
		assertThat(fileCustomizers).isInstanceOf(FileCustomizers.class);
		assertThat(
				(Collection<?>) ReflectionUtils.getField("customizers", fileCustomizers))
				.hasSize(3);

	}

}
