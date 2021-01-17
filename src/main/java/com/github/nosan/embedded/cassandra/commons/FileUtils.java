/*
 * Copyright 2020-2021 the original author or authors.
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
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Simple utility methods for dealing with files and directories.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public final class FileUtils {

	private static final char[] HEX_CODE = "0123456789abcdef".toCharArray();

	private static final int BUFFER_SIZE = 8192;

	private FileUtils() {
	}

	/**
	 * Deletes the supplied {@link Path}. For directories, recursively delete any nested directories or files as well.
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
	 * Copies a path to a target path. For directories, it will be copied recursively.
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
	 * Copies a path to a target path. For directories, it will be copied recursively.
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

	/**
	 * Computes the checksum of the file using the provided algorithm.
	 *
	 * @param file the file to read
	 * @param algorithm the name of the algorithm.
	 * @return the computed checksum in lowercase hex format
	 * @throws NoSuchAlgorithmException if no Provider supports a MessageDigestSpi implementation for the specified
	 * algorithm.
	 * @throws IOException in the case of I/O errors
	 */
	public static String checksum(Path file, String algorithm) throws NoSuchAlgorithmException, IOException {
		Objects.requireNonNull(file, "File must not be null");
		Objects.requireNonNull(file, "Algorithm must not be null");
		MessageDigest md = MessageDigest.getInstance(algorithm);
		byte[] buffer = new byte[BUFFER_SIZE];
		try (InputStream is = Files.newInputStream(file)) {
			int read;
			while ((read = is.read(buffer)) != -1) {
				md.update(buffer, 0, read);
			}
		}
		byte[] data = md.digest();
		StringBuilder hex = new StringBuilder(data.length * 2);
		for (byte b : data) {
			hex.append(HEX_CODE[(b >> 4) & 0xF]);
			hex.append(HEX_CODE[(b & 0xF)]);
		}
		return hex.toString();
	}

}
