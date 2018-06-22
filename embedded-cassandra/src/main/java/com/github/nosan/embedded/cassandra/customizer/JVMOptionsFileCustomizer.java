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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import de.flapdoodle.embed.process.distribution.Distribution;

/**
 * Java options file customizer.
 *
 * @author Dmytro Nosan
 */
public class JVMOptionsFileCustomizer extends AbstractFileCustomizer {

	private final Set<String> jvmOptions;

	public JVMOptionsFileCustomizer(Set<String> jvmOptions) {
		this.jvmOptions = Objects.requireNonNull(jvmOptions,
				"JVM Options must null be null");
	}

	public JVMOptionsFileCustomizer() {
		this(new LinkedHashSet<>(Arrays.asList("-ea", "-Xms128m", "-Xmx256m",
				"-Djava.net.preferIPv4Stack=true")));
	}

	@Override
	protected boolean isMatch(File file, Distribution distribution) {
		return file.getName().equals("jvm.options");
	}

	@Override
	protected void process(File file, Distribution distribution) throws IOException {
		try (PrintWriter fileWriter = new PrintWriter(new FileWriter(file))) {
			for (String line : this.jvmOptions) {
				fileWriter.println(line);
			}
		}
	}

}
