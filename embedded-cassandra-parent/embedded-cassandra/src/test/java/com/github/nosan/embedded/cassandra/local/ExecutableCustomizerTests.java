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

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.nosan.embedded.cassandra.util.OS;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ExecutableCustomizer}.
 *
 * @author Dmytro Nosan
 */
public class ExecutableCustomizerTests {

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	private final ExecutableCustomizer customizer = new ExecutableCustomizer();

	@Test
	public void setExecutableUnixFile() throws IOException {
		if (OS.get() != OS.WINDOWS) {
			File file = createFile("cassandra");
			this.customizer.customize(this.temporaryFolder.getRoot().toPath());
			assertThat(file.canExecute()).isTrue();
		}
	}

	private File createFile(String name) throws IOException {
		TemporaryFolder temporaryFolder = this.temporaryFolder;
		File file = new File(temporaryFolder.newFolder("bin"), name);
		assertThat(file.createNewFile()).isTrue();
		assertThat(file.setExecutable(false)).isTrue();
		return file;
	}
}
