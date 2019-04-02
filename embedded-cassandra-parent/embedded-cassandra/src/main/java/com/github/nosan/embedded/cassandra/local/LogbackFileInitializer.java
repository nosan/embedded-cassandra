/*
 * Copyright 2018-2019 the original author or authors.
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

package com.github.nosan.embedded.cassandra.local;

import java.net.URL;

import com.github.nosan.embedded.cassandra.util.annotation.Nullable;

/**
 * {@link Initializer} to initialize {@code logback.xml}.
 *
 * @author Dmytro Nosan
 * @since 1.0.9
 */
class LogbackFileInitializer extends AbstractFileReplacerInitializer {

	/**
	 * Creates a {@link LogbackFileInitializer}.
	 *
	 * @param logbackFile URL to {@code logback.xml}
	 */
	LogbackFileInitializer(@Nullable URL logbackFile) {
		super(logbackFile, (workDir, version) -> workDir.resolve("conf/logback.xml"));
	}

}
