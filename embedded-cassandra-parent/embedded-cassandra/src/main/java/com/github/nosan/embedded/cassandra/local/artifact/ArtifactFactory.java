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

package com.github.nosan.embedded.cassandra.local.artifact;

import javax.annotation.Nonnull;

import org.apiguardian.api.API;

import com.github.nosan.embedded.cassandra.Version;

/**
 * Factory that creates a {@link Artifact}.
 *
 * @author Dmytro Nosan
 * @see Artifact
 * @see RemoteArtifactFactory
 * @see EmptyArtifactFactory
 * @since 1.0.0
 */
@API(since = "1.0.0", status = API.Status.STABLE)
@FunctionalInterface
public interface ArtifactFactory {

	/**
	 * Creates a new  configured {@link Artifact}.
	 *
	 * @param version a version
	 * @return {@code Artifact} to use
	 */
	@Nonnull
	Artifact create(@Nonnull Version version);
}
