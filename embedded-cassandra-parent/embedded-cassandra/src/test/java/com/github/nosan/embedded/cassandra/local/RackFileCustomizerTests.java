/*
 * Copyright 2018-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nosan.embedded.cassandra.local;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.nosan.embedded.cassandra.Version;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link RackFileCustomizer}.
 *
 * @author Dmytro Nosan
 */
class RackFileCustomizerTests {

	@Test
	void customize(@TempDir Path temporaryFolder) throws IOException {
		Path conf = temporaryFolder.resolve("conf");
		Files.createDirectories(conf);
		RackFileCustomizer customizer = new RackFileCustomizer(
				getClass().getResource("/cassandra-rackdc.properties"));

		customizer.customize(conf.getParent(), Version.parse("3.11.3"));

		try (InputStream inputStream = getClass().getResourceAsStream("/cassandra-rackdc.properties")) {
			assertThat(conf.resolve("cassandra-rackdc.properties"))
					.hasBinaryContent(IOUtils.toByteArray(inputStream));
		}
	}

}
