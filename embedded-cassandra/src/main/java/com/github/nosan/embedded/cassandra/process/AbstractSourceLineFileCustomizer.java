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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A {@link FileCustomizer File Customizer} which provides `per-line` file customizations.
 *
 * @author Dmytro Nosan
 */
abstract class AbstractSourceLineFileCustomizer extends AbstractFileCustomizer {

	@Override
	protected final void process(File file, Context context) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		try (PrintWriter printWriter = new PrintWriter(result);
				BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String process = process(line, file, context);
				printWriter.println(process);
			}
		}
		try (PrintWriter fileWriter = new PrintWriter(new FileWriter(file))) {
			fileWriter.print(result.toString());
		}
	}

	/**
	 * Process the provided line.
	 *
	 * @param line source line.
	 * @param file source file.
	 * @param context {@link Context Process Context}.
	 * @return processed line.
	 */
	protected abstract String process(String line, File file, Context context);

}
