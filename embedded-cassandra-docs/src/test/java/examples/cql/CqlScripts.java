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

package examples.cql;

import java.util.List;

import com.github.nosan.embedded.cassandra.commons.io.ClassPathResource;
import com.github.nosan.embedded.cassandra.cql.CqlScript;

public class CqlScripts {

	void cqlResource() {
		// tag::source[]
		CqlScript script = CqlScript.ofResource(new ClassPathResource("schema.cql"));
		List<String> statements = script.getStatements();
		// end::source[]
	}

}
