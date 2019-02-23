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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.function.Predicate;

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

	/**
	 * Return the temporary directory.
	 *
	 * @return a directory (java.io.tmpdir)
	 */
	@Nonnull
	public static Path getTmpDirectory() {
		return Paths.get(new SystemProperty("java.io.tmpdir").getRequired());
	}

	/**
	 * Return the user home directory.
	 *
	 * @return a directory (user.home)
	 */
	@Nonnull
	public static Path getUserHomeDirectory() {
		return Paths.get(new SystemProperty("user.home").getRequired());
	}

	/**
	 * Return the user directory.
	 *
	 * @return a directory (user.dir)
	 */
	@Nonnull
	public static Path getUserDirectory() {
		return Paths.get(new SystemProperty("user.dir").getRequired());
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
	 * Recursively copy the contents of the {@code src} file/directory
	 * to the {@code dest} file/directory.
	 *
	 * @param src the source directory
	 * @param dest the destination directory
	 * @param fileFilter the filter to check whether {@code src file} should be copied or not
	 * @throws IOException in the case of I/O errors
	 * @since 1.3.0
	 */
	public static void copy(@Nonnull Path src, @Nonnull Path dest, @Nullable Predicate<? super Path> fileFilter)
			throws IOException {
		Objects.requireNonNull(src, "Source must not be null");
		Objects.requireNonNull(dest, "Destination must not be null");
		Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(@Nonnull Path dir, @Nonnull BasicFileAttributes attributes)
					throws IOException {
				Files.createDirectories(dest.resolve(src.relativize(dir)));
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(@Nonnull Path file, @Nonnull BasicFileAttributes attributes)
					throws IOException {
				if (fileFilter == null || fileFilter.test(file)) {
					Files.copy(file, dest.resolve(src.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}
}
