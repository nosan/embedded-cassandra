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

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.commons.util.FileUtils;

/**
 * This class is used to create a {@link java.nio.channels.FileLock}. It is recommended practice to lock file with a try
 * with resources block, such as:
 * <pre> {@code
 * class X {
 *   public void m() {
 *     try(FileLock lock = FileLock.of(lockFile)){
 *         if(lock.tryLock(1, TimeUnit.MINUTES){
 *             // locked
 *         } else {
 *             // not locked
 *         }
 *     }
 * }}}
 * </pre>
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public final class FileLock implements AutoCloseable {

	private final FileChannel fileChannel;

	private final Map<Thread, java.nio.channels.FileLock> locks = new ConcurrentHashMap<>();

	private FileLock(FileChannel fileChannel) {
		this.fileChannel = fileChannel;
	}

	/**
	 * Creates a new {@link FileLock} instance for the specified file. Note! This method creates a {@link FileChannel}
	 * for the specified file and it must be closed in the end.
	 *
	 * @param file the file that should be locked
	 * @return a new {@link FileLock}
	 * @throws IOException in the case of I/O errors
	 */
	public static FileLock of(Path file) throws IOException {
		Objects.requireNonNull(file, "'file' must not be null");
		return new FileLock(FileChannel.open(FileUtils.createIfNotExists(file), StandardOpenOption.WRITE));
	}

	/**
	 * Returns the file channel upon whose file this lock was created.
	 *
	 * @return The file channel
	 */
	public FileChannel getFileChannel() {
		return this.fileChannel;
	}

	/**
	 * Acquires an exclusive lock on the file.
	 *
	 * @param timeout the maximum time to wait
	 * @param timeUnit the time unit of the {@code timeout} argument
	 * @return {@code true} if lock has been acquired otherwise {@code false}
	 * @throws FileLockInterruptionException If the invoking thread is interrupted while blocked in this method
	 * @throws IOException If some other I/O error occurs
	 */
	public boolean tryLock(long timeout, TimeUnit timeUnit) throws FileLockInterruptionException, IOException {
		Objects.requireNonNull(timeUnit, "'timeUnit' must not be null");
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
	 * Releases the lock to the file if it was acquired by the current thread.
	 *
	 * @throws IOException If an I/O error occurs
	 */
	public void release() throws IOException {
		java.nio.channels.FileLock fileLock = this.locks.remove(Thread.currentThread());
		if (fileLock != null) {
			fileLock.close();
		}
	}

	/**
	 * Closes the underlying {@link FileChannel} and releases all locks.
	 *
	 * @throws IOException If some other I/O error occurs
	 */
	@Override
	public void close() throws IOException {
		//closes all locks and channel
		this.fileChannel.close();
		this.locks.clear();
	}

	@Nullable
	private static java.nio.channels.FileLock lock(FileChannel fileChannel) throws IOException {
		try {
			return fileChannel.tryLock();
		}
		catch (OverlappingFileLockException ex) {
			return null;
		}
	}

}
