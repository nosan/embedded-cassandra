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

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;

/**
 * Utility class to construct a {@link CqlScript}.
 *
 * @author Dmytro Nosan
 * @since 1.4.2
 */
abstract class CqlScriptFactory {

	private static final CqlScripts EMPTY = new CqlScripts(Collections.emptyList());

	static <T> CqlScript create(T[] locations, Function<T, CqlScript> mapper) {
		Objects.requireNonNull(locations, "Locations must not be null");
		if (locations.length == 0) {
			return EMPTY;
		}
		if (locations.length == 1) {
			return mapper.apply(locations[0]);
		}
		return new CqlScripts(Arrays.stream(locations).map(mapper).toArray(CqlScript[]::new));
	}

}
