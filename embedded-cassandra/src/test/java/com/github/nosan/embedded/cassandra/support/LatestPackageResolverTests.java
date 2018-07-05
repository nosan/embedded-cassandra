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

package com.github.nosan.embedded.cassandra.support;

import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import org.junit.Test;

import com.github.nosan.embedded.cassandra.ExecutableVersion;
import com.github.nosan.embedded.cassandra.Version;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LatestPackageResolver}.
 *
 * @author Dmytro Nosan
 */
public class LatestPackageResolverTests {

	private final LatestPackageResolver packageResolver = new LatestPackageResolver();

	@Test
	public void getFileSet() {
		Distribution distribution = Distribution
				.detectFor(new ExecutableVersion(Version.LATEST));

		FileSet fileSet = this.packageResolver.getFileSet(distribution);

		assertThat(fileSet.entries()).hasSize(186);

	}

	@Test
	public void getArchiveType() {

		Distribution distribution = Distribution
				.detectFor(new ExecutableVersion(Version.LATEST));

		ArchiveType archiveType = this.packageResolver.getArchiveType(distribution);
		assertThat(archiveType).isEqualTo(ArchiveType.TGZ);
	}

	@Test
	public void getPath() {
		Distribution distribution = Distribution
				.detectFor(new ExecutableVersion(Version.LATEST));

		String path = this.packageResolver.getPath(distribution);

		assertThat(path)
				.isEqualTo("/cassandra/3.11.2/apache-cassandra-3.11.2-bin.tar.gz");
	}

}
