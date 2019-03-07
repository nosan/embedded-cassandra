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

package com.github.nosan.embedded.cassandra.test.support;

import java.util.Arrays;
import java.util.Locale;

import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * {@link TestRule} to enable {@link DisableIfOS} and {@link EnableIfOS} annotations.
 *
 * @author Dmytro Nosan
 * @since 1.4.1
 */
public final class OSRule implements TestRule {

	@Override
	public Statement apply(Statement statement, Description description) {
		Class<?> testClass = description.getTestClass();
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				if (description.getAnnotation(EnableIfOS.class) != null) {
					check(description, description.getAnnotation(EnableIfOS.class));
				}
				else {
					check(description, testClass.getAnnotation(EnableIfOS.class));
				}
				check(description, description.getAnnotation(DisableIfOS.class));
				check(description, testClass.getAnnotation(DisableIfOS.class));
				statement.evaluate();
			}
		};
	}

	private static void check(Description description, EnableIfOS enable) {
		if (enable != null) {
			String os = System.getProperty("os.name");
			String[] values = enable.value();
			Assume.assumeTrue(String.format("%s is enabled only for %s. Current OS is '%s'",
					description, Arrays.toString(values), os), match(os, values));
		}
	}

	private static void check(Description description, DisableIfOS disable) {
		if (disable != null) {
			String os = System.getProperty("os.name");
			String[] values = disable.value();
			Assume.assumeFalse(String.format("%s is disabled. Current OS '%s' is on the list %s",
					description, os, Arrays.toString(values)), match(os, values));
		}
	}

	private static boolean match(String os, String[] systems) {
		for (String s : systems) {
			if (os.toLowerCase(Locale.ENGLISH).contains(s.toLowerCase(Locale.ENGLISH))) {
				return true;
			}
		}
		return false;
	}

}
