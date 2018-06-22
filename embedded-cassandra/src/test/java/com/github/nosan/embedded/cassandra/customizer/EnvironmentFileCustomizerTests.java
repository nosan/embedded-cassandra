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
import java.nio.file.Files;

import com.github.nosan.embedded.cassandra.config.Version;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Platform;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EnvironmentFileCustomizerTests}.
 *
 * @author Dmytro Nosan
 */
public class EnvironmentFileCustomizerTests {

	@Test
	public void environment() throws IOException {
		Distribution distribution = Distribution.detectFor(Version.LATEST);
		File file = new File(getName(distribution));
		try (PrintWriter ps = new PrintWriter(new FileWriter(file))) {
			ps.println("JVM_OPTS=\"$JVM_OPTS -Dcassandra.jmx.local.port=$JMX_PORT\"");
			ps.println(
					"JVM_OPTS=\"$JVM_OPTS -Dcom.sun.management.jmxremote.authenticate=false\"");
			ps.println("JVM_OPTS=\"$JVM_OPTS -Xloggc:${CASSANDRA_HOME}/logs/gc.log\"");
			ps.println("JVM_OPTS_FILE=$CASSANDRA_CONF/jvm.options");
		}

		try {
			new EnvironmentFileCustomizer().customize(file, distribution);

			assertThat(file).hasContent(
					"#JVM_OPTS=\"$JVM_OPTS -Dcassandra.jmx.local.port=$JMX_PORT\"\n"
							+ "JVM_OPTS=\"$JVM_OPTS -Dcom.sun.management.jmxremote.authenticate=false\"\n"
							+ "#JVM_OPTS=\"$JVM_OPTS -Xloggc:${CASSANDRA_HOME}/logs/gc.log\"\n"
							+ "JVM_OPTS_FILE=$CASSANDRA_CONF/jvm.options");

		}
		finally {
			Files.deleteIfExists(file.toPath());
		}

	}

	private String getName(Distribution distribution) {
		return (distribution.getPlatform() != Platform.Windows ? "cassandra-env.sh"
				: "cassandra-env.ps1");
	}

}
