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

package com.github.nosan.embedded.cassandra.util;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLockInterruptionException;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * Utility class to lock a file. <b>Only for internal purposes.</b>
 *
 * @author Dmytro Nosan
 * @see java.nio.channels.FileLock
 * @since 1.4.2
 */
public final class FileLock implements AutoCloseable {

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
	public FileLock(Path file) {
		this.file = Objects.requireNonNull(file, "File must not be null");
	}

	/**
	 * Acquires an exclusive lock on the file.
	 *
	 * @throws java.nio.channels.FileLockInterruptionException If the invoking thread is interrupted while blocked in
	 * this method
	 * @throws java.io.IOException If some other I/O error occurs
	 */
	public void lock() throws IOException, FileLockInterruptionException {
		Path file = this.file;
		FileChannel fileChannel = FileChannel.open(file, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
		this.fileChannel = fileChannel;
		java.nio.channels.FileLock fileLock;
		if (log.isDebugEnabled()) {
			log.debug("Acquires a lock to the file '{}' ...", file);
		}
		while ((fileLock = tryLock(fileChannel)) == null) {
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException ex) {
				throw new FileLockInterruptionException();
			}
		}
		this.fileLock = fileLock;
		if (log.isDebugEnabled()) {
			log.debug("The lock to the file '{}' is acquired", file);
		}
	}

	/**
	 * Releases this lock.
	 */
	public void release() {
		close(() -> Files.deleteIfExists(this.file));
		close(this.fileLock);
		close(this.fileChannel);
	}

	/**
	 * Releases this lock.
	 */
	@Override
	public void close() {
		release();
	}

	@Nullable
	private static java.nio.channels.FileLock tryLock(FileChannel fileChannel) throws IOException {
		try {
			return fileChannel.lock();
		}
		catch (OverlappingFileLockException ex) {
			return null;
		}
	}

	private static void close(@Nullable AutoCloseable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			}
			catch (Exception ex) {
				log.error(ex.getMessage(), ex);
			}
		}
	}

}
