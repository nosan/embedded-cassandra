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

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;

import com.github.nosan.embedded.cassandra.ExecutableConfig;

/**
 * Utility class for creating command line.
 *
 * @author Dmytro Nosan
 */
abstract class ArgumentUtils {

	/**
	 * Creates a command line.
	 *
	 * @param context Process context.
	 * @return Command line.
	 */
	static List<String> get(Context context) {
		IExtractedFileSet fileSet = context.getExtractedFileSet();
		ExecutableConfig executableConfig = context.getExecutableConfig();
		Distribution distribution = context.getDistribution();
		IRuntimeConfig runtimeConfig = context.getRuntimeConfig();
		List<String> args = new ArrayList<>();
		if (distribution.getPlatform() == Platform.Windows) {
			args.add("powershell");
			args.add("-ExecutionPolicy");
			args.add("Unrestricted");
		}
		args.add(fileSet.executable().getAbsolutePath());
		args.add("-f");
		args.add(getJmxOpt(distribution) + executableConfig.getConfig().getJmxPort());
		return runtimeConfig.getCommandLinePostProcessor().process(distribution, args);
	}

	private static String getJmxOpt(Distribution distribution) {
		return (distribution.getPlatform() != Platform.Windows
				? "-Dcassandra.jmx.local.port=" : "`-Dcassandra.jmx.local.port=");
	}

}
