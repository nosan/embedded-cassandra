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
import java.io.IOException;
import java.nio.file.Files;

import com.github.nosan.embedded.cassandra.config.Version;
import de.flapdoodle.embed.process.distribution.Distribution;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JVMOptionsFileCustomizer}.
 *
 * @author Dmytro Nosan
 */
public class JVMOptionsFileCustomizerTests {

	@Test
	public void jvmOptions() throws IOException {
		File file = new File("jvm.options");
		try {
			new JVMOptionsFileCustomizer().customize(file,
					Distribution.detectFor(Version.LATEST));

			assertThat(file).hasContent("-ea\n" + "-Xms128m\n" + "-Xmx256m\n"
					+ "-Djava.net.preferIPv4Stack=true");

		}
		finally {
			Files.deleteIfExists(file.toPath());
		}

	}

}
