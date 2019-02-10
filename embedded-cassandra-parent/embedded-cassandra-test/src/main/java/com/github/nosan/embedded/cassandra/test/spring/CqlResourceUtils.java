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

package com.github.nosan.embedded.cassandra.test.spring;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.test.context.util.TestContextResourceUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.github.nosan.embedded.cassandra.cql.AbstractCqlResourceScript;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.cql.StaticCqlScript;

/**
 * Utility class to convert Spring {@link Resource}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
abstract class CqlResourceUtils {

	/**
	 * Converts Spring {@link Resource} as {@link CqlScript}.
	 *
	 * @param resolver {@link ResourcePatternResolver} for Spring resources.
	 * @param config CQL config.
	 * @return CQL scripts.
	 * @throws UncheckedIOException if an I/O error occurs
	 */
	@Nonnull
	static CqlScript[] getScripts(@Nonnull ResourcePatternResolver resolver, @Nonnull CqlConfig config) {
		List<CqlScript> cqlScripts = new ArrayList<>();

		String[] scripts = config.getScripts();
		String encoding = config.getEncoding();
		String[] statements = config.getStatements();
		Class<?> testClass = config.getTestClass();

		if (!ObjectUtils.isEmpty(scripts)) {
			Charset charset = StringUtils.hasText(encoding) ? Charset.forName(encoding) : null;
			for (String script : TestContextResourceUtils.convertToClasspathResourcePaths(testClass, scripts)) {
				for (Resource resource : getResources(script, resolver)) {
					cqlScripts.add(new SpringCqlScript(resource, charset));
				}
			}
		}
		if (!ObjectUtils.isEmpty(statements)) {
			cqlScripts.add(new StaticCqlScript(statements));
		}
		return cqlScripts.toArray(new CqlScript[0]);
	}

	/**
	 * Converts Spring {@link Resource} as {@link URL}.
	 *
	 * @param resourceLoader {@link ResourceLoader} to load resource
	 * @param resource a path to the resource
	 * @param testClass class to load the resource
	 * @return URL
	 * @throws UncheckedIOException if an I/O error occurs
	 * @since 1.0.7
	 */
	@Nonnull
	static URL getURL(@Nonnull ResourceLoader resourceLoader, @Nonnull String resource, @Nullable Class<?> testClass) {
		String[] locations = TestContextResourceUtils.convertToClasspathResourcePaths(testClass, resource);
		Assert.isTrue(locations.length == 1, "Invalid location length");
		return toURL(resourceLoader.getResource(locations[0]));
	}

	private static URL toURL(Resource resource) {
		try {
			return resource.getURL();
		}
		catch (IOException ex) {
			throw new UncheckedIOException(String.format("Could not get URL for (%s)", resource), ex);
		}
	}

	private static List<Resource> getResources(String script, ResourcePatternResolver resourcePatternResolver) {
		try {
			List<Resource> resources = new ArrayList<>(Arrays.asList(resourcePatternResolver.getResources(script)));
			resources.removeIf(resource -> !resource.exists());
			resources.sort(Comparator.comparing(r -> toURL(r).toString()));
			return resources;
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	/**
	 * {@link CqlScript} implementation for {@link Resource}.
	 */
	private static final class SpringCqlScript extends AbstractCqlResourceScript {

		@Nonnull
		private final Resource resource;

		/**
		 * Creates a {@link SpringCqlScript}.
		 *
		 * @param resource the spring resource
		 * @param encoding the encoding to use for reading from the resource
		 */
		SpringCqlScript(@Nonnull Resource resource, @Nullable Charset encoding) {
			super(encoding);
			this.resource = Objects.requireNonNull(resource, "Resource must not be null");
		}

		@Nonnull
		@Override
		protected InputStream getInputStream() throws IOException {
			return this.resource.getInputStream();
		}

		@Nonnull
		@Override
		public String toString() {
			return this.resource.toString();
		}

		@Override
		public boolean equals(@Nullable Object other) {
			if (this == other) {
				return true;
			}
			if (other == null || getClass() != other.getClass()) {
				return false;
			}
			SpringCqlScript that = (SpringCqlScript) other;
			return Objects.equals(this.resource, that.resource);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.resource);
		}
	}
}
