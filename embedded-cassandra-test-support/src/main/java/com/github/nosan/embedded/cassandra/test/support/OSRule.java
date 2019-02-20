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

import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * {@link TestRule} to enable {@link DisableIfOS} and {@link EnableIfOS} annotations.
 *
 * @author Dmytro Nosan
 * @since 1.4.1
 */
public final class OSRule implements MethodRule, TestRule {

	@Override
	public Statement apply(Statement base, FrameworkMethod method, Object target) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				OSRule.evaluate(method.getAnnotation(EnableIfOS.class),
						method.getAnnotation(DisableIfOS.class), base);
			}
		};
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				OSRule.evaluate(description.getAnnotation(EnableIfOS.class),
						description.getAnnotation(DisableIfOS.class), base);
			}
		};
	}

	private static void evaluate(EnableIfOS enable, DisableIfOS disable, Statement base) throws Throwable {
		if (disable != null) {
			if (!match(disable.value())) {
				base.evaluate();
			}
		}
		else if (enable != null) {
			if (match(enable.value())) {
				base.evaluate();
			}
		}
		else {
			base.evaluate();
		}
	}

	private static boolean match(String[] operatingSystems) {
		String currentOS = System.getProperty("os.name").toLowerCase();
		for (String os : operatingSystems) {
			if (currentOS.contains(os.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

}
