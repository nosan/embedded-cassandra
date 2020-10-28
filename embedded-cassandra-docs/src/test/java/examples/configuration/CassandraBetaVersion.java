/*
 * Copyright 2018-2020 the original author or authors.
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

package examples.configuration;

import com.github.nosan.embedded.cassandra.EmbeddedCassandraFactory;
import com.github.nosan.embedded.cassandra.artifact.Artifact;

class CassandraBetaVersion {

	void source() {
		// tag::source[]
		EmbeddedCassandraFactory cassandraFactory = new EmbeddedCassandraFactory();
		cassandraFactory.setArtifact(Artifact.ofVersion("4.0-beta2"));
		// end::source[]
	}

}