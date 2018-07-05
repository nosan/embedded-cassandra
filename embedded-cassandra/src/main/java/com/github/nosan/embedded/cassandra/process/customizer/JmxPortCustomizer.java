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

package com.github.nosan.embedded.cassandra.process.customizer;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Platform;

/**
 * {@link FileCustomizer File Customizer} to replace $JMX_PORT or JMX_PORT in the
 * `cassandra-env.sh` or `cassandra-env.ps1` files.
 *
 * @author Dmytro Nosan
 */
public class JmxPortCustomizer extends AbstractSourceLineFileCustomizer {

	private static final Pattern REGEX = Pattern
			.compile("(\\s*\\$?JMX_PORT\\s*=)(\\s*\"?\\s*)(\\d+)(\\s*\"?\\s*)");

	private final int port;

	public JmxPortCustomizer(int port) {
		this.port = port;
	}

	@Override
	protected boolean isMatch(File file, Distribution distribution) {
		String fileName = file.getName();
		return (distribution.getPlatform() != Platform.Windows
				? fileName.equals("cassandra-env.sh")
				: fileName.equals("cassandra-env.ps1"));
	}

	@Override
	protected String process(String line, File file, Distribution distribution) {
		Matcher matcher = REGEX.matcher(line);
		if (matcher.matches()) {
			return matcher.replaceAll("$1$2" + this.port + "$4");
		}
		return line;
	}

}
