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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tests for {@link GlobUtils}.
 *
 * @author Dmytro Nosan
 */
public class GlobUtilsTests {

	@Test
	public void walkPath() throws URISyntaxException {
		ClassLoader classLoader = getClass().getClassLoader();
		String glob = "**";
		URI uri = getClass().getResource("/com/github").toURI();
		List<URI> uris = GlobUtils.walkFileTree(uri, classLoader, glob);
		assertThat(uris).containsExactly(getClass().getResource("keyspace.cql").toURI());
		assertThat(uris).allMatch(GlobUtilsTests::hasStream);
	}

	@Test
	public void walkJar() throws URISyntaxException {
		URI uri = getClass().getResource("/test.jar").toURI();
		String glob = "**";
		ClassLoader classLoader = getClass().getClassLoader();
		List<URI> uris = GlobUtils.walkFileTree(uri, classLoader, glob);
		assertThat(uris).hasSize(3);
		assertThat(uris).allMatch(GlobUtilsTests::hasStream);
	}

	@Test
	public void walkJarUri() throws URISyntaxException {
		URI uri = new URI("jar", getClass().getResource("/test.jar").toURI().toString(), "!/");
		String glob = "**";
		ClassLoader classLoader = getClass().getClassLoader();
		List<URI> uris = GlobUtils.walkFileTree(uri, classLoader, glob);
		assertThat(uris).hasSize(3);
		assertThat(uris).allMatch(GlobUtilsTests::hasStream);
	}

	@Test
	public void walkZip() throws URISyntaxException {
		URI uri = getClass().getResource("/apache-cassandra-3.11.3.zip").toURI();
		String glob = "**";
		ClassLoader classLoader = getClass().getClassLoader();
		List<URI> uris = GlobUtils.walkFileTree(uri, classLoader, glob);
		assertThat(uris).hasSize(49);
		assertThat(uris).allMatch(GlobUtilsTests::hasStream);
	}

	private static boolean hasStream(URI uri) {
		try {
			try (InputStream ignore = uri.toURL().openStream()) {
				return true;
			}
		}
		catch (IOException ex) {
			fail(String.format("Could not open stream for (%s)", uri), ex);
		}
		return false;
	}

}
