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

package com.github.nosan.embedded.cassandra.local.artifact;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.time.Duration;

import javax.annotation.Nonnull;

import org.junit.Test;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.util.FileUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link RemoteArtifactFactoryBuilder}.
 *
 * @author Dmytro Nosan
 */
public class RemoteArtifactFactoryBuilderTests {

	@Test
	public void buildFactory() {

		Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("locahost", 8080));
		UrlFactory urlFactory = new UrlFactory() {
			@Nonnull
			@Override
			public URL[] create(@Nonnull Version version) {
				return new URL[0];
			}
		};

		RemoteArtifactFactory factory = new RemoteArtifactFactoryBuilder()
				.setUrlFactory(urlFactory)
				.setProxy(proxy)
				.setDirectory(FileUtils.getTmpDirectory())
				.setReadTimeout(Duration.ofSeconds(100))
				.setConnectTimeout(Duration.ofMinutes(100))
				.build();


		assertThat(factory.getDirectory()).isEqualTo(FileUtils.getTmpDirectory());
		assertThat(factory.getUrlFactory()).isEqualTo(urlFactory);
		assertThat(factory.getProxy()).isEqualTo(proxy);
		assertThat(factory.getReadTimeout()).isEqualTo(Duration.ofSeconds(100));
		assertThat(factory.getConnectTimeout()).isEqualTo(Duration.ofMinutes(100));
	}

	@Test
	public void defaultBuildFactory() {
		RemoteArtifactFactory factory = new RemoteArtifactFactoryBuilder()
				.build();

		assertThat(factory.getDirectory()).isNull();
		assertThat(factory.getUrlFactory()).isNull();
		assertThat(factory.getProxy()).isNull();
		assertThat(factory.getReadTimeout()).isNull();
		assertThat(factory.getConnectTimeout()).isNull();
	}
}
