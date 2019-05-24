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

package com.github.nosan.embedded.cassandra.local;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLockInterruptionException;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * Utility class to lock a file.
 *
 * @author Dmytro Nosan
 * @see java.nio.channels.FileLock
 * @since 1.4.2
 */
class FileLock implements AutoCloseable {

	private static final Logger log = LoggerFactory.getLogger(FileLock.class);

	private final Path file;

	@Nullable
	private FileChannel fileChannel;

	@Nullable
	private java.nio.channels.FileLock fileLock;

	/**
	 * Creates a new {@link FileLock}.
	 *
	 * @param file the lock file.
	 */
	FileLock(Path file) {
		this.file = file;
	}

	/**
	 * Releases this lock.
	 */
	@Override
	public void close() {
		close(this.fileLock, () -> String.format("Can not close a file lock '%s'", this.fileLock));
		close(this.fileChannel, () -> String.format("Can not close a file channel '%s'", this.fileChannel));
	}

	/**
	 * Acquires an exclusive lock on the file.
	 *
	 * @throws java.nio.channels.FileLockInterruptionException If the invoking thread is interrupted while blocked in
	 * this method
	 * @throws java.io.IOException If some other I/O error occurs
	 */
	void lock() throws IOException, FileLockInterruptionException {
		Path file = this.file;
		log.info("Acquires a lock to the file '{}' ...", file);
		FileChannel fileChannel = await(3, TimeUnit.SECONDS, () -> open(file),
				() -> String.format("File lock for a file '%s' has not been acquired because "
						+ "FileChannel.open(...) was not created.", file));
		this.fileChannel = fileChannel;
		this.fileLock = await(5, TimeUnit.MINUTES, () -> lock(fileChannel), () -> String.format("File lock for "
				+ "a file '%s' has not been acquired because FileChannel.lock() was not created.", file));
		log.info("The lock to the file '{}' was acquired", file);
	}

	@Nullable
	private static FileChannel open(Path file) {
		try {
			return FileChannel.open(file, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
		}
		catch (IOException ex) {
			log.error(String.format("Can not open a file channel to a file '%s'.", file), ex);
		}
		return null;
	}

	@Nullable
	private static java.nio.channels.FileLock lock(FileChannel fileChannel) throws IOException {
		try {
			return fileChannel.lock();
		}
		catch (OverlappingFileLockException ex) {
			return null;
		}
	}

	private static void close(@Nullable AutoCloseable closeable, Supplier<String> message) {
		if (closeable != null) {
			try {
				closeable.close();
			}
			catch (Exception ex) {
				log.error(message.get(), ex);
			}
		}
	}

	private static <T> T await(long timeout, TimeUnit unit, Callable<T> callback,
			Supplier<String> message) throws IOException {
		long startTime = System.nanoTime();
		long rem = unit.toNanos(timeout);
		do {
			T result = callback.call();
			if (result != null) {
				return result;
			}
			if (rem > 0) {
				sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 1000));
			}
			rem = unit.toNanos(timeout) - (System.nanoTime() - startTime);
		} while (rem > 0);
		throw new IOException(message.get());
	}

	private static void sleep(long millis) throws FileLockInterruptionException {
		try {
			Thread.sleep(millis);
		}
		catch (InterruptedException ex) {
			throw new FileLockInterruptionException();
		}
	}

	@FunctionalInterface
	private interface Callable<T> {

		@Nullable
		T call() throws IOException;

	}

}
