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

package com.github.nosan.embedded.cassandra.util;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.github.nosan.embedded.cassandra.test.support.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link OS}.
 *
 * @author Dmytro Nosan
 */
@RunWith(Parameterized.class)
public class OSTests {

	@Rule
	public final OSProperty osProperty;

	private final OS expectedOS;

	public OSTests(String name, OS expectedOS) {
		this.expectedOS = expectedOS;
		this.osProperty = new OSProperty(name);
	}

	@Test
	public void detect() {
		assertThat(ReflectionUtils.getStaticMethod(OS.class, "detect", new Object[0]))
				.isEqualTo(this.expectedOS);
		EnumSet<OS> os = EnumSet.allOf(OS.class);
		os.remove(this.expectedOS);
		os.forEach(o -> assertThat(os).isNotEqualTo(this.expectedOS));
	}

	@Parameterized.Parameters(name = "{0} - {1}")
	public static Iterable<Object[]> os() {
		List<Object[]> parameters = new ArrayList<>();
		parameters.add(new Object[]{"Solaris", OS.SOLARIS});
		parameters.add(new Object[]{"Windows 95", OS.WINDOWS});
		parameters.add(new Object[]{"Windows 2003", OS.WINDOWS});
		parameters.add(new Object[]{"Linux", OS.LINUX});
		parameters.add(new Object[]{"Mac OS X", OS.MAC});
		parameters.add(new Object[]{"Mac OS", OS.MAC});
		parameters.add(new Object[]{"SunOS", OS.OTHER});
		return parameters;
	}

	private static final class OSProperty implements MethodRule {

		private final String property;

		OSProperty(String property) {
			this.property = property;
		}

		@Override
		public Statement apply(Statement base, FrameworkMethod method, Object target) {
			String oldProperty = System.getProperty("os.name");
			return new Statement() {
				@Override
				public void evaluate() throws Throwable {
					System.setProperty("os.name", OSProperty.this.property);
					try {
						base.evaluate();
					}
					finally {
						System.setProperty("os.name", oldProperty);
					}

				}
			};
		}
	}

}
