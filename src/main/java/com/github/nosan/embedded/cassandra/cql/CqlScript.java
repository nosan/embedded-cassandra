/*
 * Copyright 2020 the original author or authors.
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

package com.github.nosan.embedded.cassandra.cql;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.github.nosan.embedded.cassandra.commons.ClassPathResource;
import com.github.nosan.embedded.cassandra.commons.Resource;

/**
 * {@link CqlScript} interface that loads CQL statements from various sources.
 *
 * @author Dmytro Nosan
 * @see CqlDataSet
 * @see AbstractCqlScript
 * @see ResourceCqlScript
 * @see StringCqlScript
 * @see #ofClassPath(String)
 * @since 4.0.0
 */
@FunctionalInterface
public interface CqlScript {

	/**
	 * Creates {@link CqlScript} with the specified resource name and default charset.
	 *
	 * @param name the resource name
	 * @return a new {@link CqlScript}
	 */
	static CqlScript ofClassPath(String name) {
		return ofClassPath(name, Charset.defaultCharset());
	}

	/**
	 * Creates {@link CqlScript} with the specified resource name and charset.
	 *
	 * @param name the resource name
	 * @param charset the encoding to use
	 * @return a new {@link CqlScript}
	 */
	static CqlScript ofClassPath(String name, Charset charset) {
		return ofResource(new ClassPathResource(name), charset);
	}

	/**
	 * Creates {@link CqlScript} with the specified resource and default charset.
	 *
	 * @param resource the resource to use
	 * @return a new {@link CqlScript}
	 * @since 4.0.1
	 */
	static CqlScript ofResource(Resource resource) {
		return ofResource(resource, Charset.defaultCharset());
	}

	/**
	 * Creates {@link CqlScript} with the specified resources and charset.
	 *
	 * @param resource the resource to use
	 * @param charset the encoding to use
	 * @return a new {@link CqlScript}
	 * @since 4.0.1
	 */
	static CqlScript ofResource(Resource resource, Charset charset) {
		Objects.requireNonNull(charset, "Charset must not be null");
		Objects.requireNonNull(resource, "Resources must not be null");
		return new ResourceCqlScript(resource, charset);
	}

	/**
	 * Performs the provided {@code callback} for each statement of the {@link CqlScript}.
	 *
	 * @param callback The action to be performed for each statement
	 */
	default void forEachStatement(Consumer<? super String> callback) {
		Objects.requireNonNull(callback, "Callback must not be null");
		getStatements().forEach(callback);
	}

	/**
	 * Gets {@code CQL} statements.
	 *
	 * @return {@code CQL} statements (never null)
	 */
	List<String> getStatements();

}
