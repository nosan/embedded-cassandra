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

package com.github.nosan.embedded.cassandra.api.cql;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.nosan.embedded.cassandra.commons.io.ClassPathResource;
import com.github.nosan.embedded.cassandra.commons.io.Resource;

/**
 * {@link CqlDataSet} interface that contains a list of {@link CqlScript}(s).
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
@FunctionalInterface
public interface CqlDataSet extends CqlScript {

	/**
	 * Constructs a new {@link CqlDataSet} with no {@link CqlScript}(s).
	 *
	 * @return a new empty {@link CqlDataSet}
	 */
	static CqlDataSet empty() {
		return DefaultCqlDataSet.EMPTY;
	}

	/**
	 * Constructs a new {@link CqlDataSet} with the specified CQL ({@code string}) scripts.
	 *
	 * @param scripts the CQL scripts
	 * @return a new {@link CqlDataSet}
	 */
	static CqlDataSet ofStrings(String... scripts) {
		Objects.requireNonNull(scripts, "'scripts' must not be null");
		if (scripts.length == 0) {
			return empty();
		}
		if (scripts.length == 1) {
			return new DefaultCqlDataSet(Collections.singletonList(CqlScript.ofString(scripts[0])));
		}
		return new DefaultCqlDataSet(Arrays.stream(scripts).map(CqlScript::ofString).collect(Collectors.toList()));
	}

	/**
	 * Constructs a new {@link CqlDataSet} with the specified {@link Resource Resources}.
	 *
	 * @param resources the resources
	 * @return a new {@link CqlDataSet}
	 */
	static CqlDataSet ofResources(Resource... resources) {
		return ofResources(StandardCharsets.UTF_8, resources);
	}

	/**
	 * Constructs a new {@link CqlDataSet} with the specified {@link Resource Resources} and {@link Charset}.
	 *
	 * @param resources the resources
	 * @param charset the charset to use
	 * @return a new {@link CqlDataSet}
	 */
	static CqlDataSet ofResources(Charset charset, Resource... resources) {
		Objects.requireNonNull(charset, "'charset' must not be null");
		Objects.requireNonNull(resources, "'resources' must not be null");
		if (resources.length == 0) {
			return empty();
		}
		if (resources.length == 1) {
			return new DefaultCqlDataSet(Collections.singletonList(CqlScript.ofResource(charset, resources[0])));
		}
		return new DefaultCqlDataSet(Arrays.stream(resources).map(resource -> CqlScript.ofResource(charset, resource))
				.collect(Collectors.toList()));
	}

	/**
	 * Constructs a new {@link CqlDataSet} with the specified resource names.
	 *
	 * @param names the resource names
	 * @return a new {@link CqlDataSet}
	 */
	static CqlDataSet ofClasspaths(String... names) {
		return ofClasspaths(StandardCharsets.UTF_8, names);
	}

	/**
	 * Constructs a new {@link CqlDataSet} with the specified resource names and {@link Charset}.
	 *
	 * @param names the resource names
	 * @param charset the charset to use
	 * @return a new {@link CqlDataSet}
	 */
	static CqlDataSet ofClasspaths(Charset charset, String... names) {
		Objects.requireNonNull(charset, "'charset' must not be null");
		Objects.requireNonNull(names, "'names' must not be null");
		return ofResources(charset, Arrays.stream(names).map(ClassPathResource::new).toArray(Resource[]::new));
	}

	/**
	 * Constructs a new {@link CqlDataSet} with the specified {@link CqlScript}(s).
	 *
	 * @param scripts the list of the {@link CqlScript}(s)
	 * @return a new {@link CqlDataSet}
	 */
	static CqlDataSet ofScripts(CqlScript... scripts) {
		Objects.requireNonNull(scripts, "'scripts' must not be null");
		if (scripts.length == 0) {
			return empty();
		}
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
	 * Appends all of the scripts in the specified {@link CqlDataSet} to the end of this {@link CqlDataSet} and returns
	 * the new {@link CqlDataSet} that contains both of them.
	 *
	 * @param dataSet {@link CqlDataSet} containing scripts to be added to this {@link CqlDataSet}
	 * @return a new {@link CqlDataSet}
	 */
	default CqlDataSet add(CqlDataSet dataSet) {
		Objects.requireNonNull(dataSet, "'dataSet' must not be null");
		return ofScripts(Stream.concat(getScripts().stream(), dataSet.getScripts().stream()).toArray(CqlScript[]::new));
	}

	/**
	 * Performs the given {@code callback} for each script of the {@link CqlDataSet}.
	 *
	 * @param callback The action to be performed for each script
	 */
	default void forEachScript(Consumer<? super CqlScript> callback) {
		Objects.requireNonNull(callback, "'callback' must not be null");
		getScripts().forEach(callback);
	}

	/**
	 * Returns {@code CQL} statements.
	 *
	 * @return {@code CQL} statements
	 */
	@Override
	default List<String> getStatements() {
		List<String> statements = new ArrayList<>();
		getScripts().forEach(script -> statements.addAll(script.getStatements()));
		return Collections.unmodifiableList(statements);
	}

	/**
	 * Returns {@code CQL} scripts.
	 *
	 * @return {@code CQL} scripts
	 */
	List<? extends CqlScript> getScripts();

}
