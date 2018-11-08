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

package com.github.nosan.embedded.cassandra.test.spring;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.test.context.util.TestContextResourceUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.cql.StaticCqlScript;
import com.github.nosan.embedded.cassandra.cql.UrlCqlScript;

/**
 * Utility class to convert Spring {@link Resource}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
abstract class CqlResourceUtils {

	/**
	 * Convert Spring {@link Resource} as {@link CqlScript}.
	 *
	 * @param resolver {@link ResourcePatternResolver} for Spring resources.
	 * @param config CQL config.
	 * @return CQL scripts.
	 * @throws IOException if an I/O error occurs
	 */
	@Nonnull
	static CqlScript[] getScripts(@Nonnull ResourcePatternResolver resolver, @Nonnull CqlConfig config)
			throws IOException {
		Objects.requireNonNull(resolver, "Resource Pattern Resolver must not be null");
		Objects.requireNonNull(config, "CqlConfig must not be null");
		List<CqlScript> cqlScripts = new ArrayList<>();

		String[] scripts = config.getScripts();
		String encoding = config.getEncoding();
		String[] statements = config.getStatements();
		Class<?> testClass = config.getTestClass();

		if (!ObjectUtils.isEmpty(scripts)) {
			Charset charset = (!StringUtils.hasText(encoding) ? null : Charset.forName(encoding));
			List<Resource> resources = new ArrayList<>();
			for (String script : TestContextResourceUtils.convertToClasspathResourcePaths(testClass, scripts)) {
				resources.addAll(Arrays.asList(resolver.getResources(script)));
			}
			for (Resource resource : resources) {
				cqlScripts.add(new UrlCqlScript(resource.getURL(), charset));
			}
		}
		if (!ObjectUtils.isEmpty(statements)) {
			cqlScripts.add(new StaticCqlScript(statements));
		}
		return cqlScripts.toArray(new CqlScript[0]);
	}


	/**
	 * Convert Spring {@link Resource} as {@link URL}.
	 *
	 * @param resourceLoader {@link ResourceLoader} to load resource
	 * @param resource a path to the resource
	 * @param testClass class to load the resource
	 * @return URL
	 * @throws IOException if an I/O error occurs
	 * @since 1.0.7
	 */
	@Nullable
	static URL getURL(@Nonnull ResourceLoader resourceLoader, @Nullable Class<?> testClass, @Nullable String resource)
			throws IOException {
		if (!StringUtils.hasText(resource)) {
			return null;
		}
		Objects.requireNonNull(resourceLoader, "Resource Loader must not be null");
		String location = TestContextResourceUtils.convertToClasspathResourcePaths(testClass, resource)[0];
		return resourceLoader.getResource(location).getURL();
	}

}
