/*
 * Copyright 2020 the original author or authors.
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
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Simple utility methods for dealing with files and directories.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public final class FileUtils {

	private FileUtils() {
	}

	/**
	 * Delete the supplied {@link Path}. For directories, recursively delete any nested directories or files as well.
	 *
	 * @param path the {@code path} to delete
	 * @return {@code true} if the {@code path} existed and was deleted, or {@code false} it did not exist
	 * @throws IOException in the case of I/O errors
	 */
	public static boolean delete(Path path) throws IOException {
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
			public FileVisitResult postVisitDirectory(Path dir, IOException ex) throws IOException {
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
	 * @param options specifying how the copy should be done the path to the target file
	 * @throws IOException in the case of I/O errors
	 */
	public static void copy(Path src, Path dest, CopyOption... options) throws IOException {
		copy(src, dest, (path, attributes) -> true, options);
	}

	/**
	 * Recursively copy the contents of the {@code src} file/directory to the {@code dest} file/directory.
	 *
	 * @param src the source path
	 * @param dest the destination path
	 * @param options specifying how the copy should be done the path to the target file
	 * @param filter the function used to decide whether a path should be copied or not
	 * @throws IOException in the case of I/O errors
	 */
	public static void copy(Path src, Path dest, BiPredicate<? super Path, ? super BasicFileAttributes> filter,
			CopyOption... options) throws IOException {
		Objects.requireNonNull(src, "Source Path must not be null");
		Objects.requireNonNull(dest, "Destination Path must not be null");
		Objects.requireNonNull(options, "Copy Options must not be null");
		Files.walkFileTree(src, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attrs) throws IOException {
				if (filter == null || filter.test(directory, attrs)) {
					Files.createDirectories(dest.resolve(src.relativize(directory)));
					return FileVisitResult.CONTINUE;
				}
				return FileVisitResult.SKIP_SUBTREE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (filter == null || filter.test(file, attrs)) {
					Files.copy(file, dest.resolve(src.relativize(file)), options);
				}
				return FileVisitResult.CONTINUE;
			}
		});

	}

}
