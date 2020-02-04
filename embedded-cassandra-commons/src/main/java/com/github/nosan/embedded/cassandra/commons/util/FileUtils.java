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

package com.github.nosan.embedded.cassandra.commons.util;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Objects;
import java.util.function.BiPredicate;

import com.github.nosan.embedded.cassandra.annotations.Nullable;

/**
 * Simple utility methods for dealing with a {@link Path}.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public final class FileUtils {

	private FileUtils() {
	}

	/**
	 * Creates a new and empty file, if the file does not exist.
	 *
	 * @param file the path to the file to create
	 * @param attributes an optional list of file attributes to set atomically when creating the file
	 * @return the file
	 * @throws IOException if an I/O error occurs or the parent directory does not exist
	 * @see Files#createFile(Path, FileAttribute[])
	 */
	public static Path createIfNotExists(Path file, FileAttribute<?>... attributes) throws IOException {
		Objects.requireNonNull(file, "'file' must not be null");
		Objects.requireNonNull(attributes, "'attributes' must not be null");
		try {
			return Files.createFile(file, attributes);
		}
		catch (FileAlreadyExistsException ex) {
			return file;
		}
	}

	/**
	 * Delete the supplied {@link Path}. For directories, recursively delete any nested directories or files as well.
	 *
	 * @param path the {@code path} to delete
	 * @return {@code true} if the {@code path} existed and was deleted, or {@code false} it did not exist
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
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.deleteIfExists(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, @Nullable IOException ex) throws IOException {
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
	 * Recursively copy the contents of the {@code src} file/directory to the {@code dest} file/directory.
	 *
	 * @param src the source path
	 * @param dest the destination path
	 * @param matcher the function used to decide whether a path should be copied or not
	 * @throws IOException in the case of I/O errors
	 */
	public static void copy(Path src, Path dest,
			@Nullable BiPredicate<? super Path, ? super BasicFileAttributes> matcher) throws IOException {
		Objects.requireNonNull(src, "'src' must not be null");
		Objects.requireNonNull(dest, "'dest' must not be null");
		Files.walkFileTree(src, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attrs) throws IOException {
				if (matcher == null || matcher.test(directory, attrs)) {
					Files.createDirectories(dest.resolve(src.relativize(directory)));
					return FileVisitResult.CONTINUE;
				}
				return FileVisitResult.SKIP_SUBTREE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (matcher == null || matcher.test(file, attrs)) {
					Files.copy(file, dest.resolve(src.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
				}
				return FileVisitResult.CONTINUE;
			}
		});

	}

}
