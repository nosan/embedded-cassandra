/*
 * Copyright 2018-2019 the original author or authors.
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

package com.github.nosan.embedded.cassandra.local;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link DirectoryCustomizer} to initialize {@code commitlog_archiving.properties}.
 *
 * @author Dmytro Nosan
 * @since 1.2.8
 */
class CommitLogArchivingFileCustomizer implements DirectoryCustomizer {

	private static final Logger log = LoggerFactory.getLogger(CommitLogArchivingFileCustomizer.class);

	@Nullable
	private final URL commitLogArchivingFile;

	/**
	 * Creates a {@link CommitLogArchivingFileCustomizer}.
	 *
	 * @param commitLogArchivingFile URL to {@code commitlog_archiving.properties}
	 */
	CommitLogArchivingFileCustomizer(@Nullable URL commitLogArchivingFile) {
		this.commitLogArchivingFile = commitLogArchivingFile;
	}

	@Override
	public void customize(@Nonnull Path directory) throws IOException {
		URL commitLogArchivingFile = this.commitLogArchivingFile;
		if (commitLogArchivingFile != null) {
			Path target = directory.resolve("conf/commitlog_archiving.properties");
			if (log.isDebugEnabled()) {
				log.debug("Replace ({}) with ({})", target, commitLogArchivingFile);
			}

			try (InputStream is = commitLogArchivingFile.openStream()) {
				Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
			}
			catch (IOException ex) {
				throw new IOException(String.format("Commit Log Archiving Properties (%s) could not be saved",
						commitLogArchivingFile), ex);
			}
		}

	}
}

