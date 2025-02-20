/*
 * Copyright 2020-2025 the original author or authors.
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

package com.github.nosan.embedded.cassandra.commons;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLockInterruptionException;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for obtaining an exclusive lock on a file.
 *
 * <p>The {@code FileLock} class simplifies the process of locking files, allowing controlled access
 * to shared resources in a multi-threaded environment. It uses Java NIO's {@link java.nio.channels.FileLock} to handle
 * file locks.</p>
 *
 * <p><strong>Usage:</strong></p>
 * <p>It is recommended to use {@link FileLock} with a try-with-resources block to ensure that
 * any underlying file resources are properly released:</p>
 *
 * <pre>{@code
 * Path lockFile = Path.of("example.lock");
 * try (FileLock lock = FileLock.of(lockFile)) {
 *     if (lock.tryLock(1, TimeUnit.MINUTES)) {
 *         // Perform actions while the file is locked
 *     } else {
 *         // Handle inability to acquire lock
 *     }
 * }
 * }</pre>
 *
 * <p><b>Note:</b> An instance of this class should not be shared across different threads.</p>
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public final class FileLock implements AutoCloseable {

	private final FileChannel fileChannel;

	private final Map<Thread, java.nio.channels.FileLock> locks = new ConcurrentHashMap<>();

	private FileLock(FileChannel fileChannel) {
		this.fileChannel = fileChannel;
	}

	/**
	 * Creates a {@link FileLock} instance for the specified file.
	 *
	 * <p>The specified file will be opened in write mode with the {@link StandardOpenOption#CREATE}
	 * option to ensure that the file will be created if it does not already exist.</p>
	 *
	 * @param file the path to the file to lock
	 * @return a new {@link FileLock} instance
	 * @throws IOException if an I/O error occurs while opening the file
	 * @throws NullPointerException if the {@code file} is {@code null}
	 */
	public static FileLock of(Path file) throws IOException {
		Objects.requireNonNull(file, "File must not be null");
		return new FileLock(FileChannel.open(file, StandardOpenOption.WRITE, StandardOpenOption.CREATE));
	}

	/**
	 * Attempts to acquire an exclusive lock on the file.
	 *
	 * <p>This method tries to acquire a lock on the file within the given timeout period.
	 * If the lock is successfully acquired during this time, the method returns {@code true}. Otherwise, it returns
	 * {@code false} after the timeout period has elapsed.</p>
	 *
	 * @param timeout the maximum amount of time to wait for the lock
	 * @param timeUnit the unit of time for the {@code timeout} parameter
	 * @return {@code true} if the lock was successfully acquired, otherwise {@code false}
	 * @throws IllegalArgumentException if the {@code timeout} is negative
	 * @throws FileLockInterruptionException if the thread is interrupted while waiting for the lock
	 * @throws IOException if an I/O error occurs while trying to acquire the lock
	 * @throws NullPointerException if {@code timeUnit} is {@code null}
	 */
	public synchronized boolean tryLock(long timeout, TimeUnit timeUnit)
			throws FileLockInterruptionException, IOException {
		Objects.requireNonNull(timeUnit, "TimeUnit must not be null");
		if (timeout < 0) {
			throw new IllegalArgumentException("Timeout must not be negative");
		}
		java.nio.channels.FileLock fileLock = this.locks.get(Thread.currentThread());
		if (fileLock != null && fileLock.isValid()) {
			return true;
		}
		long startTime = System.nanoTime();
		long rem = timeUnit.toNanos(timeout);
		do {
			fileLock = lock(this.fileChannel);
			if (fileLock != null) {
				this.locks.put(Thread.currentThread(), fileLock);
				return true;
			}
			if (rem > 0) {
				try {
					Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100));
				}
				catch (InterruptedException ex) {
					throw new FileLockInterruptionException();
				}
			}
			rem = timeUnit.toNanos(timeout) - (System.nanoTime() - startTime);
		} while (rem > 0);
		return false;
	}

	/**
	 * Releases all locks and closes the underlying {@link FileChannel}.
	 *
	 * <p>After this method is called, the file associated with this {@link FileLock} will
	 * no longer be locked, and its {@link FileChannel} will be closed.</p>
	 *
	 * @throws IOException if an I/O error occurs while closing the {@link FileChannel}
	 */
	@Override
	public synchronized void close() throws IOException {
		this.fileChannel.close();
		this.locks.clear();
	}

	private static java.nio.channels.FileLock lock(FileChannel fileChannel) throws IOException {
		try {
			return fileChannel.tryLock();
		}
		catch (OverlappingFileLockException ex) {
			return null; // Another thread or process already owns the lock
		}
	}

}
