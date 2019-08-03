/*
 * Copyright 2018-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nosan.embedded.cassandra.artifact;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.api.Version;
import com.github.nosan.embedded.cassandra.commons.FileLock;
import com.github.nosan.embedded.cassandra.commons.PathSupplier;
import com.github.nosan.embedded.cassandra.commons.util.FileUtils;

/**
 * An artifact that provides {@link Artifact.Resource} based on the specified archive file and version.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public class ArchiveArtifact implements Artifact {

	private static final Logger log = LoggerFactory.getLogger(ArchiveArtifact.class);

	private final Version version;

	private final PathSupplier archiveSupplier;

	@Nullable
	private Path extractDirectory;

	/**
	 * Constructs a new {@link ArchiveArtifact} with the specified archive file and version.
	 *
	 * @param version the version
	 * @param archiveSupplier the supplier to get a path to the archive
	 */
	public ArchiveArtifact(Version version, PathSupplier archiveSupplier) {
		this.version = Objects.requireNonNull(version, "'version' must not be null");
		this.archiveSupplier = Objects.requireNonNull(archiveSupplier, "'archiveSupplier' must not be null");
	}

	/**
	 * Constructs a new {@link ArchiveArtifact} with the specified archive file, version and extract directory.
	 *
	 * @param version the version
	 * @param archiveSupplier the supplier to get a path to the archive
	 * @param extractDirectory the directory to extract an archive file
	 */
	public ArchiveArtifact(Version version, @Nullable Path extractDirectory, PathSupplier archiveSupplier) {
		this.version = Objects.requireNonNull(version, "'version' must not be null");
		this.archiveSupplier = Objects.requireNonNull(archiveSupplier, "'archiveSupplier' must not be null");
		this.extractDirectory = extractDirectory;
	}

	/**
	 * Directory used to extract an archive file. Defaults to {@code {user.home}/.embeddedCassandra/artifact}
	 *
	 * @return the directory
	 */
	@Nullable
	public Path getExtractDirectory() {
		return this.extractDirectory;
	}

	/**
	 * Sets directory used to extract an archive file.
	 *
	 * @param extractDirectory the directory to extract an archive file
	 */
	public void setExtractDirectory(@Nullable Path extractDirectory) {
		this.extractDirectory = extractDirectory;
	}

	/**
	 * Returns Cassandra's version.
	 *
	 * @return the version
	 */
	public Version getVersion() {
		return this.version;
	}

	/**
	 * Returns archive supplier.
	 *
	 * @return the supplier to get a path to the archive
	 */
	public PathSupplier getArchiveSupplier() {
		return this.archiveSupplier;
	}

	@Override
	public Resource getResource() throws Exception {
		Path archiveDirectory = this.extractDirectory;
		if (archiveDirectory == null) {
			archiveDirectory = FileUtils.getUserHome().resolve(".embeddedCassandra/artifact");
		}
		Path archiveDir = archiveDirectory.resolve(this.version.toString());
		DefaultArtifact artifact = new DefaultArtifact(this.version, archiveDir);
		if (!Files.exists(archiveDir.resolve(".extracted"))) {
			return extract(archiveDir, artifact);
		}
		return artifact.getResource();
	}

	private Resource extract(Path archiveDir, DefaultArtifact artifact) throws Exception {
		Files.createDirectories(archiveDir);
		Path lockFile = archiveDir.resolve(".lock");
		try (FileLock fileLock = FileLock.of(lockFile)) {
			log.info("Acquires a lock to the file '{}' ...", lockFile);
			if (!fileLock.tryLock(5, TimeUnit.MINUTES)) {
				throw new IllegalStateException("File lock cannot be acquired for a file '" + lockFile + "'");
			}
			log.info("The lock to the file '{}' was acquired", lockFile);
			if (!Files.exists(archiveDir.resolve(".extracted"))) {
				Path archiveFile = this.archiveSupplier.get();
				if (archiveFile == null) {
					throw new IllegalStateException("Archive file must not be null");
				}
				Archiver archiver = ArchiverFactory.createArchiver(archiveFile.toFile());
				log.info("Extracts '{}' file into '{}' directory", archiveFile, archiveDir);
				archiver.extract(archiveFile.toFile(), archiveDir.toFile());
				Resource resource = artifact.getResource();
				FileUtils.createIfNotExists(archiveDir.resolve(".extracted"));
				return resource;
			}
		}
		return artifact.getResource();
	}

}
