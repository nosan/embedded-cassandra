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

package com.github.nosan.embedded.cassandra.artifact;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.github.nosan.embedded.cassandra.api.Version;

/**
 * Factory interface that can be used to create a list of URLs to the Apache Cassandra repositories.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
@FunctionalInterface
public interface UrlFactory {

	/**
	 * Returns list of URLs for the specified version.
	 *
	 * @param version Cassandra's version
	 * @return URLs to  to the Apache Cassandra repositories
	 * @throws MalformedURLException if {@code URL} cannot be built
	 */
	List<URL> create(Version version) throws MalformedURLException;

}
