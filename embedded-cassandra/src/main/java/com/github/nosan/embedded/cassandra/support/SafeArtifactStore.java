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

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;
import de.flapdoodle.embed.process.io.file.Files;
import de.flapdoodle.embed.process.store.IArtifactStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Safe {@link IArtifactStore} which removes safely files.
 *
 * @author Dmytro Nosan
 */
public class SafeArtifactStore implements IArtifactStore {


	private static final Logger log = LoggerFactory.getLogger(SafeArtifactStore.class);


	private final IArtifactStore delegate;

	public SafeArtifactStore(IArtifactStore delegate) {
		this.delegate = Objects.requireNonNull(delegate, "Artifact Store must not be null");
	}

	@Override
	public boolean checkDistribution(Distribution distribution) throws IOException {
		return this.delegate.checkDistribution(distribution);
	}

	@Override
	public IExtractedFileSet extractFileSet(Distribution distribution) throws IOException {
		return this.delegate.extractFileSet(distribution);
	}

	@Override
	public void removeFileSet(Distribution distribution, IExtractedFileSet files) {
		if (files.baseDirIsGenerated()) {
			try {
				Files.forceDelete(files.baseDir().toPath());
				return;
			}
			catch (IOException ex) {
				log.debug(ex.getMessage(), ex);
			}
		}
		for (FileType type : FileType.values()) {
			for (File file : files.files(type)) {
				try {
					Files.forceDelete(file.toPath());
				}
				catch (IOException ex) {
					log.debug(ex.getMessage(), ex);
				}
			}
		}
	}
}
