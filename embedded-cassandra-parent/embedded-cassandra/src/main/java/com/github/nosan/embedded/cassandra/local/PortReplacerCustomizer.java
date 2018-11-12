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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.reader.UnicodeReader;

import com.github.nosan.embedded.cassandra.util.PortUtils;
import com.github.nosan.embedded.cassandra.util.StreamUtils;

/**
 * {@link DirectoryCustomizer} to replace all {@code 0} ports in a {@code cassandra.yaml}.
 *
 * @author Dmytro Nosan
 * @since 1.0.9
 */
class PortReplacerCustomizer implements DirectoryCustomizer {

	private static final Logger log = LoggerFactory.getLogger(PortReplacerCustomizer.class);


	@Override
	public void customize(@Nonnull Path directory) throws IOException {
		Iterator<Integer> ports = PortUtils.getPorts(5).iterator();
		Path target = directory.resolve("conf/cassandra.yaml");
		String source = StreamUtils.toString(new UnicodeReader(Files.newInputStream(target)));
		Pattern pattern = Pattern.compile("^([a-z_]+)_port:\\s*([0-9]+)\\s*$", Pattern.MULTILINE);
		log.debug("Replace any ({}) in a ({})", pattern, target);
		Matcher matcher = pattern.matcher(source);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String name = matcher.group(1);
			int port = Integer.parseInt(matcher.group(2));
			matcher.appendReplacement(sb, String.format("%s_port: %s", name, getPort(port, ports)));
		}
		matcher.appendTail(sb);
		try (BufferedWriter writer = Files.newBufferedWriter(target)) {
			writer.write(sb.toString());
		}

	}

	private static int getPort(int port, Iterator<Integer> ports) {
		if (port != 0) {
			return port;
		}
		if (ports.hasNext()) {
			return ports.next();
		}
		//edge case, maybe in the future new ports will be added and this method should not fail.
		return PortUtils.getPort();
	}
}
