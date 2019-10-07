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

package com.github.nosan.embedded.cassandra.api.cql;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.github.nosan.embedded.cassandra.commons.io.ClassPathResource;
import com.github.nosan.embedded.cassandra.commons.io.Resource;

/**
 * {@link CqlScript} interface that contains a list of {@code CQL} statements.
 *
 * @author Dmytro Nosan
 * @see AbstractCqlScript
 * @since 3.0.0
 */
@FunctionalInterface
public interface CqlScript {

	/**
	 * Constructs a new {@link CqlScript} with the specified CQL ({@code string}) script.
	 *
	 * @param script the CQL script
	 * @return a new {@link CqlScript}
	 */
	static CqlScript ofString(String script) {
		Objects.requireNonNull(script, "'script' must not be null");
		return new StringCqlScript(script);
	}

	/**
	 * Constructs a new {@link CqlScript} with the specified {@link Resource}.
	 *
	 * @param resource the resource
	 * @return a new {@link CqlScript}
	 */
	static CqlScript ofResource(Resource resource) {
		return ofResource(StandardCharsets.UTF_8, resource);
	}

	/**
	 * Constructs a new {@link CqlScript} with the specified {@link Resource} and {@link Charset}.
	 *
	 * @param resource the resource
	 * @param charset the charset to use
	 * @return a new {@link CqlScript}
	 */
	static CqlScript ofResource(Charset charset, Resource resource) {
		Objects.requireNonNull(charset, "'charset' must not be null");
		Objects.requireNonNull(resource, "'resource' must not be null");
		return new ResourceCqlScript(charset, resource);
	}

	/**
	 * Constructs a new {@link CqlScript} with the specified resource name.
	 *
	 * @param name the resource name
	 * @return a new {@link CqlScript}
	 */
	static CqlScript ofClasspath(String name) {
		return ofClasspath(StandardCharsets.UTF_8, name);
	}

	/**
	 * Constructs a new {@link CqlScript} with the specified resource name and {@link Charset}.
	 *
	 * @param name the resource name
	 * @param charset the charset to use
	 * @return a new {@link CqlScript}
	 */
	static CqlScript ofClasspath(Charset charset, String name) {
		Objects.requireNonNull(charset, "'charset' must not be null");
		Objects.requireNonNull(name, "'name' must not be null");
		return ofResource(charset, new ClassPathResource(name));
	}

	/**
	 * Performs the given {@code callback} for each statement of the {@link CqlScript}.
	 *
	 * @param callback The action to be performed for each statement
	 */
	default void forEachStatement(Consumer<? super String> callback) {
		Objects.requireNonNull(callback, "'callback' must not be null");
		getStatements().forEach(callback);
	}

	/**
	 * Returns {@code CQL} statements.
	 *
	 * @return {@code CQL} statements
	 */
	List<String> getStatements();

}
