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

package com.github.nosan.embedded.cassandra;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Version}.
 *
 * @author Dmytro Nosan
 */
public class VersionTests {

	@Rule
	public final ExpectedException throwable = ExpectedException.none();

	@Test
	public void shouldParseMajorMinorPatch() {
		Version version = Version.parse("3.11.3");
		assertThat(version)
				.isEqualTo(new Version(3, 11, 3));
		assertThat(version).isEqualByComparingTo(new Version(3, 11, 3));
		assertThat(version).isNotEqualByComparingTo(new Version(3, 11, 2));
		assertThat(version.getMajor()).isEqualTo(3);
		assertThat(version.getMinor()).isEqualTo(11);
		assertThat(version.getPatch()).isEqualTo(3);
		assertThat(version.toString()).isEqualTo("3.11.3");
	}

	@Test
	public void shouldParseMajorMinor() {
		Version version = Version.parse("3.11");
		assertThat(version).isEqualTo(new Version(3, 11));
		assertThat(version).isEqualByComparingTo(new Version(3, 11));
		assertThat(version).isNotEqualByComparingTo(new Version(3, 11, 2));
		assertThat(version.getMajor()).isEqualTo(3);
		assertThat(version.getMinor()).isEqualTo(11);
		assertThat(version.getPatch()).isEqualTo(-1);
		assertThat(version.toString()).isEqualTo("3.11");
	}

	@Test
	public void shouldParseMajor() {
		Version version = Version.parse("3");
		assertThat(version)
				.isEqualTo(new Version(3));
		assertThat(version).isEqualByComparingTo(new Version(3));
		assertThat(version).isNotEqualByComparingTo(new Version(3, 11, 2));
		assertThat(version.getMajor()).isEqualTo(3);
		assertThat(version.getMinor()).isEqualTo(-1);
		assertThat(version.getPatch()).isEqualTo(-1);
		assertThat(version.toString()).isEqualTo("3");
	}

	@Test
	public void shouldNotParse() {
		this.throwable.expect(IllegalArgumentException.class);
		this.throwable.expectMessage("Expected format is ");
		Version.parse("1.q");

	}
}
