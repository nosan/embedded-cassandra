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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apiguardian.api.API;

/**
 * Utility methods for dealing with files.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
@API(since = "1.0.0", status = API.Status.INTERNAL)
public abstract class FileUtils {

	private static final Path TMP_DIR = Paths.get(new SystemProperty("java.io.tmpdir").get());

	private static final Path USER_HOME_DIR = Paths.get(new SystemProperty("user.home").get());

	private static final Path USER_DIR = Paths.get(new SystemProperty("user.dir").get());

	/**
	 * Return the temporary directory.
	 *
	 * @return a directory (java.io.tmpdir)
	 */
	@Nonnull
	public static Path getTmpDirectory() {
		return TMP_DIR;
	}

	/**
	 * Return the user home directory.
	 *
	 * @return a directory (user.home)
	 */
	@Nonnull
	public static Path getUserHomeDirectory() {
		return USER_HOME_DIR;
	}


	/**
	 * Return the user directory.
	 *
	 * @return a directory (user.dir)
	 */
	@Nonnull
	public static Path getUserDirectory() {
		return USER_DIR;
	}

	/**
	 * Delete the supplied {@link Path}. For directories,
	 * recursively delete any nested directories or files as well.
	 *
	 * @param path the {@code Path} to delete
	 * @return {@code true} if the {@code Path} existed and was deleted,
	 * or {@code false} it it did not exist
	 * @throws IOException in the case of I/O errors
	 */
	public static boolean delete(@Nullable Path path) throws IOException {
		if (path == null) {
			return false;
		}
		if (!Files.exists(path)) {
			return false;
		}
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(@Nonnull Path file, @Nonnull BasicFileAttributes attrs)
					throws IOException {
				Files.deleteIfExists(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(@Nonnull Path dir, @Nullable IOException ex) throws IOException {
				if (ex != null) {
					throw ex;
				}
				Files.deleteIfExists(dir);
				return FileVisitResult.CONTINUE;
			}
		});
		return true;
	}


	/**
	 * Tests whether a {@code Path} is a temporary.
	 *
	 * @param path {@code Path} to test
	 * @return {@code true} if the {@code Path} is a temporary
	 */
	public static boolean isTemporary(@Nullable Path path) {
		if (path == null) {
			return false;
		}
		Path tmpDir = getTmpDirectory();
		while (path != null && path.getNameCount() > tmpDir.getNameCount()) {
			path = path.getParent();
		}
		return tmpDir.equals(path);
	}

}
