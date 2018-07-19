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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import com.github.nosan.embedded.cassandra.ExecutableConfig;

/**
 * {@link FileCustomizer} to override {@code logback.xml} file.
 *
 * @author Dmytro Nosan
 */
class LogbackFileCustomizer extends AbstractFileCustomizer {

	@Override
	protected boolean isMatch(File file, Context context) {
		return file.getName().equalsIgnoreCase("logback.xml");
	}

	@Override
	protected void process(File file, Context context) throws IOException {
		ExecutableConfig executableConfig = context.getExecutableConfig();
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(executableConfig.getLogback().openStream()));
				PrintWriter printWriter = new PrintWriter(new FileWriter(file))) {
			reader.lines().forEach(printWriter::println);
		}
	}
}
