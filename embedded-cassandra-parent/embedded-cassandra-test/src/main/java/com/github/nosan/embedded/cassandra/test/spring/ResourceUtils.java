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

package com.github.nosan.embedded.cassandra.test.spring;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.test.context.util.TestContextResourceUtils;

/**
 * Utility class for dealing with a {@link Resource}.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
abstract class ResourceUtils {

	/**
	 * Resolve the given locations into {@link URL} objects.
	 *
	 * @param resourcePatternResolver pattern resolver
	 * @param locations the paths to be converted
	 * @param testClass the class with which the paths are associated
	 * @return a new array of converted resource paths
	 * @throws UncheckedIOException if an I/O error occurs
	 */
	static URL[] getResources(ResourcePatternResolver resourcePatternResolver, Class<?> testClass,
			String... locations) {
		List<URL> urls = new ArrayList<>();
		for (String location : TestContextResourceUtils.convertToClasspathResourcePaths(testClass, locations)) {
			for (Resource resource : getResources(resourcePatternResolver, location)) {
				urls.add(toURL(resource));
			}
		}
		return urls.toArray(new URL[0]);
	}

	/**
	 * Resolve the given location into {@link URL} object.
	 *
	 * @param resourceLoader {@link ResourceLoader} to load resource
	 * @param location the path to be converted
	 * @param testClass the class with which the path is associated
	 * @return the URL to the resource
	 * @throws UncheckedIOException if an I/O error occurs
	 */
	static URL getResource(ResourceLoader resourceLoader, Class<?> testClass, String location) {
		String[] locations = TestContextResourceUtils.convertToClasspathResourcePaths(testClass, location);
		Resource resource = resourceLoader.getResource(locations[0]);
		return toURL(resource);
	}

	private static List<Resource> getResources(ResourcePatternResolver resourcePatternResolver, String location) {
		try {
			List<Resource> resources = new ArrayList<>(Arrays.asList(resourcePatternResolver.getResources(location)));
			resources.sort(Comparator.comparing(r -> toURL(r).toString()));
			return resources;
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private static URL toURL(Resource resource) {
		try {
			return resource.getURL();
		}
		catch (IOException ex) {
			throw new UncheckedIOException(String.format("Can not get URL from a resource '%s'", resource), ex);
		}
	}

}
