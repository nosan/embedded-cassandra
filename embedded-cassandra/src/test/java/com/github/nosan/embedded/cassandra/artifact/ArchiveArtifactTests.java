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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.nosan.embedded.cassandra.api.Version;
import com.github.nosan.embedded.cassandra.commons.PathSupplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ArchiveArtifact}.
 *
 * @author Dmytro Nosan
 */
class ArchiveArtifactTests {

	private static final Version VERSION = Version.of("3.11.4");

	@Test
	void testArtifact(@TempDir Path temporaryFolder) throws Exception {
		ArchiveArtifact artifact = new ArchiveArtifact(VERSION, new OncePathSupplier());
		artifact.setExtractDirectory(temporaryFolder);
		assertResource(artifact.getResource());
		//reuse
		assertResource(artifact.getResource());
	}

	@Test
	void testArtifactThreads(@TempDir Path temporaryFolder) throws Exception {
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		ArchiveArtifact artifact = new ArchiveArtifact(VERSION, new OncePathSupplier());
		artifact.setExtractDirectory(temporaryFolder);
		try {
			CountDownLatch latch = new CountDownLatch(2);
			Future<Artifact.Resource> dir = executorService.submit(() -> {
				latch.countDown();
				return artifact.getResource();
			});
			Future<Artifact.Resource> dir1 = executorService.submit(() -> {
				latch.countDown();
				return artifact.getResource();
			});
			latch.await();
			assertResource(dir.get());
			assertResource(dir1.get());
		}
		finally {
			executorService.shutdown();
		}

	}

	private void assertResource(Artifact.Resource resource) {
		Path directory = resource.getDirectory();
		assertThat(resource.getVersion()).isEqualTo(VERSION);
		assertThat(directory.resolve("bin")).exists();
		assertThat(directory.resolve("lib")).exists();
		assertThat(directory.resolve("conf")).exists();
	}

	private static final class OncePathSupplier implements PathSupplier {

		private final AtomicBoolean alreadyCalled = new AtomicBoolean(false);

		@Override
		public Path get() throws Exception {
			if (this.alreadyCalled.compareAndSet(false, true)) {
				return Paths.get(getClass().getResource("/apache-cassandra-3.11.4-bin.tar.gz").toURI());
			}
			throw new IllegalStateException("Path Supplier is called more than one time");
		}

	}

}
