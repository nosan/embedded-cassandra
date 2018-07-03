/*
 * Copyright 2012-2018 the original author or authors.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import com.datastax.driver.core.Session;

/**
 * Utility class for executing CQL Scripts using different resources.
 *
 * @author Dmytro Nosan
 */
public abstract class CqlScriptUtils {

	/**
	 * Executing CQL scripts using `class-related` and `system` classpath resources.
	 * @param session Cassandra's session.
	 * @param clazz the class to load resources with.
	 * @param resources Array of `classpath` resources.
	 * @throws IOException If an I/O error occurs or resource is not found.
	 * @see Session#execute(String)
	 */
	public static void executeScripts(Session session, Class<?> clazz,
			String... resources) throws IOException {
		for (String resource : resources) {
			executeScripts(session, getUrl(resource, clazz).openStream());
		}
	}

	/**
	 * Executing CQL scripts using `system` classpath resources.
	 * @param session Cassandra's session.
	 * @param resources Array of `classpath` resources.
	 * @throws IOException If an I/O error occurs or resource is not found.
	 * @see Session#execute(String)
	 */
	public static void executeScripts(Session session, String... resources)
			throws IOException {
		for (String resource : resources) {
			executeScripts(session, getUrl(resource, null).openStream());
		}
	}

	/**
	 * Executing CQL scripts using {@link URI} resources.
	 * @param session Cassandra's session.
	 * @param resources Array of {@link URI} resources.
	 * @throws IOException If an I/O error occurs or resource is not found.
	 * @see Session#execute(String)
	 */
	public static void executeScripts(Session session, URI... resources)
			throws IOException {
		for (URI resource : resources) {
			executeScripts(session, resource.toURL().openStream());
		}
	}

	/**
	 * Executing CQL scripts using {@link URL} resources.
	 * @param session Cassandra's session.
	 * @param resources Array of {@link URL} resources.
	 * @throws IOException If an I/O error occurs or resource is not found.
	 * @see Session#execute(String)
	 */
	public static void executeScripts(Session session, URL... resources)
			throws IOException {
		for (URL resource : resources) {
			executeScripts(session, resource.openStream());
		}
	}

	/**
	 * Executing CQL scripts using {@link File} resources.
	 * @param session Cassandra's session.
	 * @param resources Array of {@link File} resources.
	 * @throws IOException If an I/O error occurs or resource is not found.
	 * @see Session#execute(String)
	 */
	public static void executeScripts(Session session, File... resources)
			throws IOException {
		for (File resource : resources) {
			executeScripts(session, new FileInputStream(resource));
		}
	}

	/**
	 * Executing CQL scripts using {@link Path} resources.
	 * @param session Cassandra's session.
	 * @param resources Array of {@link Path} resources.
	 * @throws IOException If an I/O error occurs or resource is not found.
	 * @see Session#execute(String)
	 */
	public static void executeScripts(Session session, Path... resources)
			throws IOException {
		for (Path resource : resources) {
			executeScripts(session, new FileInputStream(resource.toFile()));
		}
	}

	/**
	 * Executing CQL scripts using {@link InputStream} resources.
	 * @param session Cassandra's session.
	 * @param resources Array of {@link InputStream} resources.
	 * @throws IOException If an I/O error occurs or resource is not found.
	 * @see Session#execute(String)
	 */
	public static void executeScripts(Session session, InputStream... resources)
			throws IOException {
		for (InputStream resource : resources) {
			executeScripts(session, resource);
		}
	}

	/**
	 * Executing CQL scripts using {@link Reader} resources.
	 * @param session Cassandra's session.
	 * @param resources Array of {@link Reader} resources.
	 * @throws IOException If an I/O error occurs or resource is not found.
	 * @see Session#execute(String)
	 */
	public static void executeScripts(Session session, Reader... resources)
			throws IOException {
		for (Reader resource : resources) {
			executeScripts(session, resource);
		}
	}

	private static void executeScripts(Session session, InputStream resource)
			throws IOException {
		executeScripts(session, new InputStreamReader(resource, StandardCharsets.UTF_8));
	}

	private static void executeScripts(Session session, Reader resource)
			throws IOException {
		String script = readAll(resource);
		List<String> statements = CqlScriptParser.getStatements(script);
		executeStatements(session, statements.toArray(new String[0]));
	}

	private static void executeStatements(Session session, String... statements) {
		for (String statement : statements) {
			session.execute(statement);
		}
	}

	private static URL getUrl(String resource, Class<?> clazz)
			throws FileNotFoundException {
		URL url = null;
		if (clazz != null) {
			url = clazz.getResource(resource);
		}
		if (url == null) {
			ClassLoader cl = getClassLoader();
			if (cl != null) {
				url = cl.getResource(resource);
			}
		}

		if (url == null) {
			throw new FileNotFoundException("Resource [" + resource + "] doesn't exist.");
		}
		return url;

	}

	private static ClassLoader getClassLoader() {
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			if (cl != null) {
				return cl;
			}
		}
		catch (Throwable ignore) {
		}

		try {
			ClassLoader cl = ClassLoader.getSystemClassLoader();
			if (cl != null) {
				return cl;
			}
		}
		catch (Throwable ignore) {
		}

		return null;

	}

	private static String readAll(Reader resource) throws IOException {
		String line;
		StringBuilder result = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(resource)) {
			while ((line = reader.readLine()) != null) {
				result.append(line);
			}
		}
		return result.toString();
	}

}
