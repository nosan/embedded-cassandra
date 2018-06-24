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

import de.flapdoodle.embed.process.config.store.IDownloadConfig;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.io.progress.Slf4jProgressListener;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CassandraDownloadConfigBuilder}.
 *
 * @author Dmytro Nosan
 */
public class CassandraDownloadConfigBuilderTests {

	private static final Logger log = LoggerFactory
			.getLogger(CassandraDownloadConfigBuilderTests.class);

	@Test
	public void defaults() {

		IDownloadConfig downloadConfig = new CassandraDownloadConfigBuilder().defaults()
				.build();

		assertThat(downloadConfig.getArtifactStorePath().asFile().getAbsolutePath())
				.contains(".embedded-cassandra");
		assertThat(downloadConfig.getDownloadPath().getPath(null))
				.isEqualTo("http://apache.ip-connect.vn.ua");
		assertThat(downloadConfig.getFileNaming()).isInstanceOf(UUIDTempNaming.class);
		assertThat(downloadConfig.getDownloadPrefix())
				.isEqualTo("embedded-cassandra-download");
		assertThat(downloadConfig.getUserAgent()).isEqualTo(
				"Mozilla/5.0 (compatible; Embedded Cassandra; +https://github.com/nosan/embedded-cassandra)");
		assertThat(downloadConfig.getPackageResolver())
				.isInstanceOf(PackageResolverFactory.class);
		assertThat(downloadConfig.getProgressListener())
				.isInstanceOf(StandardConsoleProgressListener.class);
		assertThat(downloadConfig.getTimeoutConfig()).isNotNull();

	}

	@Test
	public void defaultsLogger() {

		IDownloadConfig downloadConfig = new CassandraDownloadConfigBuilder()
				.defaults(log).build();

		assertThat(downloadConfig.getArtifactStorePath().asFile().getAbsolutePath())
				.contains(".embedded-cassandra");
		assertThat(downloadConfig.getDownloadPath().getPath(null))
				.isEqualTo("http://apache.ip-connect.vn.ua");
		assertThat(downloadConfig.getFileNaming()).isInstanceOf(UUIDTempNaming.class);
		assertThat(downloadConfig.getDownloadPrefix())
				.isEqualTo("embedded-cassandra-download");
		assertThat(downloadConfig.getUserAgent()).isEqualTo(
				"Mozilla/5.0 (compatible; Embedded Cassandra; +https://github.com/nosan/embedded-cassandra)");
		assertThat(downloadConfig.getPackageResolver())
				.isInstanceOf(PackageResolverFactory.class);
		assertThat(downloadConfig.getProgressListener())
				.isInstanceOf(Slf4jProgressListener.class);
		assertThat(downloadConfig.getTimeoutConfig()).isNotNull();

	}

}
