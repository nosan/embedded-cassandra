/*
 * Copyright 2018-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ArchiveUtils}.
 *
 * @author Dmytro Nosan
 */
@RunWith(Parameterized.class)
public class ArchiveUtilsTests {

	private final String name;

	private final String archiveFormat;

	private final String compression;

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	public ArchiveUtilsTests(String name, String archiveFormat, String compression) {
		this.name = name;
		this.archiveFormat = archiveFormat;
		this.compression = compression;
	}

	@Test
	public void extract() throws Exception {
		File archive = this.temporaryFolder.newFile(this.name);
		File cassandra = new File(getClass().getResource("/cassandra.yaml").toURI());
		archive(this.archiveFormat, archive, cassandra);
		compress(this.compression, archive);
		File destination = this.temporaryFolder.newFolder();
		ArchiveUtils.extract(archive.toPath(), destination.toPath(), null);
		assertThat(destination.toPath().resolve("dir/cass.yaml").toFile())
				.hasSameContentAs(cassandra);

	}

	private static void archive(String archiveFormat, File archive, File cassandra) throws Exception {
		ArchiveStreamFactory archiveStreamFactory = new ArchiveStreamFactory();
		try (ArchiveOutputStream os = archiveStreamFactory
				.createArchiveOutputStream(archiveFormat, Files.newOutputStream(archive.toPath()))) {
			ArchiveEntry archiveEntry = os.createArchiveEntry(cassandra, "dir/cass.yaml");
			os.putArchiveEntry(archiveEntry);
			try (InputStream is = Files.newInputStream(cassandra.toPath())) {
				os.write(IOUtils.toByteArray(is));
			}
			os.closeArchiveEntry();
		}
	}


	private static void compress(String compression, File archive) throws Exception {
		if (StringUtils.hasText(compression)) {
			byte[] content;
			try (InputStream is = Files.newInputStream(archive.toPath())) {
				content = IOUtils.toByteArray(is);
			}
			CompressorStreamFactory factory = new CompressorStreamFactory();
			try (OutputStream os = factory
					.createCompressorOutputStream(compression, Files.newOutputStream(archive.toPath()))) {
				IOUtils.copy(new ByteArrayInputStream(content), os);
			}
		}
	}

	@Parameterized.Parameters(name = "{0}")
	public static Iterable<Object[]> archives() {
		List<Object[]> parameters = new ArrayList<>();
		parameters.add(new Object[]{".tar.gz", ArchiveStreamFactory.TAR, CompressorStreamFactory.GZIP});
		parameters.add(new Object[]{".tgz", ArchiveStreamFactory.TAR, CompressorStreamFactory.GZIP});
		parameters.add(new Object[]{".tar.bz2", ArchiveStreamFactory.TAR, CompressorStreamFactory.BZIP2});
		parameters.add(new Object[]{".tbz2", ArchiveStreamFactory.TAR, CompressorStreamFactory.BZIP2});
		parameters.add(new Object[]{".a", ArchiveStreamFactory.AR, null});
		parameters.add(new Object[]{".ar", ArchiveStreamFactory.AR, null});
		parameters.add(new Object[]{".cpio", ArchiveStreamFactory.CPIO, null});
		parameters.add(new Object[]{".jar", ArchiveStreamFactory.JAR, null});
		parameters.add(new Object[]{".tar", ArchiveStreamFactory.TAR, null});
		parameters.add(new Object[]{".zip", ArchiveStreamFactory.ZIP, null});
		parameters.add(new Object[]{".zipx", ArchiveStreamFactory.ZIP, null});
		return parameters;
	}


}
