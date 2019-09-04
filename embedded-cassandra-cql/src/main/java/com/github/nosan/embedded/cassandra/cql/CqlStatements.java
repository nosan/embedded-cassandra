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

package com.github.nosan.embedded.cassandra.cql;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.github.nosan.embedded.cassandra.commons.io.Resource;

/**
 * {@link CqlStatements} interface that contains a list of {@code CQL} statements.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public interface CqlStatements extends Iterable<String> {

	/**
	 * Constructs a new {@link CqlStatements} with the specified CQL scripts.
	 *
	 * @param scripts the CQL scripts
	 * @return a new {@link CqlStatements}
	 */
	static CqlStatements forScripts(String... scripts) {
		Objects.requireNonNull(scripts, "'scripts' must not be null");
		return new ScriptCqlStatements(scripts);
	}

	/**
	 * Constructs a new {@link CqlStatements} with the specified {@link Resource Resources}.
	 *
	 * @param resources the resources
	 * @return a new {@link CqlStatements}
	 */
	static CqlStatements forResources(Resource... resources) {
		return forResources(StandardCharsets.UTF_8, resources);
	}

	/**
	 * Constructs a new {@link CqlStatements} with the specified {@link Resource Resources} and {@link Charset}.
	 *
	 * @param resources the resources
	 * @param charset the charset to use
	 * @return a new {@link CqlStatements}
	 */
	static CqlStatements forResources(Charset charset, Resource... resources) {
		Objects.requireNonNull(resources, "'resources' must not be null");
		Objects.requireNonNull(charset, "'charset' must not be null");
		return new ResourceCqlStatements(charset, resources);
	}

	/**
	 * Creates a {@link Stream} for CQL statements.
	 *
	 * @return a new {@link Stream}
	 */
	default Stream<String> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

}
