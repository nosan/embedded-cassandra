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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.github.nosan.embedded.cassandra.commons.Resource;

/**
 * Default implementation of {@link CqlDataSet}.
 *
 * @author Dmytro Nosan
 * @since 4.0.1
 */
public class DefaultCqlDataSet implements CqlDataSet {

	private final List<CqlScript> scripts;

	/**
	 * Creates a new {@link DefaultCqlDataSet} with the specified CQL scripts.
	 *
	 * @param scripts the CQL scripts
	 */
	public DefaultCqlDataSet(Collection<? extends CqlScript> scripts) {
		Objects.requireNonNull(scripts, "Scripts must not be null");
		this.scripts = List.copyOf(scripts);
	}

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

	static class Builder implements CqlDataSet.Builder {

		private final List<CqlScript> scripts = new ArrayList<>();

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

		@Override
		public Builder addScript(String script) {
			Objects.requireNonNull(script, "Script must not be null");
			this.scripts.add(new StringCqlScript(script));
			return this;
		}

		@Override
		public Builder addResource(Resource resource) {
			Objects.requireNonNull(resource, "Resource must not be null");
			addResource(resource, StandardCharsets.UTF_8);
			return this;
		}

		@Override
		public Builder addResource(Resource resource, Charset charset) {
			Objects.requireNonNull(resource, "Resource must not be null");
			Objects.requireNonNull(charset, "Charset must not be null");
			this.scripts.add(CqlScript.ofResource(resource, charset));
			return this;
		}

		@Override
		public Builder addStatements(String... statements) {
			Objects.requireNonNull(statements, "Statements must not be null");
			this.scripts.add(CqlScript.ofStatements(statements));
			return this;
		}

		@Override
		public Builder addStatements(List<? extends String> statements) {
			Objects.requireNonNull(statements, "Statements must not be null");
			this.scripts.add(CqlScript.ofStatements(statements));
			return this;
		}

		@Override
		public CqlDataSet build() {
			return new DefaultCqlDataSet(this.scripts);
		}

	}

}
