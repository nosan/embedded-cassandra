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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.nosan.embedded.cassandra.commons.io.Resource;

/**
 * {@link CqlScript} interface that contains a list of {@code CQL} statements.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public interface CqlScript {

	/**
	 * Constructs a new {@link CqlScript} with the specified CQL ({@code string}) scripts.
	 *
	 * @param scripts the CQL scripts
	 * @return a new {@link CqlScript}
	 */
	static CqlScript ofStrings(String... scripts) {
		Objects.requireNonNull(scripts, "'scripts' must not be null");
		if (scripts.length == 0) {
			return EmptyCqlScript.INSTANCE;
		}
		if (scripts.length == 1) {
			return new StringCqlScript(scripts[0]);
		}
		return new CqlScripts(Arrays.stream(scripts).map(StringCqlScript::new).collect(Collectors.toList()));
	}

	/**
	 * Constructs a new {@link CqlScript} with the specified {@link Resource Resources}.
	 *
	 * @param resources the resources
	 * @return a new {@link CqlScript}
	 */
	static CqlScript ofResources(Resource... resources) {
		return ofResources(StandardCharsets.UTF_8, resources);
	}

	/**
	 * Constructs a new {@link CqlScript} with the specified {@link Resource Resources} and {@link Charset}.
	 *
	 * @param resources the resources
	 * @param charset the charset to use
	 * @return a new {@link CqlScript}
	 */
	static CqlScript ofResources(Charset charset, Resource... resources) {
		Objects.requireNonNull(resources, "'resources' must not be null");
		Objects.requireNonNull(charset, "'charset' must not be null");
		if (resources.length == 0) {
			return EmptyCqlScript.INSTANCE;
		}
		if (resources.length == 1) {
			return new ResourceCqlScript(charset, resources[0]);
		}
		List<CqlScript> scripts = new ArrayList<>();
		for (Resource resource : resources) {
			scripts.add(new ResourceCqlScript(charset, resource));
		}
		return new CqlScripts(scripts);
	}

	/**
	 * Performs the given {@code callback} for each statement of the {@link CqlScript}.
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
