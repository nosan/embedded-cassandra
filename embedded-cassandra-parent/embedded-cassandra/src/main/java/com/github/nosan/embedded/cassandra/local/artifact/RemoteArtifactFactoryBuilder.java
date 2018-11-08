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

import java.io.File;
import java.net.Proxy;
import java.nio.file.Path;
import java.time.Duration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Builder to create a {@link RemoteArtifactFactory}.
 *
 * @author Dmytro Nosan
 * @see RemoteArtifactFactory
 * @since 1.0.0
 */
public final class RemoteArtifactFactoryBuilder {

	@Nullable
	private Path directory;

	@Nullable
	private UrlFactory urlFactory;

	@Nullable
	private Proxy proxy;

	@Nullable
	private Duration readTimeout;

	@Nullable
	private Duration connectTimeout;


	/**
	 * Initializes the value for the {@link RemoteArtifactFactory#getDirectory() directory} attribute.
	 *
	 * @param directory The value for directory
	 * @return {@code this} builder for use in a chained invocation
	 */
	@Nonnull
	public RemoteArtifactFactoryBuilder setDirectory(@Nullable Path directory) {
		this.directory = directory;
		return this;
	}

	/**
	 * Initializes the value for the {@link RemoteArtifactFactory#getDirectory() directory} attribute.
	 *
	 * @param directory The value for directory
	 * @return {@code this} builder for use in a chained invocation
	 */
	@Nonnull
	public RemoteArtifactFactoryBuilder setDirectory(@Nullable File directory) {
		return setDirectory((directory != null) ? directory.toPath() : null);
	}

	/**
	 * Initializes the value for the {@link RemoteArtifactFactory#getUrlFactory() urlFactory} attribute.
	 *
	 * @param urlFactory The value for urlFactory
	 * @return {@code this} builder for use in a chained invocation
	 * @see DefaultUrlFactory
	 */
	@Nonnull
	public RemoteArtifactFactoryBuilder setUrlFactory(@Nullable UrlFactory urlFactory) {
		this.urlFactory = urlFactory;
		return this;
	}

	/**
	 * Initializes the value for the {@link RemoteArtifactFactory#getProxy() proxy} attribute.
	 *
	 * @param proxy The value for proxy
	 * @return {@code this} builder for use in a chained invocation
	 */
	@Nonnull
	public RemoteArtifactFactoryBuilder setProxy(@Nullable Proxy proxy) {
		this.proxy = proxy;
		return this;
	}

	/**
	 * Initializes the value for the {@link RemoteArtifactFactory#getReadTimeout() readTimeout} attribute.
	 *
	 * @param readTimeout The value for readTimeout
	 * @return {@code this} builder for use in a chained invocation
	 */
	@Nonnull
	public RemoteArtifactFactoryBuilder setReadTimeout(@Nullable Duration readTimeout) {
		this.readTimeout = readTimeout;
		return this;
	}

	/**
	 * Initializes the value for the {@link RemoteArtifactFactory#getConnectTimeout() connectTimeout} attribute.
	 *
	 * @param connectTimeout The value for connectTimeout
	 * @return {@code this} builder for use in a chained invocation
	 */
	@Nonnull
	public RemoteArtifactFactoryBuilder setConnectTimeout(@Nullable Duration connectTimeout) {
		this.connectTimeout = connectTimeout;
		return this;
	}


	/**
	 * Builds a new {@link RemoteArtifactFactory}.
	 *
	 * @return a new instance
	 */
	@Nonnull
	public RemoteArtifactFactory build() {
		RemoteArtifactFactory factory = new RemoteArtifactFactory();
		factory.setConnectTimeout(this.connectTimeout);
		factory.setReadTimeout(this.readTimeout);
		factory.setDirectory(this.directory);
		factory.setProxy(this.proxy);
		factory.setUrlFactory(this.urlFactory);
		return factory;
	}
}
