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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.github.nosan.embedded.cassandra.commons.ClassPathResource;
import com.github.nosan.embedded.cassandra.commons.Resource;

/**
 * A {@link CqlScript} represents a collection of executable Cassandra Query Language (CQL) statements.
 *
 * <p>This functional interface provides methods for constructing {@code CqlScript} instances from various sources,
 * such as resources, classpath files, or raw strings. Implementations of this interface encapsulate CQL statements and
 * provide convenient functionality for working with them.</p>
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * // Load CQL from a classpath resource
 * CqlScript script = CqlScript.ofClassPath("example.cql");
 * script.forEachStatement(System.out::println);
 *
 * // Create CQL script from explicit statements
 * CqlScript script = CqlScript.ofStatements("CREATE TABLE test (...)", "INSERT INTO test VALUES (...)");
 * script.getStatements().forEach(System.out::println);
 * }</pre>
 *
 * @author Dmytro Nosan
 * @see CqlDataSet
 * @see AbstractCqlScript
 * @see ResourceCqlScript
 * @see StringCqlScript
 * @since 4.0.0
 */
@FunctionalInterface
public interface CqlScript {

	/**
	 * Creates a {@link CqlScript} instance using a classpath resource and the default {@link Charset}.
	 *
	 * @param name the name of the resource (must not be {@code null})
	 * @return a new {@link CqlScript} instance
	 * @throws NullPointerException if {@code name} is {@code null}
	 */
	static CqlScript ofClassPath(String name) {
		return ofClassPath(name, Charset.defaultCharset());
	}

	/**
	 * Creates a {@link CqlScript} instance using a classpath resource and the specified {@link Charset}.
	 *
	 * @param name the name of the resource (must not be {@code null})
	 * @param charset the character encoding to use when reading the resource (must not be {@code null})
	 * @return a new {@link CqlScript} instance
	 * @throws NullPointerException if {@code name} or {@code charset} is {@code null}
	 */
	static CqlScript ofClassPath(String name, Charset charset) {
		return ofResource(new ClassPathResource(name), charset);
	}

	/**
	 * Creates a {@link CqlScript} instance using the specified {@link Resource} and the default {@link Charset}.
	 *
	 * @param resource the {@link Resource} to use (must not be {@code null})
	 * @return a new {@link CqlScript} instance
	 * @throws NullPointerException if {@code resource} is {@code null}
	 * @since 4.0.1
	 */
	static CqlScript ofResource(Resource resource) {
		return ofResource(resource, Charset.defaultCharset());
	}

	/**
	 * Creates a {@link CqlScript} instance using the specified {@link Resource} and {@link Charset}.
	 *
	 * @param resource the {@link Resource} to use (must not be {@code null})
	 * @param charset the character encoding to use when reading the resource (must not be {@code null})
	 * @return a new {@link CqlScript} instance
	 * @throws NullPointerException if {@code resource} or {@code charset} is {@code null}
	 * @since 4.0.1
	 */
	static CqlScript ofResource(Resource resource, Charset charset) {
		Objects.requireNonNull(charset, "Charset must not be null");
		Objects.requireNonNull(resource, "Resource must not be null");
		return new ResourceCqlScript(resource, charset);
	}

	/**
	 * Creates a {@link CqlScript} instance from an array of CQL statements.
	 *
	 * @param statements an array of CQL statements (must not be {@code null})
	 * @return a new {@link CqlScript} instance
	 * @throws NullPointerException if {@code statements} is {@code null}
	 * @since 5.0.0
	 */
	static CqlScript ofStatements(String... statements) {
		Objects.requireNonNull(statements, "Statements must not be null");
		return ofStatements(Arrays.asList(statements));
	}

	/**
	 * Creates a {@link CqlScript} instance from a {@link List} of CQL statements.
	 *
	 * @param statements a list of CQL statements (must not be {@code null})
	 * @return a new {@link CqlScript} instance
	 * @throws NullPointerException if {@code statements} is {@code null}
	 * @since 5.0.0
	 */
	static CqlScript ofStatements(List<? extends String> statements) {
		Objects.requireNonNull(statements, "Statements must not be null");
		return new StatementsCqlScript(statements);
	}

	/**
	 * Iterates over all the statements in this {@link CqlScript}, applying the provided {@link Consumer}.
	 *
	 * <p>This method allows developers to perform an action (e.g., execution, logging) for each individual
	 * CQL statement.</p>
	 *
	 * @param callback a {@link Consumer} that processes each statement (must not be {@code null})
	 * @throws NullPointerException if {@code callback} is {@code null}
	 */
	default void forEachStatement(Consumer<? super String> callback) {
		Objects.requireNonNull(callback, "Callback must not be null");
		getStatements().forEach(callback);
	}

	/**
	 * Retrieves the list of CQL statements encapsulated within this {@link CqlScript}.
	 *
	 * <p>The returned {@link List} contains all the parsed or explicitly provided statements. This method
	 * guarantees that the result is never {@code null}, but the list may be empty if no statements are provided or
	 * parsed.</p>
	 *
	 * @return an unmodifiable list of CQL statements (never {@code null})
	 */
	List<String> getStatements();

}
