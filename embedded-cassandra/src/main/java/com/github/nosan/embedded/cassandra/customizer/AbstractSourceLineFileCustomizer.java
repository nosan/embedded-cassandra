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

package com.github.nosan.embedded.cassandra.customizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import de.flapdoodle.embed.process.distribution.Distribution;

/**
 * A {@link FileCustomizer} which provides `per-line` file manipulation.
 *
 * @author Dmytro Nosan
 */
public abstract class AbstractSourceLineFileCustomizer extends AbstractFileCustomizer {

	@Override
	protected final void process(File file, Distribution distribution)
			throws IOException {
		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(processLine(line, distribution));
			}
		}
		try (PrintWriter fileWriter = new PrintWriter(new FileWriter(file))) {
			for (String line : lines) {
				fileWriter.println(line);
			}
		}
	}

	/**
	 * Process the provided line.
	 * @param line Source line.
	 * @param distribution {@link Distribution}.
	 * @return processed line.
	 */
	protected abstract String processLine(String line, Distribution distribution);

}
