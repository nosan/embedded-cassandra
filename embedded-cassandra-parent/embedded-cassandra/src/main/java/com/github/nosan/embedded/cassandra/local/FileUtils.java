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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiPredicate;

import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * Utility methods for dealing with files.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
abstract class FileUtils {

	/**
	 * Delete the supplied {@link Path}. For directories, recursively delete any nested directories or files as well.
	 *
	 * @param path the {@code path} to delete
	 * @return {@code true} if the {@code path} existed and was deleted, or {@code false} it it did not exist
	 * @throws IOException in the case of I/O errors
	 */
	static boolean delete(@Nullable Path path) throws IOException {
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
	 * @param source the source path
	 * @param destination the destination path
	 * @param matcher the function used to decide whether a path should be copied or not
	 * @throws IOException in the case of I/O errors
	 * @since 1.3.0
	 */
	static void copy(Path source, Path destination,
			BiPredicate<? super Path, ? super BasicFileAttributes> matcher) throws IOException {
		Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attrs) throws IOException {
				if (matcher.test(directory, attrs)) {
					Files.createDirectories(destination.resolve(source.relativize(directory)));
					return FileVisitResult.CONTINUE;
				}
				return FileVisitResult.SKIP_SUBTREE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (matcher.test(file, attrs)) {
					Files.copy(file, destination.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

}
