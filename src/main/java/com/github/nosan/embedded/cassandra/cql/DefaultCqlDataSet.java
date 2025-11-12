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
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.github.nosan.embedded.cassandra.commons.Resource;

/**
 * Default implementation of the {@link CqlDataSet} interface.
 *
 * @author Dmytro Nosan
 * @see CqlDataSet
 * @see Builder
 * @since 4.0.1
 */
public class DefaultCqlDataSet implements CqlDataSet {

	private final List<CqlScript> scripts;

	/**
	 * Creates a new immutable {@link DefaultCqlDataSet} with the specified CQL scripts.
	 *
	 * @param scripts the collection of {@link CqlScript} instances to include in this dataset (must not be
	 * {@code null})
	 * @throws NullPointerException if {@code scripts} is {@code null}
	 */
	public DefaultCqlDataSet(Collection<? extends CqlScript> scripts) {
		Objects.requireNonNull(scripts, "Scripts must not be null");
		this.scripts = List.copyOf(scripts);
	}

	/**
	 * Gets the list of {@link CqlScript} instances contained within this dataset.
	 *
	 * <p>The returned list is unmodifiable to preserve the immutability of the dataset.</p>
	 *
	 * @return an unmodifiable list of {@link CqlScript} instances (never {@code null})
	 */
	@Override
	public List<CqlScript> getScripts() {
		return this.scripts;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		DefaultCqlDataSet that = (DefaultCqlDataSet) other;
		return this.scripts.equals(that.scripts);
	}

	@Override
	public int hashCode() {
		return this.scripts.hashCode();
	}

	@Override
	public String toString() {
		return "DefaultCqlDataSet{" + "scripts=" + this.scripts + '}';
	}

	/**
	 * A builder for constructing {@link DefaultCqlDataSet} instances.
	 *
	 * <p>The {@code Builder} class provides methods for adding various types of CQL scripts, resources, and raw
	 * statements to the dataset. Once all components are added, the {@link #build()} method can be called to create an
	 * immutable {@link DefaultCqlDataSet} instance.</p>
	 *
	 * <p><b>Example Usage:</b></p>
	 * <pre>{@code
	 * CqlDataSet dataSet = new DefaultCqlDataSet.Builder()
	 *     .addScript("CREATE TABLE test (...);")
	 *     .addStatements("INSERT INTO test VALUES (...);")
	 *     .addResource(new ClassPathResource("data.cql"))
	 *     .build();
	 * }</pre>
	 *
	 * @see DefaultCqlDataSet
	 */
	static class Builder implements CqlDataSet.Builder {

		private final List<CqlScript> scripts = new ArrayList<>();

		/**
		 * Adds a {@link CqlScript} instance to the builder.
		 *
		 * <p>If the provided {@link CqlScript} is an instance of {@link CqlDataSet}, its scripts are flattened
		 * and added to avoid nested datasets.</p>
		 *
		 * @param script the {@link CqlScript} to add (must not be {@code null})
		 * @return this builder
		 * @throws NullPointerException if {@code script} is {@code null}
		 */
		@Override
		public Builder addScript(CqlScript script) {
			Objects.requireNonNull(script, "Script must not be null");
			if (script instanceof CqlDataSet) {
				this.scripts.addAll(((CqlDataSet) script).getScripts());
			}
			else {
				this.scripts.add(script);
			}
			return this;
		}

		/**
		 * Adds a plain string CQL script to the builder.
		 *
		 * @param script the plain string CQL script to add (must not be {@code null})
		 * @return this builder
		 * @throws NullPointerException if {@code script} is {@code null}
		 */
		@Override
		public Builder addScript(String script) {
			Objects.requireNonNull(script, "Script must not be null");
			this.scripts.add(new StringCqlScript(script));
			return this;
		}

		/**
		 * Adds a {@link Resource} with the default charset to the builder.
		 *
		 * @param resource the {@link Resource} to add (must not be {@code null})
		 * @return this builder
		 * @throws NullPointerException if {@code resource} is {@code null}
		 */
		@Override
		public Builder addResource(Resource resource) {
			Objects.requireNonNull(resource, "Resource must not be null");
			addResource(resource, Charset.defaultCharset());
			return this;
		}

		/**
		 * Adds a {@link Resource} with the specified {@link Charset} to the builder.
		 *
		 * @param resource the {@link Resource} to add (must not be {@code null})
		 * @param charset the character encoding to use (must not be {@code null})
		 * @return this builder
		 * @throws NullPointerException if {@code resource} or {@code charset} is {@code null}
		 */
		@Override
		public Builder addResource(Resource resource, Charset charset) {
			Objects.requireNonNull(resource, "Resource must not be null");
			Objects.requireNonNull(charset, "Charset must not be null");
			this.scripts.add(CqlScript.ofResource(resource, charset));
			return this;
		}

		/**
		 * Adds one or more static CQL statements to the builder.
		 *
		 * @param statements the static CQL statements to add (must not be {@code null})
		 * @return this builder
		 * @throws NullPointerException if {@code statements} is {@code null}
		 */
		@Override
		public Builder addStatements(String... statements) {
			Objects.requireNonNull(statements, "Statements must not be null");
			this.scripts.add(CqlScript.ofStatements(statements));
			return this;
		}

		/**
		 * Adds a list of static CQL statements to the builder.
		 *
		 * @param statements the list of CQL statements to add (must not be {@code null})
		 * @return this builder
		 * @throws NullPointerException if {@code statements} is {@code null}
		 */
		@Override
		public Builder addStatements(List<? extends String> statements) {
			Objects.requireNonNull(statements, "Statements must not be null");
			this.scripts.add(CqlScript.ofStatements(statements));
			return this;
		}

		/**
		 * Builds and returns a new immutable {@link DefaultCqlDataSet}.
		 *
		 * @return a new {@link DefaultCqlDataSet}
		 */
		@Override
		public CqlDataSet build() {
			return new DefaultCqlDataSet(this.scripts);
		}

	}

}
