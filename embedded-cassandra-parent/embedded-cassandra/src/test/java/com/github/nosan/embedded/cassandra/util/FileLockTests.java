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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FileLock}.
 *
 * @author Dmytro Nosan
 */
class FileLockTests {

	@Test
	void sequenceAccess(@TempDir Path temporaryFolder) throws InterruptedException {
		long start = System.currentTimeMillis();
		Path lockFile = temporaryFolder.resolve(String.format("%s.lock", UUID.randomUUID()));
		CountDownLatch latch = new CountDownLatch(2);
		List<Exception> exceptions = new ArrayList<>();
		Runnable runnable = () -> {
			try (FileLock fileLock = new FileLock(lockFile)) {
				fileLock.lock();
				Thread.sleep(1500);
			}
			catch (Exception ex) {
				exceptions.add(ex);
			}
			finally {
				latch.countDown();
			}
		};
		new Thread(runnable).start();
		new Thread(runnable).start();
		latch.await();
		long elapsed = System.currentTimeMillis() - start;
		assertThat(exceptions).isEmpty();
		assertThat(elapsed).isGreaterThan(3000);

	}

}
