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

import java.util.Collection;

/**
 * CQL Script that abstracts from the actual type of underlying resource.
 *
 * @author Dmytro Nosan
 * @see UrlCqlScript
 * @see ClassPathCqlScript
 * @see StaticCqlScript
 * @see FileCqlScript
 * @see PathCqlScript
 * @see InputStreamCqlScript
 */
public interface CqlScript {

	/**
	 * Return a description for this script.
	 *
	 * @return Description of the script.
	 */
	String getName();

	/**
	 * Return a list of CQL statements.
	 *
	 * @return CQL statements.
	 */
	Collection<String> getStatements();
}
