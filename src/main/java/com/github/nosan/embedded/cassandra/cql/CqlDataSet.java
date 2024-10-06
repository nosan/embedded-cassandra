/*
 * Copyright 2020-2024 the original author or authors.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.nosan.embedded.cassandra.commons.ClassPathResource;
import com.github.nosan.embedded.cassandra.commons.Resource;

/**
 * {@link CqlDataSet} represents the set of {@link CqlScript}.
 *
 * @author Dmytro Nosan
 * @see CqlScript
 * @see DefaultCqlDataSet
 * @since 4.0.1
 */
@FunctionalInterface
public interface CqlDataSet extends CqlScript {

	/**
	 * Creates a new {@link CqlDataSet.Builder}.
	 *
	 * @return a new {@link CqlDataSet.Builder}
	 * @since 5.0.0
	 */
	static Builder builder() {
		return new DefaultCqlDataSet.Builder();
	}

	/**
	 * Creates {@link CqlDataSet} with the specified resource names and default charset.
	 *
	 * @param names the resource names
	 * @return a new {@link CqlDataSet}
	 */
	static CqlDataSet ofClassPaths(String... names) {
		return ofClassPaths(Charset.defaultCharset(), names);
	}

	/**
	 * Creates {@link CqlDataSet} with the specified resource names and charset.
	 *
	 * @param names the resource names
	 * @param charset the encoding to use
	 * @return a new {@link CqlDataSet}
	 */
	static CqlDataSet ofClassPaths(Charset charset, String... names) {
		Objects.requireNonNull(charset, "Charset must not be null");
		Objects.requireNonNull(names, "Classpath resources must not be null");
		return new DefaultCqlDataSet(Arrays.stream(names).map(ClassPathResource::new)
				.map(resource -> new ResourceCqlScript(resource, charset)).collect(Collectors.toList()));
	}

	/**
	 * Creates {@link CqlDataSet} with the specified resources and default charset.
	 *
	 * @param resources the resources to use
	 * @return a new {@link CqlDataSet}
	 */
	static CqlDataSet ofResources(Resource... resources) {
		return ofResources(Charset.defaultCharset(), resources);
	}

	/**
	 * Creates {@link CqlDataSet} with the specified resources and charset.
	 *
	 * @param resources the resources to use
	 * @param charset the encoding to use
	 * @return a new {@link CqlDataSet}
	 */
	static CqlDataSet ofResources(Charset charset, Resource... resources) {
		Objects.requireNonNull(charset, "Charset must not be null");
		Objects.requireNonNull(resources, "Resources must not be null");
		return new DefaultCqlDataSet(Arrays.stream(resources).map(resource -> new ResourceCqlScript(resource, charset))
				.collect(Collectors.toList()));
	}

	/**
	 * Creates {@link CqlDataSet} with the specified CQL scripts.
	 *
	 * @param scripts the scripts to use
	 * @return a new {@link CqlDataSet}
	 */
	static CqlDataSet ofScripts(CqlScript... scripts) {
		Objects.requireNonNull(scripts, "Cql Scripts must not be null");
		List<CqlScript> result = new ArrayList<>();
		for (CqlScript script : scripts) {
			if (script instanceof CqlDataSet) {
				result.addAll(((CqlDataSet) script).getScripts());
			}
			else {
				result.add(script);
			}
		}
		return new DefaultCqlDataSet(result);
	}

	/**
	 * Performs the given {@code callback} for each script of the {@link CqlDataSet}.
	 *
	 * @param callback The action to be performed for each script
	 */
	default void forEachScript(Consumer<? super CqlScript> callback) {
		Objects.requireNonNull(callback, "Callback must not be null");
		getScripts().forEach(callback);
	}

	@Override
	default List<String> getStatements() {
		List<String> statements = new ArrayList<>();
		forEachScript(script -> statements.addAll(script.getStatements()));
		return Collections.unmodifiableList(statements);
	}

	/**
	 * Gets {@code CQL} scripts.
	 *
	 * @return {@code CQL} scripts (never null)
	 */
	List<? extends CqlScript> getScripts();

	/**
	 * A {@link  CqlDataSet} builder.
	 *
	 * @since 5.0.0
	 */
	interface Builder {

		/**
		 * Add {@link  CqlScript} to the builder.
		 *
		 * @param script {@link  CqlScript} to add
		 * @return self
		 */
		Builder addScript(CqlScript script);

		/**
		 * Add a plain script to the builder.
		 *
		 * @param script plain strings script to add
		 * @return self
		 */
		Builder addScript(String script);

		/**
		 * Add {@link  Resource} with default UTF-8 encoding to the builder.
		 *
		 * @param resource {@link  Resource} to add
		 * @return self
		 */
		Builder addResource(Resource resource);

		/**
		 * Add {@link  Resource} with the provided encoding to the builder.
		 *
		 * @param resource {@link  Resource} to add
		 * @param charset encoding to use
		 * @return self
		 */
		Builder addResource(Resource resource, Charset charset);

		/**
		 * Add static statements to the builder.
		 *
		 * @param statements static statements
		 * @return self
		 */
		Builder addStatements(String... statements);

		/**
		 * Add static statements to the builder.
		 *
		 * @param statements static statements
		 * @return self
		 */
		Builder addStatements(List<? extends String> statements);

		/**
		 * Build a new {@link  CqlDataSet}.
		 *
		 * @return a new {@link CqlDataSet}
		 */
		CqlDataSet build();

	}

}
