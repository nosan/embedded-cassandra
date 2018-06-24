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

import de.flapdoodle.embed.process.config.store.DownloadConfigBuilder;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.io.directories.UserHome;
import de.flapdoodle.embed.process.io.progress.Slf4jProgressListener;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import org.slf4j.Logger;

/**
 * {@link DownloadConfigBuilder} builder with defaults methods.
 *
 * @author Dmytro Nosan
 */
public class CassandraDownloadConfigBuilder extends DownloadConfigBuilder {

	private static final String USER_AGENT = "Mozilla/5.0 (compatible; Embedded Cassandra; "
			+ "+https://github" + ".com/nosan/embedded-cassandra)";

	private static final String DOWNLOAD_PATH = "http://apache.ip-connect.vn.ua";

	private static final String DOWNLOAD_PREFIX = "embedded-cassandra-download";

	private static final UserHome ARTIFACT_STORE_PATH = new UserHome(
			".embedded-cassandra");

	/**
	 * Configure builder with default settings. {@link StandardConsoleProgressListener}
	 * listener will be used.
	 * @return builder with defaults settings.
	 */
	public CassandraDownloadConfigBuilder defaults() {
		fileNaming(new UUIDTempNaming());
		downloadPath(DOWNLOAD_PATH);
		progressListener(new StandardConsoleProgressListener());
		artifactStorePath(ARTIFACT_STORE_PATH);
		downloadPrefix(DOWNLOAD_PREFIX);
		packageResolver(new PackageResolverFactory());
		userAgent(USER_AGENT);
		return this;
	}

	/**
	 * Configure builder with default settings and logger.
	 * @param logger logger for process outputs.
	 * @return builder with defaults settings.
	 */
	public CassandraDownloadConfigBuilder defaults(Logger logger) {
		Objects.requireNonNull(logger, "Logger must not be null");
		fileNaming(new UUIDTempNaming());
		downloadPath(DOWNLOAD_PATH);
		progressListener(new Slf4jProgressListener(logger));
		artifactStorePath(ARTIFACT_STORE_PATH);
		downloadPrefix(DOWNLOAD_PREFIX);
		packageResolver(new PackageResolverFactory());
		userAgent(USER_AGENT);
		return this;
	}

}
