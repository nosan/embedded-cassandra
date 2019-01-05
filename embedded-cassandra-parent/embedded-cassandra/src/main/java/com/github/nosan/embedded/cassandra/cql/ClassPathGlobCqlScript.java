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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apiguardian.api.API;

import com.github.nosan.embedded.cassandra.util.ClassUtils;

/**
 * Glob {@link CqlScript} implementation for {@link ClassLoader#getResources(String)}.
 * <p>
 * All resources will be interpreted as a {@link URI} and <b>sorted</b> by {@code uri.toURL().toString()}.
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
 * <td>{@code {roles,keyspace}.cql}</td>
 * <td>Matches file names starting with {@code roles.cql or keyspace.cql}</td>
 * </tr>
 * <tr>
 * <td><tt>home&#47;*&#47;*&#47;roles.cql</tt>
 * <td>Matches <tt>home&#47;any&#47;any&#47;roles.cql</tt></td>
 * </tr>
 * <tr>
 * <td><tt>home&#47;**&#47;roles.cql</tt>
 * <td>Matches <tt>home&#47;...&#47;roles.cql</tt></td>
 * </tr>
 * </table>
 * </blockquote>
 *
 * @author Dmytro Nosan
 * @see CqlScript#classpathGlobs(String...)
 * @since 1.2.6
 */
@API(since = "1.2.6", status = API.Status.STABLE)
public final class ClassPathGlobCqlScript implements CqlScript {

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
		Objects.requireNonNull(glob, "Glob must not be null");
		this.glob = normalize(glob);
		this.encoding = encoding;
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
		ClassLoader classLoader = this.classLoader;
		String glob = this.glob;
		Charset encoding = this.encoding;
		List<CqlScript> scripts = getURLs(classLoader).stream()
				.map(ClassPathGlobCqlScript::toURI)
				.map(uri -> GlobUtils.walkFileTree(uri, classLoader, glob))
				.flatMap(Collection::stream)
				.map(ClassPathGlobCqlScript::toURL)
				.sorted(Comparator.comparing(URL::toString))
				.map(url -> new UrlCqlScript(url, encoding))
				.collect(Collectors.toList());
		return new CqlScripts(scripts).getStatements();
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
		return Objects.equals(this.glob, that.glob) &&
				Objects.equals(this.encoding, that.encoding) &&
				Objects.equals(this.classLoader, that.classLoader);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.glob, this.encoding, this.classLoader);
	}

	@Override
	@Nonnull
	public String toString() {
		return this.glob;
	}

	private static List<URL> getURLs(ClassLoader classLoader) {
		try {
			Enumeration<URL> enumeration = (classLoader != null) ? classLoader.getResources("") : null;
			if (enumeration == null) {
				return Collections.emptyList();
			}
			return Collections.list(enumeration);
		}
		catch (IOException ex) {
			throw new UncheckedIOException(String.format("Could not get URLs for ClassLoader (%s)", classLoader), ex);
		}
	}

	private static URI toURI(URL url) {
		try {
			return url.toURI();
		}
		catch (URISyntaxException ex) {
			throw new IllegalStateException(String.format("Could not transform (%s) to the URI", url), ex);
		}
	}

	private static URL toURL(URI uri) {
		try {
			return uri.toURL();
		}
		catch (MalformedURLException ex) {
			throw new IllegalStateException(String.format("Could not transform (%s) to the URL", uri), ex);
		}
	}

	private static String normalize(String glob) {
		return glob.replaceAll(WINDOWS, "/").replaceAll("/+", "/");
	}

}
