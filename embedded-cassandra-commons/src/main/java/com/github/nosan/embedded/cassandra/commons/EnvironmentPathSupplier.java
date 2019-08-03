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

package com.github.nosan.embedded.cassandra.commons;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import com.github.nosan.embedded.cassandra.annotations.Nullable;

/**
 * {@link PathSupplier} that returns the path based on the specified env property.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public class EnvironmentPathSupplier implements PathSupplier {

	private final String name;

	/**
	 * Constructs a {@link EnvironmentPathSupplier} with the specified env property name.
	 *
	 * @param name the name of the env property
	 */
	public EnvironmentPathSupplier(String name) {
		this.name = Objects.requireNonNull(name, "'name' must not be null");
	}

	@Nullable
	@Override
	public Path get() {
		String path = System.getenv(this.name);
		return (path != null) ? Paths.get(path) : null;
	}

}
