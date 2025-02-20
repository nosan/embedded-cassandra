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
 * A utility class providing simple methods for dealing with files and directories.
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
	 * Deletes the specified {@link Path}. If the path refers to a directory, it will recursively delete its contents
	 * before deleting the directory itself.
	 *
	 * <p>This method is safe to use even if the specified path does not exist.</p>
	 *
	 * @param path the path to delete; if {@code null}, this method will return {@code false}
	 * @return {@code true} if the path existed and was deleted, or {@code false} if it did not exist
	 * @throws IOException if an I/O error occurs while deleting the path
	 */
	public static boolean delete(Path path) throws IOException {
		if (path == null) {
			return false;
		}
		if (!Files.exists(path)) {
			return false;
		}
		Files.walkFileTree(path, new SimpleFileVisitor<>() {

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
	 * Copies a file or directory to the target location. If the source path refers to a directory, it will be copied
	 * recursively, including its contents.
	 *
	 * @param src the source path
	 * @param dest the target path
	 * @param options the options specifying how the copy should be performed
	 * @throws IOException if an I/O error occurs during the copy
	 * @throws NullPointerException if any of the arguments are {@code null}
	 */
	public static void copy(Path src, Path dest, CopyOption... options) throws IOException {
		copy(src, dest, (path, attributes) -> true, options);
	}

	/**
	 * Copies a file or directory to the target location with a filtering function. If the source path refers to a
	 * directory, it will be copied recursively, but only files and directories that pass the filter will be included.
	 *
	 * @param src the source path
	 * @param dest the destination path
	 * @param filter a predicate used to determine whether a file or directory should be copied
	 * @param options the options specifying how the copy should be performed
	 * @throws IOException if an I/O error occurs during the copy
	 * @throws NullPointerException if the source path, destination path, or options array is {@code null}
	 */
	public static void copy(Path src, Path dest, BiPredicate<? super Path, ? super BasicFileAttributes> filter,
			CopyOption... options) throws IOException {
		Objects.requireNonNull(src, "Source Path must not be null");
		Objects.requireNonNull(dest, "Destination Path must not be null");
		Objects.requireNonNull(options, "Copy Options must not be null");

		Files.walkFileTree(src, new SimpleFileVisitor<>() {

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
	 * Computes the checksum of the specified file using the given algorithm.
	 *
	 * <p>This method reads the file's content and computes the hash using the provided
	 * algorithm (e.g., {@code MD5}, {@code SHA-1}, {@code SHA-256}). The result is returned as a lowercase hexadecimal
	 * string.</p>
	 *
	 * @param file the file for which the checksum is computed
	 * @param algorithm the name of the algorithm to use for the hash computation
	 * @return the computed checksum as a lowercase hexadecimal string
	 * @throws IOException if an I/O error occurs while reading the file
	 * @throws NoSuchAlgorithmException if the specified algorithm is not available
	 * @throws NullPointerException if the file or algorithm is {@code null}
	 */
	public static String checksum(Path file, String algorithm) throws NoSuchAlgorithmException, IOException {
		Objects.requireNonNull(file, "File must not be null");
		Objects.requireNonNull(algorithm, "Algorithm must not be null");
		MessageDigest md = MessageDigest.getInstance(algorithm);
		byte[] buffer = new byte[BUFFER_SIZE];

		try (InputStream is = Files.newInputStream(file)) {
			int read;
			while ((read = is.read(buffer)) != -1) {
				md.update(buffer, 0, read);
			}
		}

		byte[] hash = md.digest();
		StringBuilder hex = new StringBuilder(hash.length * 2);
		for (byte b : hash) {
			hex.append(HEX_CODE[(b >> 4) & 0xF]);
			hex.append(HEX_CODE[b & 0xF]);
		}

		return hex.toString();
	}

}
