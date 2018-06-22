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

import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Platform;

/**
 * Environment file customizer. Basic implementation disable `Xloggc` , `jmx.local.port`
 * and `jmx.remote.port`.
 *
 * @author Dmytro Nosan
 */
public class EnvironmentFileCustomizer extends AbstractSourceLineFileCustomizer {

	@Override
	protected boolean isMatch(File file, Distribution distribution) {
		String fileName = file.getName();
		return (distribution.getPlatform() != Platform.Windows
				? fileName.equals("cassandra-env.sh")
				: fileName.equals("cassandra-env.ps1"));
	}

	@Override
	protected String processLine(String line, Distribution distribution) {
		if (line.trim().contains("-Dcassandra.jmx.local.port")) {
			return "#" + line;
		}
		if (line.trim().contains("-Dcassandra.jmx.remote.port")) {
			return "#" + line;
		}
		if (line.trim().contains("-Xloggc:")) {
			return "#" + line;
		}
		return line;
	}

}
