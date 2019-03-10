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

import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link PathCqlScript}.
 *
 * @author Dmytro Nosan
 */
class PathCqlScriptTests {

	@Test
	void getStatements() throws URISyntaxException {
		PathCqlScript pathCqlScript = new PathCqlScript(Paths.get(
				getClass().getResource("/roles.cql").toURI()));
		assertThat(pathCqlScript.getStatements())
				.containsExactly("CREATE TABLE IF NOT EXISTS test.roles (id text PRIMARY KEY)");
	}

	@Test
	void helpers() throws Exception {
		assertThat(new PathCqlScript(Paths.get(getClass().getResource("/roles.cql").toURI())))
				.isEqualTo(new PathCqlScript(Paths.get(getClass().getResource("/roles.cql").toURI())));
		assertThat(
				new PathCqlScript(Paths.get(getClass().getResource("/roles.cql").toURI())).toString())
				.contains("roles.cql");
	}

	@Test
	void invalidResource() {
		assertThatThrownBy(() -> new PathCqlScript(Paths.get("hz.cql")).getStatements())
				.isInstanceOf(UncheckedIOException.class);
	}

}
