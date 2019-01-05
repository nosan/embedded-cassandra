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
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

	private static final String WINDOWS = "\\\\";

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

	/**
	 * Walks a file tree with a {@code glob} pattern filter. Resources will be sorted by
	 * {@link URI#compareTo(URI)}.
	 * <b>Note!</b> Prefix {@code glob:} will be added automatically.
	 *
	 * @param uri the {@link URI} to start with. (must be <b>file:,jar:,war:</b>)
	 * @param glob the glob pattern (e.g. <b>**</b>)
	 * @return the sorted resources
	 * @throws IOException if an I/O error occurs
	 * @see FileSystem#getPathMatcher(String)
	 * @since 1.2.10
	 */
	@Nonnull
	public static List<URI> walkGlobFileTree(@Nonnull URI uri, @Nonnull String glob) throws IOException {
		Objects.requireNonNull(uri, "URI must not be null");
		Objects.requireNonNull(glob, "Glob must not be null");
		return walkGlobFileTree(uri, glob, ClassUtils.getClassLoader())
				.stream()
				.sorted(URI::compareTo)
				.collect(Collectors.toList());

	}

	private static Set<URI> walkGlobFileTree(URI uri, String glob, ClassLoader cl) throws IOException {
		Map<String, Object> env = Collections.emptyMap();
		if ("file".equals(uri.getScheme()) && isJar(uri)) {
			URI jarUri = URI.create(String.format("jar:%s", uri));
			try (FileSystem fileSystem = FileSystems.newFileSystem(jarUri, env, cl)) {
				return walkGlobFileTree(fileSystem.getPath("/"), glob);
			}
		}
		if ("jar".equals(uri.getScheme())) {
			String[] tokens = uri.toString().split("!");
			if (tokens.length == 2) {
				String jarUri = tokens[0];
				String jarEntry = tokens[1];
				try (FileSystem fileSystem = FileSystems.newFileSystem(URI.create(jarUri), env, cl)) {
					return walkGlobFileTree(fileSystem.getPath(jarEntry), glob);
				}
			}
		}
		return walkGlobFileTree(Paths.get(uri), glob);
	}

	private static Set<URI> walkGlobFileTree(Path path, String glob) throws IOException {
		if (!Files.exists(path) || !Files.isReadable(path)) {
			return Collections.emptySet();
		}
		PathMatcher pathMatcher = toPathMatcher(path, glob);
		Set<URI> uris = new LinkedHashSet<>();
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				FileSystem fileSystem = file.getFileSystem();
				Path normalizedPath = fileSystem.getPath(normalizePath(fileSystem,
						String.valueOf(file.toAbsolutePath())));
				if (pathMatcher.matches(normalizedPath.toAbsolutePath()) && Files.isReadable(file)) {
					uris.add(file.toUri());
				}
				return FileVisitResult.CONTINUE;
			}
		});
		return uris;
	}

	private static boolean isJar(URI uri) {
		return uri.toString().endsWith(".jar");
	}

	private static PathMatcher toPathMatcher(Path path, String glob) {
		FileSystem fileSystem = path.getFileSystem();
		String globSyntax = glob;
		if (globSyntax.startsWith("glob:")) {
			globSyntax = globSyntax.substring(5);
		}
		globSyntax = path.toAbsolutePath() + "/" + globSyntax;
		return fileSystem.getPathMatcher(String.format("glob:%s", normalizePath(fileSystem, globSyntax)));
	}

	private static String normalizePath(FileSystem fileSystem, String path) {
		String newPath = path.replaceAll(WINDOWS, "/").replaceAll("/+", "/").trim();
		if ("\\".equals(fileSystem.getSeparator())) {
			return newPath.replaceAll("/", WINDOWS + WINDOWS);
		}
		return newPath.replaceAll("/", fileSystem.getSeparator());
	}

}
