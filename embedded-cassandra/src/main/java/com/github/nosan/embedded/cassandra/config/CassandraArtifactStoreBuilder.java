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

import java.util.Objects;

import de.flapdoodle.embed.process.config.store.IDownloadConfig;
import de.flapdoodle.embed.process.io.directories.PropertyOrTempDirInPlatformTempDir;
import de.flapdoodle.embed.process.store.ArtifactStoreBuilder;
import de.flapdoodle.embed.process.store.Downloader;
import org.slf4j.Logger;

/**
 * {@link ArtifactStoreBuilder} builder with defaults methods.
 *
 * @author Dmytro Nosan
 */
public class CassandraArtifactStoreBuilder extends ArtifactStoreBuilder {

	/**
	 * Configure builder with default settings.
	 */
	public CassandraArtifactStoreBuilder() {
		this(new CassandraDownloadConfigBuilder().build());
	}

	/**
	 * Configure builder with default settings and logger.
	 * @param logger logger for process outputs.
	 */
	public CassandraArtifactStoreBuilder(Logger logger) {
		this(new CassandraDownloadConfigBuilder(
				Objects.requireNonNull(logger, "Logger must not be null")).build());
	}

	private CassandraArtifactStoreBuilder(IDownloadConfig downloadConfig) {
		executableNaming().overwriteDefault(new OriginTempNaming());
		tempDir().overwriteDefault(new PropertyOrTempDirInPlatformTempDir());
		downloader().overwriteDefault(new Downloader());
		download().overwriteDefault(downloadConfig);
	}

}
