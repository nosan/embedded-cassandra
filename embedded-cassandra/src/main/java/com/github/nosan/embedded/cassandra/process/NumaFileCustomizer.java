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
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.runtime.NUMA;

import com.github.nosan.embedded.cassandra.ExecutableConfig;

/**
 * Numa {@link FileCustomizer}. Disable Numa if platform doesn't support it.
 *
 * @author Dmytro Nosan
 */
class NumaFileCustomizer extends AbstractSourceLineFileCustomizer {

	private final static Pattern REGEX = Pattern.compile("-XX:\\+UseNUMA");

	private static boolean isNUMA(Context context) {
		ExecutableConfig executableConfig = context.getExecutableConfig();
		Distribution distribution = context.getDistribution();
		return NUMA.isNUMA(executableConfig.supportConfig(), distribution.getPlatform());
	}

	@Override
	protected boolean isMatch(File file, Context context) throws IOException {
		String fileName = file.getName();
		return fileName.equals("jvm.options");
	}

	@Override
	protected String process(String line, File file, Context context) {
		Matcher matcher = REGEX.matcher(line);
		if (matcher.find()) {
			if (isNUMA(context)) {
				return line;
			}
			return "-XX:-UseNUMA";
		}
		return line;
	}

}
