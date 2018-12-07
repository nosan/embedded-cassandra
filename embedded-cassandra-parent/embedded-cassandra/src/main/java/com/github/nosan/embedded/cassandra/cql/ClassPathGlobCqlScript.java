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

package com.github.nosan.embedded.cassandra.cql;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.nosan.embedded.cassandra.util.ClassUtils;
import com.github.nosan.embedded.cassandra.util.OS;

/**
 * {@link CqlScript} implementation for class path {@code glob matching} resources.
 * <p>
 * {@code glob} examples:
 * <blockquote>
 * <table border="0" summary="Pattern Language">
 * <tr>
 * <td>{@code *.cql}</td>
 * <td>Matches a path that represents a file name ending in {@code .cql}</td>
 * </tr>
 * <tr>
 * <td>{@code **.cql}</td>
 * <td>Matches all path that represents a file name ending in {@code .cql}</td>
 * </tr>
 * <tr>
 * <td>{@code rol?s.cql}</td>
 * <td>Matches file names starting with {@code rol<any>s.cql}</td>
 * </tr>
 * <tr>
 * <td><tt>&#47;home&#47;*&#47;*&#47;roles.cql</tt>
 * <td>Matches <tt>&#47;home&#47;any&#47;any&#47;roles.cql</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&#47;home&#47;**&#47;roles.cql</tt>
 * <td>Matches <tt>&#47;home&#47;any&#47;any&#47;...&#47roles.cql</tt></td>
 * </tr>
 * </table>
 * </blockquote>
 *
 * @author Dmytro Nosan
 * @see CqlScript#classpathGlobs(String...)
 * @since 1.2.6
 */
public class ClassPathGlobCqlScript implements CqlScript {


	private static final String WINDOWS = "\\\\";

	@Nonnull
	private final String glob;

	@Nullable
	private final Charset encoding;

	@Nullable
	private final ClassLoader classLoader;

	/**
	 * Create a new {@link ClassPathGlobCqlScript}.
	 *
	 * @param glob the glob pattern within the class path
	 */
	public ClassPathGlobCqlScript(@Nonnull String glob) {
		this(glob, null, null);
	}

	/**
	 * Create a new {@link ClassPathGlobCqlScript}.
	 *
	 * @param glob the glob pattern within the class path
	 * @param encoding the encoding to use for reading from the resource
	 */
	public ClassPathGlobCqlScript(@Nonnull String glob, @Nullable Charset encoding) {
		this(glob, null, encoding);
	}

	/**
	 * Create a new {@link ClassPathGlobCqlScript}.
	 *
	 * @param glob the glob pattern within the class path
	 * @param classLoader the class loader to load the resource with.
	 */
	public ClassPathGlobCqlScript(@Nonnull String glob, @Nullable ClassLoader classLoader) {
		this(glob, classLoader, null);
	}

	/**
	 * Create a new {@link ClassPathGlobCqlScript}.
	 *
	 * @param glob the glob pattern within the class path
	 * @param classLoader the class loader to load the resource with.
	 * @param encoding the encoding to use for reading from the resource
	 */
	public ClassPathGlobCqlScript(@Nonnull String glob, @Nullable ClassLoader classLoader,
			@Nullable Charset encoding) {
		Objects.requireNonNull(glob, "glob must not be null");
		this.encoding = encoding;
		this.glob = toGlob(glob);
		this.classLoader = (classLoader != null) ? classLoader : ClassUtils.getClassLoader();
	}


	/**
	 * {@inheritDoc}
	 *
	 * @throws UncheckedIOException if an I/O error occurs
	 */
	@Nonnull
	@Override
	public Collection<String> getStatements() {
		URL url = getUrl();
		if (url == null) {
			return Collections.emptyList();
		}
		Path directory = getDirectory(url);
		try {
			List<Path> candidates = new ArrayList<>();
			PathMatcher pathMatcher = getPathMatcher(this.glob);
			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					if (match(file, directory, pathMatcher)) {
						candidates.add(file);
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException ex) {
					return FileVisitResult.CONTINUE;
				}
			});
			List<PathCqlScript> scripts = candidates.stream()
					.sorted((p1, p2) -> {
						try {
							return p1.toUri().toURL().toString().compareTo(p2.toUri().toURL().toString());
						}
						catch (Exception ex) {
							return 0;
						}
					})
					.map(path -> new PathCqlScript(path, this.encoding))
					.collect(Collectors.toList());
			return new CqlScripts(scripts).getStatements();
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}


	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		ClassPathGlobCqlScript that = (ClassPathGlobCqlScript) other;

		URL url = getUrl();
		if (url != null) {
			return Objects.equals(this.glob, that.glob) &&
					Objects.equals(this.encoding, that.encoding) &&
					Objects.equals(url, that.getUrl());
		}
		return Objects.equals(this.glob, that.glob) &&
				Objects.equals(this.encoding, that.encoding) &&
				Objects.equals(this.classLoader, that.classLoader);
	}

	@Override
	public int hashCode() {
		URL url = getUrl();
		if (url != null) {
			return Objects.hash(url, this.glob, this.encoding);
		}
		return Objects.hash(this.glob, this.encoding, this.classLoader);
	}

	@Override
	@Nonnull
	public String toString() {
		return this.glob;
	}

	private String toGlob(String glob) {
		return glob.replaceAll(WINDOWS, "/").replaceAll("/+", "/");
	}

	private boolean match(Path file, Path directory, PathMatcher pathMatcher) {
		Path normalize = file.subpath(directory.getNameCount(), file.getNameCount());
		return pathMatcher.matches(normalize);
	}

	private PathMatcher getPathMatcher(String glob) {
		String g = String.format("glob:%s", OS.isWindows() ? glob.replaceAll("/", WINDOWS + WINDOWS) : glob);
		return FileSystems.getDefault().getPathMatcher(g);
	}

	private URL getUrl() {
		return (this.classLoader != null) ? this.classLoader.getResource("") : ClassLoader.getSystemResource("");
	}

	private Path getDirectory(URL url) {
		try {
			return Paths.get(url.toURI());
		}
		catch (URISyntaxException ex) {
			throw new IllegalStateException(ex);
		}
	}


}
