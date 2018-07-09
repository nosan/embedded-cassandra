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
import org.assertj.core.api.Assertions;
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

	private static final Logger log = LoggerFactory
			.getLogger(ArtifactStoreBuilderTests.class);

	@Test
	public void defaults() throws Exception {
		IArtifactStore artifactStore = new ArtifactStoreBuilder().build();

		Assertions.assertThat(ReflectionUtils.getField("_downloader", artifactStore))
				.isInstanceOf(Downloader.class);

		assertThat(ReflectionUtils.getField("_executableNaming", artifactStore))
				.isInstanceOf(OriginTempNaming.class);

		assertThat(ReflectionUtils.getField("_tempDirFactory", artifactStore))
				.isInstanceOf(PropertyOrTempDirInPlatformTempDir.class);

		Object downloadConfig = ReflectionUtils.getField("_downloadConfig",
				artifactStore);

		assertThat(downloadConfig).isNotNull();

		assertThat(ReflectionUtils.getField("_progressListener", downloadConfig))
				.isInstanceOf(StandardConsoleProgressListener.class);

	}

	@Test
	public void defaultsLogger() throws Exception {

		IArtifactStore artifactStore = new ArtifactStoreBuilder(log).build();

		assertThat(ReflectionUtils.getField("_downloader", artifactStore))
				.isInstanceOf(Downloader.class);

		assertThat(ReflectionUtils.getField("_executableNaming", artifactStore))
				.isInstanceOf(OriginTempNaming.class);

		assertThat(ReflectionUtils.getField("_tempDirFactory", artifactStore))
				.isInstanceOf(PropertyOrTempDirInPlatformTempDir.class);

		Object downloadConfig = ReflectionUtils.getField("_downloadConfig",
				artifactStore);

		assertThat(downloadConfig).isNotNull();

		assertThat(ReflectionUtils.getField("_progressListener", downloadConfig))
				.isInstanceOf(Slf4jProgressListener.class);
	}


}
