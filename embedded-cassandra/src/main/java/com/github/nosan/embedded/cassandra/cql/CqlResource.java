/*
 * Copyright 2012-2018 the original author or authors.
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

import java.io.UncheckedIOException;
import java.util.Collection;

/**
 * CQL resource that abstracts from the actual type of underlying resource.
 *
 * @author Dmytro Nosan
 * @see UrlCqlResource
 * @see ClassPathCqlResource
 * @see StaticCqlResource
 * @see FileCqlResource
 * @see PathCqlResource
 * @see InputStreamCqlResource
 */
public interface CqlResource {

	/**
	 * Return a description for this resource.
	 *
	 * @return Description of the resource.
	 */
	String getName();

	/**
	 * Return a list of CQL statements.
	 *
	 * @return CQL statements.
	 * @throws UncheckedIOException if an I/O error occurs
	 */
	Collection<String> getStatements();
}
