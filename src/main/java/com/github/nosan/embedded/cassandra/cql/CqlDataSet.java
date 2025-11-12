/*
 * Copyright 2020-2025 the original author or authors.
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
 * A {@link CqlDataSet} represents a collection of {@link CqlScript} instances, providing functionality to group and
 * manage multiple CQL scripts that can be executed sequentially.
 *
 * <p>This interface provides several factory methods for creating {@link CqlDataSet} instances, as well as
 * a {@link Builder} class for more customizable configurations.</p>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * // Creating a CqlDataSet from classpath resources
 * CqlDataSet dataSet = CqlDataSet.ofClassPaths("schema.cql", "data.cql");
 *
 * // Using a builder to combine multiple resources and scripts
 * CqlDataSet dataSet = CqlDataSet.builder()
 *         .addResource(new ClassPathResource("schema.cql"))
 *         .addScript("CREATE KEYSPACE test WITH replication = {...};")
 *         .build();
 *
 * // Iterating over CQL statements
 * dataSet.getStatements().forEach(System.out::println);
 * }</pre>
 *
 * @author Dmytro Nosan
 * @see CqlScript
 * @see DefaultCqlDataSet
 * @since 4.0.1
 */
@FunctionalInterface
public interface CqlDataSet extends CqlScript {

	/**
	 * Creates a new {@link CqlDataSet.Builder} instance for constructing {@code CqlDataSet} objects.
	 *
	 * @return a new {@link CqlDataSet.Builder}
	 * @since 5.0.0
	 */
	static Builder builder() {
		return new DefaultCqlDataSet.Builder();
	}

	/**
	 * Creates a {@link CqlDataSet} from the specified classpath resource names, using the default charset.
	 *
	 * @param names the resource names (must not be {@code null})
	 * @return a new {@link CqlDataSet} instance
	 * @throws NullPointerException if {@code names} is {@code null}
	 */
	static CqlDataSet ofClassPaths(String... names) {
		return ofClassPaths(Charset.defaultCharset(), names);
	}

	/**
	 * Creates a {@link CqlDataSet} from the specified classpath resource names, using the provided {@link Charset}.
	 *
	 * @param names the resource names (must not be {@code null})
	 * @param charset the character encoding to use when reading the resources (must not be {@code null})
	 * @return a new {@link CqlDataSet} instance
	 * @throws NullPointerException if {@code names} or {@code charset} is {@code null}
	 */
	static CqlDataSet ofClassPaths(Charset charset, String... names) {
		Objects.requireNonNull(charset, "Charset must not be null");
		Objects.requireNonNull(names, "Classpath resources must not be null");
		return new DefaultCqlDataSet(Arrays.stream(names)
				.map(ClassPathResource::new)
				.map(resource -> new ResourceCqlScript(resource, charset))
				.collect(Collectors.toList()));
	}

	/**
	 * Creates a {@link CqlDataSet} from the specified {@link Resource} instances, using the default charset.
	 *
	 * @param resources the resources to use (must not be {@code null})
	 * @return a new {@link CqlDataSet} instance
	 * @throws NullPointerException if {@code resources} is {@code null}
	 */
	static CqlDataSet ofResources(Resource... resources) {
		return ofResources(Charset.defaultCharset(), resources);
	}

	/**
	 * Creates a {@link CqlDataSet} from the specified {@link Resource} instances, using the provided {@link Charset}.
	 *
	 * @param resources the resources to use (must not be {@code null})
	 * @param charset the character encoding to use when reading the resources (must not be {@code null})
	 * @return a new {@link CqlDataSet} instance
	 * @throws NullPointerException if {@code resources} or {@code charset} is {@code null}
	 */
	static CqlDataSet ofResources(Charset charset, Resource... resources) {
		Objects.requireNonNull(charset, "Charset must not be null");
		Objects.requireNonNull(resources, "Resources must not be null");
		return new DefaultCqlDataSet(Arrays.stream(resources)
				.map(resource -> new ResourceCqlScript(resource, charset))
				.collect(Collectors.toList()));
	}

