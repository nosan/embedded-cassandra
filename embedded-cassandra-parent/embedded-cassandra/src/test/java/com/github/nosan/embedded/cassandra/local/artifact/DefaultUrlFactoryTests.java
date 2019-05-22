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

package com.github.nosan.embedded.cassandra.local.artifact;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.Version;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultUrlFactory}.
 *
 * @author Dmytro Nosan
 */
class DefaultUrlFactoryTests {

	private final DefaultUrlFactory urlFactory = new DefaultUrlFactory();

	@Test
	void shouldCreateURL() throws MalformedURLException {
		assertThat(this.urlFactory.create(Version.parse("3.11.3"))).isEqualTo(new URL[]{
				new URL("https://apache.org/dyn/closer.cgi?action=download&filename=cassandra/3.11.3/"
						+ "apache-cassandra-3.11.3-bin.tar.gz"),
				new URL("https://dist.apache.org/repos/dist/release/cassandra/3.11.3/"
						+ "apache-cassandra-3.11.3-bin.tar.gz"),
				new URL("https://archive.apache.org/dist/cassandra/3.11.3/"
						+ "apache-cassandra-3.11.3-bin.tar.gz")});

	}

}
