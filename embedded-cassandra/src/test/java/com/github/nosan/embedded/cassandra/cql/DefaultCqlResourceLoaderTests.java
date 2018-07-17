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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultCqlResourceLoader}.
 *
 * @author Dmytro Nosan
 */
public class DefaultCqlResourceLoaderTests {

	private final DefaultCqlResourceLoader loader = new DefaultCqlResourceLoader();

	@Test
	public void loadClassPathResource() {
		CqlResource cqlResource = this.loader.load("test.cql");
		assertThat(cqlResource).isInstanceOf(ClassPathCqlResource.class);
		assertStatements(cqlResource);
	}


	@Test
	public void loadClassPathResourceWithLeadingSlash() {
		CqlResource cqlResource = this.loader.load("/test.cql");
		assertThat(cqlResource).isInstanceOf(ClassPathCqlResource.class);
		assertStatements(cqlResource);
	}

	@Test
	public void loadUrlResource() {
		CqlResource cqlResource = this.loader.load(ClassLoader.getSystemResource("test.cql").toString());
		assertThat(cqlResource).isInstanceOf(UrlCqlResource.class);
		assertStatements(cqlResource);
	}


	private void assertStatements(CqlResource cqlResource) {
		assertThat(cqlResource.getStatements())
				.containsExactly("CREATE TABLE IF NOT EXISTS test.roles ( id text PRIMARY KEY )");
	}

}
