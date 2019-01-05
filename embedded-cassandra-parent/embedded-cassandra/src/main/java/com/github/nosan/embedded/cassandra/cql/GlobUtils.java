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

package com.github.nosan.embedded.cassandra.cql;

import java.io.IOException;
import java.io.UncheckedIOException;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apiguardian.api.API;

import com.github.nosan.embedded.cassandra.util.OS;

/**
 * Utility methods for dealing with a {@code glob}.
 *
 * @author Dmytro Nosan
 * @since 1.2.10
 */
@API(since = "1.2.10", status = API.Status.INTERNAL)
abstract class GlobUtils {

	private static final String WINDOWS = "\\\\";

	/**
	 * Walks a file tree.
	 *
	 * @param uri the {@link URI} to lookup resources
	 * @param classLoader the class loader to locate a {@link FileSystem}.
	 * @param glob the glob pattern (without <b>glob:</b>)
	 * @return the paths
	 */
	@Nonnull
	static List<URI> walkFileTree(@Nonnull URI uri, @Nullable ClassLoader classLoader, @Nonnull String glob) {
		try {
			return walkFileTree(uri, toGlobSyntax(glob), classLoader);
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private static List<URI> walkFileTree(URI uri, String globSyntax, ClassLoader classLoader) throws IOException {
		if ("file".equals(uri.getScheme()) && isJarOrZip(uri)) {
			try (FileSystem fileSystem = FileSystems
					.newFileSystem(URI.create(String.format("jar:%s", uri)), Collections.emptyMap(), classLoader)) {
				PathMatcher pathMatcher = fileSystem.getPathMatcher(globSyntax);
				return walkFileTree(fileSystem.getPath("/"), pathMatcher);
			}
		}
		if ("jar".equals(uri.getScheme())) {
			String[] tokens = uri.toString().split("!");
			if (tokens.length == 2) {
				String jarUri = tokens[0];
				String jarPath = tokens[1];
				try (FileSystem fileSystem = FileSystems
						.newFileSystem(URI.create(jarUri), Collections.emptyMap(), classLoader)) {
					PathMatcher pathMatcher = fileSystem.getPathMatcher(globSyntax);
					return walkFileTree(fileSystem.getPath(jarPath), pathMatcher);
				}
			}
		}
		Path path = Paths.get(uri);
		PathMatcher pathMatcher = path.getFileSystem().getPathMatcher(globSyntax);
		return walkFileTree(path, pathMatcher);
	}

	private static List<URI> walkFileTree(Path directory, PathMatcher pathMatcher) throws IOException {
		List<URI> uris = new ArrayList<>();
		Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				if (!isClassFile(file) && Files.exists(file)) {
					int beginIndex = Math.max(Math.min(directory.getNameCount(), file.getNameCount() - 1), 0);
					int endIndex = file.getNameCount();
					if (pathMatcher.matches(file.subpath(beginIndex, endIndex))) {
						uris.add(file.toUri());
					}
				}
				return FileVisitResult.CONTINUE;
			}
		});
		return uris;
	}

	private static boolean isJarOrZip(URI uri) {
		return uri.toString().endsWith(".jar") || uri.toString().endsWith(".zip");
	}

	private static boolean isClassFile(Path file) {
		return String.valueOf(file.getFileName()).endsWith(".class");
	}

	private static String toGlobSyntax(String glob) {
		return String.format("glob:%s", (OS.get() == OS.WINDOWS) ?
				glob.replaceAll("/", WINDOWS + WINDOWS) : glob);
	}

}
