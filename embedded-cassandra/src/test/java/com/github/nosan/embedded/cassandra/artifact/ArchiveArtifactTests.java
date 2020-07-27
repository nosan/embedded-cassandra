/*
 * Copyright 2018-2020 the original author or authors.
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.nosan.embedded.cassandra.api.Version;
import com.github.nosan.embedded.cassandra.commons.io.ClassPathResource;
import com.github.nosan.embedded.cassandra.commons.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ArchiveArtifact}.
 *
 * @author Dmytro Nosan
 */
class ArchiveArtifactTests {

	private static final Version VERSION = Version.of("4.0-beta1");

	@Test
	void testArtifact(@TempDir Path temporaryFolder) throws Exception {
		ArchiveArtifact artifact = new ArchiveArtifact(VERSION, new OnceResource());
		artifact.setDestination(temporaryFolder);
		assertDistribution(artifact.getDistribution());
		//reuse
		assertDistribution(artifact.getDistribution());
	}

	@Test
	void testArtifactThreads(@TempDir Path temporaryFolder) throws Exception {
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		ArchiveArtifact artifact = new ArchiveArtifact(VERSION, new OnceResource());
		artifact.setDestination(temporaryFolder);

		try {
			CountDownLatch latch = new CountDownLatch(2);
			Future<Artifact.Distribution> distribution1 = executorService.submit(() -> {
				latch.countDown();
				return artifact.getDistribution();
			});
			Future<Artifact.Distribution> distribution2 = executorService.submit(() -> {
				latch.countDown();
				return artifact.getDistribution();
			});
			latch.await();
			assertDistribution(distribution1.get());
			assertDistribution(distribution2.get());
		}
		finally {
			executorService.shutdown();
		}

	}

	private void assertDistribution(Artifact.Distribution distribution) {
		Path directory = distribution.getDirectory();
		assertThat(distribution.getVersion()).isEqualTo(VERSION);
		assertThat(directory.resolve("bin")).exists();
		assertThat(directory.resolve("lib")).exists();
		assertThat(directory.resolve("conf")).exists();
	}

	private static final class OnceResource implements Resource {

		private final AtomicBoolean alreadyCalled = new AtomicBoolean(false);

		private final ClassPathResource resource = new ClassPathResource("apache-cassandra-4.0-beta1-bin.tar.gz");

		@Override
		public String getFileName() {
			return this.resource.getFileName();
		}

		@Override
		public boolean exists() {
			return this.resource.exists();
		}

		@Override
		public InputStream getInputStream() throws IOException {
			if (this.alreadyCalled.compareAndSet(false, true)) {
				return this.resource.getInputStream();
			}
			throw new IllegalStateException("Already called");
		}

		@Override
		public URL toURL() throws IOException {
			return this.resource.toURL();
		}

	}

}
