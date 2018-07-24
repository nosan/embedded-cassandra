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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;

/**
 * Utility class for invoking {@link ContextCustomizer}.
 *
 * @author Dmytro Nosan
 */
abstract class CustomizerUtils {

	/**
	 * Invokes pre-defined {@link ContextCustomizer}.
	 *
	 * @param context Process context.
	 */
	static void customize(Context context) {
		for (ContextCustomizer customizer : getCustomizers()) {
			customizer.customize(context);
		}
	}

	private static List<ContextCustomizer> getCustomizers() {
		List<ContextCustomizer> customizers = new ArrayList<>();
		FileCustomizers fileCustomizers = new FileCustomizers();
		if (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_9)) {
			fileCustomizers.addCustomizer(new Java9CompatibilityFileCustomizer());
		}
		fileCustomizers.addCustomizer(new NumaFileCustomizer());
		fileCustomizers.addCustomizer(new JVMOptionsFileCustomizer());
		fileCustomizers.addCustomizer(new ConfigFileCustomizer());
		fileCustomizers.addCustomizer(new LogbackFileCustomizer());
		customizers.add(new RandomPortCustomizer());
		customizers.add(fileCustomizers);
		return customizers;
	}

}
