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

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.test.support.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link RemoteArtifactFactory}.
 *
 * @author Dmytro Nosan
 */
class RemoteArtifactFactoryTests {

	@Test
	void createConfigureRemoteArtifact() {
		RemoteArtifactFactory factory = new RemoteArtifactFactory();
		Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("locahost", 8080));
		UrlFactory urlFactory = version -> new URL[0];
		factory.setUrlFactory(urlFactory);
		factory.setProxy(proxy);
		factory.setReadTimeout(Duration.ofSeconds(100));
		factory.setConnectTimeout(Duration.ofMinutes(100));

		RemoteArtifact artifact = (RemoteArtifact) factory.create(Version.parse("3.11.2"));
		assertThat(ReflectionUtils.getField(artifact, "version")).isEqualTo(Version.parse("3.11.2"));
		assertThat(ReflectionUtils.getField(artifact, "urlFactory")).isEqualTo(urlFactory);
		assertThat(ReflectionUtils.getField(artifact, "proxy")).isEqualTo(proxy);
		assertThat(ReflectionUtils.getField(artifact, "readTimeout")).isEqualTo(Duration.ofSeconds(100));
		assertThat(ReflectionUtils.getField(artifact, "connectTimeout")).isEqualTo(Duration.ofMinutes(100));
	}

	@Test
	void createDefaultRemoteArtifact() {
		RemoteArtifactFactory factory = new RemoteArtifactFactory();
		RemoteArtifact artifact = (RemoteArtifact) factory.create(Version.parse("3.11.3"));
		assertThat(ReflectionUtils.getField(artifact, "version")).isEqualTo(Version.parse("3.11.3"));
		assertThat(ReflectionUtils.getField(artifact, "urlFactory")).isInstanceOf(DefaultUrlFactory.class);
		assertThat(ReflectionUtils.getField(artifact, "proxy")).isNull();
		assertThat(ReflectionUtils.getField(artifact, "readTimeout")).isEqualTo(Duration.ofSeconds(30));
		assertThat(ReflectionUtils.getField(artifact, "connectTimeout")).isEqualTo(Duration.ofSeconds(30));
	}

}
