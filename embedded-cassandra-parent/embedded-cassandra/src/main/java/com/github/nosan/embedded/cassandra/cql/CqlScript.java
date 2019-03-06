/*
 * Copyright 2018-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nosan.embedded.cassandra.cql;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;

import org.apiguardian.api.API;

import com.github.nosan.embedded.cassandra.lang.Nullable;

/**
 * CQL Script that abstracts from the actual type of underlying source.
 *
 * @author Dmytro Nosan
 * @see AbstractCqlScript
 * @see AbstractCqlResourceScript
 * @see CqlScripts
 * @see UrlCqlScript
 * @see ClassPathCqlScript
 * @see StaticCqlScript
 * @see FileCqlScript
 * @see PathCqlScript
 * @see InputStreamCqlScript
 * @see ClassPathGlobCqlScript
 * @since 1.0.0
 */
@API(since = "1.0.0", status = API.Status.STABLE)
@FunctionalInterface
public interface CqlScript {

	/**
	 * Return CQL Statements.
	 *
	 * @return CQL statements.
	 */
	Collection<String> getStatements();

	/**
	 * Factory method to create {@link CqlScripts} based on classpath {@code 'glob'} patterns.
	 *
	 * @param patterns classpath glob patterns
	 * @return CQL script
	 * @see ClassPathGlobCqlScript
	 * @since 1.2.6
	 */
	static CqlScript classpathGlobs(@Nullable String... patterns) {
		return CqlScriptConstructor.create(patterns, ClassPathGlobCqlScript::new);
	}

	/**
	 * Factory method to create {@link CqlScripts} based on classpath locations.
	 *
	 * @param locations classpath locations
	 * @return CQL script
	 * @see ClassPathCqlScript
	 */
	static CqlScript classpath(@Nullable String... locations) {
		return CqlScriptConstructor.create(locations, ClassPathCqlScript::new);
	}

	/**
	 * Factory method to create {@link CqlScripts} based on classpath locations.
	 *
	 * @param locations classpath locations
	 * @param contextClass the class to load the resource with.
	 * @return CQL script
	 * @see ClassPathCqlScript
	 */
	static CqlScript classpath(@Nullable Class<?> contextClass, @Nullable String... locations) {
		return CqlScriptConstructor.create(locations, element -> new ClassPathCqlScript(element, contextClass));
	}

	/**
	 * Factory method to create {@link CqlScripts} based on urls.
	 *
	 * @param locations URL locations
	 * @return CQL script
	 * @see UrlCqlScript
	 */
	static CqlScript urls(@Nullable URL... locations) {
		return CqlScriptConstructor.create(locations, UrlCqlScript::new);
	}

	/**
	 * Factory method to create {@link CqlScripts} based on files.
	 *
	 * @param locations File locations
	 * @return CQL script
	 * @see FileCqlScript
	 */
	static CqlScript files(@Nullable File... locations) {
		return CqlScriptConstructor.create(locations, FileCqlScript::new);
	}

	/**
	 * Factory method to create {@link CqlScripts} based on paths.
	 *
	 * @param locations Path locations
	 * @return CQL script
	 * @see PathCqlScript
	 */
	static CqlScript paths(@Nullable Path... locations) {
		return CqlScriptConstructor.create(locations, PathCqlScript::new);
	}

	/**
	 * Factory method to create {@link StaticCqlScript} based on statements.
	 *
	 * @param statements CQL statements
	 * @return CQL script
	 * @see StaticCqlScript
	 */
	static CqlScript statements(@Nullable String... statements) {
		return new StaticCqlScript(statements);
	}

}
