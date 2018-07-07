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

package com.github.nosan.embedded.cassandra;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Test rule for java versions.
 *
 * @author Dmytro Nosan
 */
public final class JavaVersionRule implements TestRule {

	private static Map<JavaVersion, Supplier<Boolean>> versions = new LinkedHashMap<>();

	static {
		versions.put(JavaVersion.JAVA_1_8, () -> SystemUtils.IS_JAVA_1_8);
		versions.put(JavaVersion.JAVA_9, () -> SystemUtils.IS_JAVA_9);
		versions.put(JavaVersion.JAVA_10, () -> SystemUtils.IS_JAVA_10);
	}

	private final JavaVersion version;

	private final boolean atLeast;

	public JavaVersionRule(JavaVersion version, boolean atLeast) {
		this.version = version;
		this.atLeast = atLeast;
	}

	@Override
	public Statement apply(Statement base, Description description) {
		JavaVersion version = this.version;
		boolean atLeast = this.atLeast;
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				if (atLeast) {
					if (SystemUtils.isJavaVersionAtLeast(version)) {
						base.evaluate();
					}
				}
				else if (versions.get(version).get()) {
					base.evaluate();
				}
			}
		};
	}

}
