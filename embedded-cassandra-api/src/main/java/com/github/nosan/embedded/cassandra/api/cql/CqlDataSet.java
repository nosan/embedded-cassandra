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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.github.nosan.embedded.cassandra.commons.io.ClassPathResource;
import com.github.nosan.embedded.cassandra.commons.io.Resource;

/**
 * {@link CqlDataSet} interface that contains a list of {@code CQL} statements.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public interface CqlDataSet {

	/**
	 * Constructs a new {@link CqlDataSet} with the specified CQL ({@code string}) scripts.
	 *
	 * @param scripts the CQL scripts
	 * @return a new {@link CqlDataSet}
	 */
	static CqlDataSet ofStrings(String... scripts) {
		Objects.requireNonNull(scripts, "'scripts' must not be null");
		return new StringsCqlDataSet(scripts);
	}

	/**
	 * Constructs a new {@link CqlDataSet} with the specified {@link Resource Resources}.
	 *
	 * @param resources the resources
	 * @return a new {@link CqlDataSet}
	 */
	static CqlDataSet ofResources(Resource... resources) {
		return ofResources(StandardCharsets.UTF_8, resources);
	}

	/**
	 * Constructs a new {@link CqlDataSet} with the specified {@link Resource Resources} and {@link Charset}.
	 *
	 * @param resources the resources
	 * @param charset the charset to use
	 * @return a new {@link CqlDataSet}
	 */
	static CqlDataSet ofResources(Charset charset, Resource... resources) {
		Objects.requireNonNull(resources, "'resources' must not be null");
		Objects.requireNonNull(charset, "'charset' must not be null");
		return new ResourcesCqlDataSet(charset, resources);
	}

	/**
	 * Constructs a new {@link CqlDataSet} with the specified resource names.
	 *
	 * @param names the resource names
	 * @return a new {@link CqlDataSet}
	 */
	static CqlDataSet ofClasspaths(String... names) {
		return ofClasspaths(StandardCharsets.UTF_8, names);
	}

	/**
	 * Constructs a new {@link CqlDataSet} with the specified resource names and {@link Charset}.
	 *
	 * @param names the resource names
	 * @param charset the charset to use
	 * @return a new {@link CqlDataSet}
	 */
	static CqlDataSet ofClasspaths(Charset charset, String... names) {
		Objects.requireNonNull(charset, "'charset' must not be null");
		Objects.requireNonNull(names, "'names' must not be null");
		return ofResources(charset, Arrays.stream(names).map(ClassPathResource::new).toArray(Resource[]::new));
	}

	/**
	 * Performs the given {@code callback} for each statement of the {@link CqlDataSet}.
	 *
	 * @param callback The action to be performed for each statement
	 */
	default void forEach(Consumer<? super String> callback) {
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
