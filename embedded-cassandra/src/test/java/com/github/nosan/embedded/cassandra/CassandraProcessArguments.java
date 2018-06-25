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

package com.github.nosan.embedded.cassandra;

import java.io.File;

import com.github.nosan.embedded.cassandra.config.Version;
import de.flapdoodle.embed.process.distribution.BitSize;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.extract.ImmutableExtractedFileSet;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CassandraProcess.ProcessArguments}.
 *
 * @author Dmytro Nosan
 */
public class CassandraProcessArguments {

	@Test
	public void unixLike() {

		Distribution distribution = new Distribution(Version.LATEST, Platform.Linux,
				BitSize.detect());
		File executable = new File("exe");
		File configurationFile = new File("config.yaml");

		CassandraProcess.ProcessArguments processArguments = new CassandraProcess.ProcessArguments(
				distribution, ImmutableExtractedFileSet.builder(new File(""))
						.executable(executable).build(),
				configurationFile);

		assertThat(processArguments.get()).containsExactly(executable.getAbsolutePath(),
				"-f",
				"-Dcassandra.config=file:" + StringUtils.repeat(File.separatorChar, 3)
						+ configurationFile.getAbsolutePath());

	}

	@Test
	public void windows() {

		Distribution distribution = new Distribution(Version.LATEST, Platform.Windows,
				BitSize.detect());
		File executable = new File("exe");
		File configurationFile = new File("config.yaml");

		CassandraProcess.ProcessArguments processArguments = new CassandraProcess.ProcessArguments(
				distribution, ImmutableExtractedFileSet.builder(new File(""))
						.executable(executable).build(),
				configurationFile);

		assertThat(processArguments.get()).containsExactly("powershell",
				"-ExecutionPolicy", "Bypass", executable.getAbsolutePath(), "-f",
				"`-Dcassandra.config=file:" + StringUtils.repeat(File.separatorChar, 3)
						+ configurationFile.getAbsolutePath());

	}

}
