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

package com.github.nosan.embedded.cassandra;

/**
 * List of the cassandra versions.
 *
 * @author Dmytro Nosan
 */
public enum CassandraVersion {

	/**
	 * The version 3.11.2.
	 */
	LATEST("3.11.2");

	private final String version;

	CassandraVersion(String version) {
		this.version = version;
	}

	/**
	 *
	 * Retrieves the cassandra version.
	 * @return Cassandra version.
	 */
	public String getVersion() {
		return this.version;
	}

}
