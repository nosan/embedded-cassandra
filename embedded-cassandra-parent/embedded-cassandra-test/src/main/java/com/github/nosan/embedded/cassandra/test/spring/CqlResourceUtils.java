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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.test.context.util.TestContextResourceUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.github.nosan.embedded.cassandra.cql.AbstractCqlResourceScript;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.cql.StaticCqlScript;
import com.github.nosan.embedded.cassandra.lang.Nullable;

/**
 * Utility class to convert Spring {@link Resource}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
abstract class CqlResourceUtils {

	private static final Logger log = LoggerFactory.getLogger(CqlResourceUtils.class);

	/**
	 * Resolve the given {@link CqlConfig} into {@link CqlScript} objects.
	 *
	 * @param resourcePatternResolver {@link ResourcePatternResolver} for Spring resources.
	 * @param config CQL config.
	 * @return CQL scripts.
	 * @throws UncheckedIOException if an I/O error occurs
	 */
	static CqlScript[] getScripts(ResourcePatternResolver resourcePatternResolver, CqlConfig config) {
		List<CqlScript> cqlScripts = new ArrayList<>();

		String[] scripts = config.getScripts();
		String encoding = config.getEncoding();
		String[] statements = config.getStatements();
		Class<?> testClass = config.getTestClass();

		if (!ObjectUtils.isEmpty(scripts)) {
			Charset charset = StringUtils.hasText(encoding) ? Charset.forName(encoding) : null;
			for (String script : TestContextResourceUtils.convertToClasspathResourcePaths(testClass, scripts)) {
				for (Resource resource : getResources(script, resourcePatternResolver)) {
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
	 * Resolve the given location into URL object.
	 *
	 * @param resourceLoader {@link ResourceLoader} to load resource
	 * @param location a path to the resource
	 * @param testClass class to load the resource
	 * @return the URL to the resource, or {@code null}
	 * @throws UncheckedIOException if an I/O error occurs
	 * @since 1.0.7
	 */
	@Nullable
	static URL getURL(ResourceLoader resourceLoader, String location, @Nullable Class<?> testClass) {
		String[] locations = TestContextResourceUtils.convertToClasspathResourcePaths(testClass, location);
		Resource resource = resourceLoader.getResource(locations[0]);
		if (resource.exists()) {
			return toURL(resource);
		}
		log.warn("{} does not exist. Please check your configuration.", resource);
		return null;
	}

	private static URL toURL(Resource resource) {
		try {
			return resource.getURL();
		}
		catch (IOException ex) {
			throw new UncheckedIOException(String.format("Could not get URL for '%s'", resource), ex);
		}
	}

	private static List<Resource> getResources(String script, ResourcePatternResolver resourcePatternResolver) {
		try {
			List<Resource> resources = new ArrayList<>();
			for (Resource resource : resourcePatternResolver.getResources(script)) {
				if (resource.exists()) {
					resources.add(resource);
				}
				else {
					log.warn("{} does not exist. Please check your configuration.", resource);
				}
			}
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

		private final Resource resource;

		/**
		 * Creates a {@link SpringCqlScript}.
		 *
		 * @param resource the spring resource
		 * @param encoding the encoding to use for reading from the resource
		 */
		SpringCqlScript(Resource resource, @Nullable Charset encoding) {
			super(encoding);
			this.resource = resource;
		}

		@Override
		protected InputStream getInputStream() throws IOException {
			return this.resource.getInputStream();
		}

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
