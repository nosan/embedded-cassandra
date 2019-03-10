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

package com.github.nosan.embedded.cassandra.local.artifact;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.util.ArchiveUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EmptyArtifact}.
 *
 * @author Dmytro Nosan
 */
public class EmptyArtifactTests {

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	private final EmptyArtifact artifact = new EmptyArtifact(new Version(3, 11, 3));

	@Test
	public void shouldReturnAnEmptyArchive() throws IOException {
		Path archive = this.artifact.get();
		File temp = this.temporaryFolder.newFolder("temp");

		ArchiveUtils.extract(archive, temp.toPath());

		assertThat(temp).isDirectory();
		assertThat(temp.listFiles()).isEmpty();

	}

}
