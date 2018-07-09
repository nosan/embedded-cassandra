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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.github.nosan.embedded.cassandra.Config;
import com.github.nosan.embedded.cassandra.ExecutableConfig;
import com.github.nosan.embedded.cassandra.JvmOptions;

/**
 * JVM options {@link FileCustomizer File Customizer}. JVM Options will be added at the
 * end of file.
 *
 * @author Dmytro Nosan
 */
class JVMOptionsFileCustomizer extends AbstractFileCustomizer {

	@Override
	protected boolean isMatch(File file, Context context) {
		return file.getName().equals("jvm.options");
	}

	@Override
	protected void process(File file, Context context) throws IOException {
		JvmOptions options = getJvmOptions(context);
		if (options != null) {
			writeOptions(file, options);
		}
	}

	private void writeOptions(File file, JvmOptions options) throws IOException {
		try (PrintWriter fileWriter = new PrintWriter(
				new FileWriter(file, isAppend(options)))) {
			for (String line : options.getOptions()) {
				fileWriter.println(line);
			}
		}
	}

	private boolean isAppend(JvmOptions options) {
		return options.getMode() == JvmOptions.Mode.ADD;
	}

	private JvmOptions getJvmOptions(Context context) {
		ExecutableConfig executableConfig = context.getExecutableConfig();
		Config config = executableConfig.getConfig();
		return config.getJvmOptions();

	}

}
