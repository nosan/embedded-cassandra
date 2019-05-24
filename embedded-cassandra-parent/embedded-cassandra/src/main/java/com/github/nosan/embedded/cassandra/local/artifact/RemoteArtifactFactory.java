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

import java.net.Proxy;
import java.net.URL;
import java.time.Duration;
import java.util.Objects;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * {@link ArtifactFactory} to create a {@link RemoteArtifact}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
public class RemoteArtifactFactory implements ArtifactFactory {

	@Nullable
	private UrlFactory urlFactory;

	@Nullable
	private Proxy proxy;

	@Nullable
	private Duration readTimeout;

	@Nullable
	private Duration connectTimeout;

	/**
	 * Factory that creates {@link URL URLs} for downloading an archive.
	 *
	 * @return The value of the {@code urlFactory} attribute
	 */
	@Nullable
	public UrlFactory getUrlFactory() {
		return this.urlFactory;
	}

	/**
	 * Initializes the value for the {@link RemoteArtifactFactory#getUrlFactory()} attribute.
	 *
	 * @param urlFactory The value for urlFactory
	 * @see DefaultUrlFactory
	 */
	public void setUrlFactory(@Nullable UrlFactory urlFactory) {
		this.urlFactory = urlFactory;
	}

	/**
	 * Proxy settings.
	 *
	 * @return The value of the {@code proxy} attribute
	 */
	@Nullable
	public Proxy getProxy() {
		return this.proxy;
	}

	/**
	 * Initializes the value for the {@link RemoteArtifactFactory#getProxy()} attribute.
	 *
	 * @param proxy The value for proxy
	 */
	public void setProxy(@Nullable Proxy proxy) {
		this.proxy = proxy;
	}

	/**
	 * Read timeout specifies the timeout when reading from InputStream when a connection is established to a resource.
	 *
	 * @return The value of the {@code readTimeout} attribute
	 */
	@Nullable
	public Duration getReadTimeout() {
		return this.readTimeout;
	}

	/**
	 * Initializes the value for the {@link RemoteArtifactFactory#getReadTimeout()} attribute.
	 *
	 * @param readTimeout The value for readTimeout
	 */
	public void setReadTimeout(@Nullable Duration readTimeout) {
		this.readTimeout = readTimeout;
	}

	/**
	 * Connection timeout to be used when opening a communications link to the resource referenced by URLConnection.
	 *
	 * @return The value of the {@code connectTimeout} attribute
	 */
	@Nullable
	public Duration getConnectTimeout() {
		return this.connectTimeout;
	}

	/**
	 * Initializes the value for the {@link RemoteArtifactFactory#getConnectTimeout()} attribute.
	 *
	 * @param connectTimeout The value for connectTimeout
	 */
	public void setConnectTimeout(@Nullable Duration connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	@Override
	public Artifact create(Version version) {
		Objects.requireNonNull(version, "Version must not be null");
		UrlFactory urlFactory = getUrlFactory();
		if (urlFactory == null) {
			urlFactory = new DefaultUrlFactory();
		}
		Duration readTimeout = getReadTimeout();
		if (readTimeout == null) {
			readTimeout = Duration.ofSeconds(30);
		}
		Duration connectTimeout = getConnectTimeout();
		if (connectTimeout == null) {
			connectTimeout = Duration.ofSeconds(30);
		}
		return new RemoteArtifact(version, urlFactory, getProxy(), readTimeout, connectTimeout);
	}

}
