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

package com.github.nosan.embedded.cassandra.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ArchiveUtils}.
 *
 * @author Dmytro Nosan
 */
class ArchiveUtilsTests {

	@ParameterizedTest
	@MethodSource("archives")
	void extract(String name, String archiveFormat, String compression, @TempDir Path temporaryFolder)
			throws Exception {
		Path archive = temporaryFolder.resolve(String.format("%s.%s",
				UUID.randomUUID(), name));
		File file = new File(getClass().getResource("/cassandra.yaml").toURI());
		archive(archiveFormat, archive, file);
		compress(compression, archive);
		Path destination = temporaryFolder.resolve(UUID.randomUUID().toString());
		ArchiveUtils.extract(archive, destination);
		assertThat(destination.resolve("cassandra.yaml").toFile()).hasSameContentAs(file);
	}

	static Stream<Arguments> archives() {
		List<Arguments> parameters = new ArrayList<>();
		parameters.add(Arguments.arguments("tar.gz", ArchiveStreamFactory.TAR, CompressorStreamFactory.GZIP));
		parameters.add(Arguments.arguments("tgz", ArchiveStreamFactory.TAR, CompressorStreamFactory.GZIP));
		parameters.add(Arguments.arguments("tar.bz2", ArchiveStreamFactory.TAR, CompressorStreamFactory.BZIP2));
		parameters.add(Arguments.arguments("tar.xz", ArchiveStreamFactory.TAR, CompressorStreamFactory.XZ));
		parameters.add(Arguments.arguments("txz", ArchiveStreamFactory.TAR, CompressorStreamFactory.XZ));
		parameters.add(Arguments.arguments("tbz2", ArchiveStreamFactory.TAR, CompressorStreamFactory.BZIP2));
		parameters.add(Arguments.arguments("a", ArchiveStreamFactory.AR, null));
		parameters.add(Arguments.arguments("ar", ArchiveStreamFactory.AR, null));
		parameters.add(Arguments.arguments("cpio", ArchiveStreamFactory.CPIO, null));
		parameters.add(Arguments.arguments("jar", ArchiveStreamFactory.JAR, null));
		parameters.add(Arguments.arguments("tar", ArchiveStreamFactory.TAR, null));
		parameters.add(Arguments.arguments("zip", ArchiveStreamFactory.ZIP, null));
		parameters.add(Arguments.arguments("zipx", ArchiveStreamFactory.ZIP, null));
		return parameters.stream();
	}

	private static void archive(String archiveFormat, Path archive, File file) throws Exception {
		ArchiveStreamFactory af = new ArchiveStreamFactory();
		try (ArchiveOutputStream os = af.createArchiveOutputStream(archiveFormat,
				Files.newOutputStream(archive))) {
			ArchiveEntry archiveEntry = os.createArchiveEntry(file, "cassandra.yaml");
			os.putArchiveEntry(archiveEntry);
			try (InputStream is = Files.newInputStream(file.toPath())) {
				os.write(IOUtils.toByteArray(is));
			}
			os.closeArchiveEntry();
		}
	}

	private static void compress(String compression, Path archive) throws Exception {
		if (StringUtils.hasText(compression)) {
			byte[] content;
			try (InputStream is = Files.newInputStream(archive)) {
				content = IOUtils.toByteArray(is);
			}
			CompressorStreamFactory cf = new CompressorStreamFactory();
			try (OutputStream os = cf.createCompressorOutputStream(compression,
					Files.newOutputStream(archive))) {
				IOUtils.copy(new ByteArrayInputStream(content), os);
			}
		}
	}

}
