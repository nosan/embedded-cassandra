/*
 * Copyright 2018-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra.local;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.reader.UnicodeReader;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.util.PortUtils;
import com.github.nosan.embedded.cassandra.util.StreamUtils;

/**
 * {@link DirectoryInitializer} to replace all {@code 0} ports in a {@code cassandra.yaml}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
class PortReplacerInitializer implements DirectoryInitializer {

	private static final Logger log = LoggerFactory.getLogger(PortReplacerInitializer.class);


	@Override
	public void initialize(@Nonnull Path directory, @Nonnull Version version) throws Exception {
		Path target = directory.resolve("conf/cassandra.yaml");
		String source = StreamUtils.toString(new UnicodeReader(Files.newInputStream(target)));
		Pattern pattern = Pattern.compile("^([a-z_]+)_port:\\s*([0-9]+)\\s*$", Pattern.MULTILINE);
		log.debug("Replace any ({}) in a ({})", pattern, target);
		Matcher matcher = pattern.matcher(source);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String name = matcher.group(1);
			int port = Integer.parseInt(matcher.group(2));
			matcher.appendReplacement(sb, String.format("%s_port: %s", name, getPort(port)));
		}
		matcher.appendTail(sb);
		try (BufferedWriter writer = Files.newBufferedWriter(target)) {
			writer.write(sb.toString());
		}

	}

	private static int getPort(int port) {
		return (port != 0) ? port : PortUtils.getPort();
	}
}
