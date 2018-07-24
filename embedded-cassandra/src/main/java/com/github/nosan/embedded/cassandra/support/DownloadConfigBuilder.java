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

import java.util.Objects;

import de.flapdoodle.embed.process.config.store.DownloadPath;
import de.flapdoodle.embed.process.config.store.TimeoutConfigBuilder;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.io.directories.UserHome;
import de.flapdoodle.embed.process.io.progress.IProgressListener;
import de.flapdoodle.embed.process.io.progress.Slf4jProgressListener;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import org.slf4j.Logger;

/**
 * {@link DownloadConfigBuilder Download Config Builder } with default behaviour.
 *
 * @author Dmytro Nosan
 */
public class DownloadConfigBuilder extends de.flapdoodle.embed.process.config.store.DownloadConfigBuilder {

	private static final String USER_AGENT = "Mozilla/5.0 (compatible; Embedded Cassandra; "
			+ "+https://github.com/nosan/embedded-cassandra)";

	private static final String DOWNLOAD_PATH = "http://www-eu.apache.org/dist";

	private static final String DOWNLOAD_PREFIX = "embedded-cassandra-download";

	private static final UserHome ARTIFACT_STORE_PATH = new UserHome(
			".embedded-cassandra");

	public DownloadConfigBuilder() {
		this(new StandardConsoleProgressListener());
	}

	public DownloadConfigBuilder(Logger logger) {
		this(new Slf4jProgressListener(
				Objects.requireNonNull(logger, "Logger must not be null")));
	}

	private DownloadConfigBuilder(IProgressListener progressListener) {
		fileNaming().overwriteDefault(new UUIDTempNaming());
		downloadPath().overwriteDefault(new DownloadPath(DOWNLOAD_PATH));
		progressListener().overwriteDefault(progressListener);
		artifactStorePath().overwriteDefault(ARTIFACT_STORE_PATH);
		downloadPrefix().overwriteDefault(new DownloadPrefix(DOWNLOAD_PREFIX));
		packageResolver().overwriteDefault(new PackageResolverFactory());
		userAgent().overwriteDefault(new UserAgent(USER_AGENT));
		timeoutConfig().overwriteDefault(new TimeoutConfigBuilder()
				.connectionTimeout(30000).readTimeout(30000).build());
	}

}
