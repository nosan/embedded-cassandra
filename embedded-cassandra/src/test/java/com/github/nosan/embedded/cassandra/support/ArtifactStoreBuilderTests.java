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

import de.flapdoodle.embed.process.io.directories.PropertyOrTempDirInPlatformTempDir;
import de.flapdoodle.embed.process.io.progress.Slf4jProgressListener;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import de.flapdoodle.embed.process.store.Downloader;
import de.flapdoodle.embed.process.store.IArtifactStore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ArtifactStoreBuilder}.
 *
 * @author Dmytro Nosan
 */
public class ArtifactStoreBuilderTests {

	private static final Logger log = LoggerFactory.getLogger(ArtifactStoreBuilderTests.class);

	@Test
	public void defaults() throws Exception {
		IArtifactStore artifactStore =
				(IArtifactStore) ReflectionUtils.getField(new ArtifactStoreBuilder().build(), "delegate");

		assertThat(ReflectionUtils.getField(artifactStore, "_downloader"))
				.isInstanceOf(Downloader.class);

		assertThat(ReflectionUtils.getField(artifactStore, "_executableNaming"))
				.isInstanceOf(OriginTempNaming.class);

		assertThat(ReflectionUtils.getField(artifactStore, "_tempDirFactory"))
				.isInstanceOf(PropertyOrTempDirInPlatformTempDir.class);

		Object downloadConfig = ReflectionUtils.getField(artifactStore,
				"_downloadConfig");

		assertThat(downloadConfig).isNotNull();

		assertThat(ReflectionUtils.getField(downloadConfig, "_progressListener"))
				.isInstanceOf(StandardConsoleProgressListener.class);

	}

	@Test
	public void defaultsLogger() throws Exception {

		IArtifactStore artifactStore =
				(IArtifactStore) ReflectionUtils.getField(new ArtifactStoreBuilder(log).build(), "delegate");


		assertThat(ReflectionUtils.getField(artifactStore, "_downloader"))
				.isInstanceOf(Downloader.class);

		assertThat(ReflectionUtils.getField(artifactStore, "_executableNaming"))
				.isInstanceOf(OriginTempNaming.class);

		assertThat(ReflectionUtils.getField(artifactStore, "_tempDirFactory"))
				.isInstanceOf(PropertyOrTempDirInPlatformTempDir.class);

		Object downloadConfig = ReflectionUtils.getField(artifactStore, "_downloadConfig");

		assertThat(downloadConfig).isNotNull();

		assertThat(ReflectionUtils.getField(downloadConfig, "_progressListener"))
				.isInstanceOf(Slf4jProgressListener.class);
	}


}
