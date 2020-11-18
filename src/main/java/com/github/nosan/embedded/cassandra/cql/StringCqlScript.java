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

import java.util.Objects;

/**
 * {@link CqlScript} implementation for string scripts.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public class StringCqlScript extends AbstractCqlScript {

	private final String script;

	/**
	 * Creates {@link StringCqlScript} with the provided CQL script.
	 *
	 * @param script CQL script that contains CQL statements
	 */
	public StringCqlScript(String script) {
		Objects.requireNonNull(script, "Script must not be null");
		this.script = script;
	}

	@Override
	protected String getScript() {
		return this.script;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		StringCqlScript that = (StringCqlScript) other;
		return this.script.equals(that.script);
	}

	@Override
	public int hashCode() {
		return this.script.hashCode();
	}

	@Override
	public String toString() {
		return "StringCqlScript{" + "script='" + this.script + '\'' + '}';
	}

}
