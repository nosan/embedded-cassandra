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

import com.github.nosan.embedded.cassandra.CassandraVersion;
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
	 * Configure builder with default settings for particular version.
	 * @param version cassandra version.
	 * @return builder with defaults settings.
	 */
	public CassandraArtifactStoreBuilder defaults(CassandraVersion version) {
		executableNaming().overwriteDefault((prefix, postfix) -> postfix);
		tempDir().overwriteDefault(new PropertyOrTempDirInPlatformTempDir());
		downloader().overwriteDefault(new Downloader());
		download().overwriteDefault(
				new CassandraDownloadConfigBuilder().defaults(version).build());
		return this;
	}

	/**
	 * Configure builder with default settings for particular version and logger.
	 * @param version cassandra version.
	 * @param logger logger for process outputs.
	 * @return builder with defaults settings.
	 */
	public CassandraArtifactStoreBuilder defaults(CassandraVersion version,
			Logger logger) {
		executableNaming().overwriteDefault((prefix, postfix) -> postfix);
		tempDir().overwriteDefault(new PropertyOrTempDirInPlatformTempDir());
		downloader().overwriteDefault(new Downloader());
		download().overwriteDefault(
				new CassandraDownloadConfigBuilder().defaults(version, logger).build());
		return this;
	}

}
