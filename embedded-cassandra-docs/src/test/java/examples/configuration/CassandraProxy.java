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

import java.net.InetSocketAddress;
import java.net.Proxy;

import com.github.nosan.embedded.cassandra.EmbeddedCassandraFactory;
import com.github.nosan.embedded.cassandra.api.Version;
import com.github.nosan.embedded.cassandra.artifact.RemoteArtifact;

class CassandraProxy {

	void source() {
		// tag::source[]
		EmbeddedCassandraFactory cassandraFactory = new EmbeddedCassandraFactory();
		RemoteArtifact artifact = new RemoteArtifact(Version.of("3.11.8"));
		artifact.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("my.proxy", 80)));
		cassandraFactory.setArtifact(artifact);
		// end::source[]
	}

}
