/*
 * Copyright 2018-2019 the original author or authors.
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

import java.io.InputStream;
import java.nio.file.Path;

import org.apache.commons.compress.utils.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.nosan.embedded.cassandra.Version;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link RackFileInitializer}.
 *
 * @author Dmytro Nosan
 */
public class RackFileInitializerTests {

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void customize() throws Exception {
		Path directory = this.temporaryFolder.newFolder("conf").toPath();
		RackFileInitializer customizer =
				new RackFileInitializer(getClass().getResource("/cassandra-rackdc.properties"));
		customizer.initialize(directory.getParent(), new Version(3, 11, 3));
		try (InputStream inputStream = getClass().getResourceAsStream("/cassandra-rackdc.properties")) {
			assertThat(directory.resolve("cassandra-rackdc.properties")).hasBinaryContent(
					IOUtils.toByteArray(inputStream));
		}

	}

	@Test
	public void notCustomize() throws Exception {
		Path directory = this.temporaryFolder.newFolder("conf").toPath();
		RackFileInitializer customizer = new RackFileInitializer(null);
		customizer.initialize(directory.getParent(), new Version(3, 11, 3));
		assertThat(directory.resolve("cassandra-rackdc.properties")).doesNotExist();
	}
}
