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

package com.github.nosan.embedded.cassandra.config;

import com.github.nosan.embedded.cassandra.ReflectionUtils;
import de.flapdoodle.embed.process.io.directories.PropertyOrTempDirInPlatformTempDir;
import de.flapdoodle.embed.process.io.progress.Slf4jProgressListener;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import de.flapdoodle.embed.process.store.CachingArtifactStore;
import de.flapdoodle.embed.process.store.Downloader;
import de.flapdoodle.embed.process.store.IArtifactStore;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CassandraArtifactStoreBuilder}.
 *
 * @author Dmytro Nosan
 */
public class CassandraArtifactStoreBuilderTests {

	@Test
	public void defaults() throws Exception {
		IArtifactStore artifactStore = unwrap(
				new CassandraArtifactStoreBuilder().defaults().build());

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
				.isInstanceOf(StandardConsoleProgressListener.class);

	}

	@Test
	public void defaultsLogger() throws Exception {

		Logger logger = Mockito.mock(Logger.class);

		IArtifactStore artifactStore = unwrap(
				new CassandraArtifactStoreBuilder().defaults(logger).build());

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

	private IArtifactStore unwrap(IArtifactStore store)
			throws NoSuchFieldException, IllegalAccessException {
		assertThat(store).isInstanceOf(CachingArtifactStore.class);
		CachingArtifactStore cachingArtifactStore = (CachingArtifactStore) store;

		return (IArtifactStore) ReflectionUtils.getField("_delegate",
				cachingArtifactStore);

	}

}
