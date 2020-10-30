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

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * {@link CqlScript} interface that contains a list of {@code CQL} statements.
 *
 * @author Dmytro Nosan
 * @see AbstractCqlScript
 * @see ResourceCqlScript
 * @see StringCqlScript
 * @since 4.0.0
 */
@FunctionalInterface
public interface CqlScript {

	/**
	 * Performs the given {@code callback} for each statement of the {@link CqlScript}.
	 *
	 * @param callback The action to be performed for each statement
	 */
	default void forEachStatement(Consumer<? super String> callback) {
		Objects.requireNonNull(callback, "Callback must not be null");
		getStatements().forEach(callback);
	}

	/**
	 * Returns {@code CQL} statements.
	 *
	 * @return {@code CQL} statements
	 */
	List<String> getStatements();

}