	/**
	 * Creates a {@link CqlDataSet} from the specified {@link CqlScript} instances.
	 *
	 * <p>If one or more {@code CqlScript} instances are themselves instances of {@code CqlDataSet}, their
	 * individual scripts are flattened into the resulting dataset.</p>
	 *
	 * @param scripts the scripts to include (must not be {@code null})
	 * @return a new {@link CqlDataSet} instance
	 * @throws NullPointerException if {@code scripts} is {@code null}
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
	 * Executes the provided {@code callback} for each {@link CqlScript} in the {@link CqlDataSet}.
	 *
	 * @param callback the action to perform on each script (must not be {@code null})
	 * @throws NullPointerException if {@code callback} is {@code null}
	 */
	default void forEachScript(Consumer<? super CqlScript> callback) {
		Objects.requireNonNull(callback, "Callback must not be null");
		getScripts().forEach(callback);
	}

	/**
	 * Retrieves all the CQL statements from every script in the {@link CqlDataSet}.
	 *
	 * @return an unmodifiable list of all CQL statements (never {@code null})
	 */
	@Override
	default List<String> getStatements() {
		List<String> statements = new ArrayList<>();
		forEachScript(script -> statements.addAll(script.getStatements()));
		return Collections.unmodifiableList(statements);
	}

	/**
	 * Retrieves all the {@link CqlScript} instances contained within this {@link CqlDataSet}.
	 *
	 * @return a list of {@link CqlScript} instances (never {@code null})
	 */
	List<? extends CqlScript> getScripts();

	/**
	 * A builder for constructing instances of {@link CqlDataSet}.
	 *
	 * <p>Provides methods for adding individual scripts, resources, and static statements to the dataset.</p>
	 *
	 * @since 5.0.0
	 */
	interface Builder {

		/**
		 * Adds a {@link CqlScript} to the builder.
		 *
		 * @param script the {@link CqlScript} to add (must not be {@code null})
		 * @return this builder
		 * @throws NullPointerException if {@code script} is {@code null}
		 */
		Builder addScript(CqlScript script);

		/**
		 * Adds a plain CQL script string to the builder.
		 *
		 * @param script the plain string script to add (must not be {@code null})
		 * @return this builder
		 * @throws NullPointerException if {@code script} is {@code null}
		 */
		Builder addScript(String script);

		/**
		 * Adds a {@link Resource} with the default charset to the builder.
		 *
		 * @param resource the {@link Resource} to add (must not be {@code null})
		 * @return this builder
		 * @throws NullPointerException if {@code resource} is {@code null}
		 */
		Builder addResource(Resource resource);

		/**
		 * Adds a {@link Resource} with the specified {@link Charset} to the builder.
		 *
		 * @param resource the {@link Resource} to add (must not be {@code null})
		 * @param charset the {@link Charset} to use (must not be {@code null})
		 * @return this builder
		 * @throws NullPointerException if {@code resource} or {@code charset} is {@code null}
		 */
		Builder addResource(Resource resource, Charset charset);

		/**
		 * Adds one or more static CQL statements to the builder.
		 *
		 * @param statements the CQL statements to add (must not be {@code null})
		 * @return this builder
		 * @throws NullPointerException if {@code statements} is {@code null}
		 */
		Builder addStatements(String... statements);

		/**
		 * Adds a list of static CQL statements to the builder.
		 *
		 * @param statements the list of CQL statements to add (must not be {@code null})
		 * @return this builder
		 * @throws NullPointerException if {@code statements} is {@code null}
		 */
		Builder addStatements(List<? extends String> statements);

		/**
		 * Builds and returns a {@link CqlDataSet} instance.
		 *
		 * @return a new {@link CqlDataSet}
		 */
		CqlDataSet build();

	}

}
