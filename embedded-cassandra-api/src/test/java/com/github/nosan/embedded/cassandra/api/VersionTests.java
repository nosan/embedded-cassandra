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

package com.github.nosan.embedded.cassandra.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link Version}.
 *
 * @author Dmytro Nosan
 */
class VersionTests {

	@Test
	void shouldParseMajorMinorPatch() {
		Version version = Version.of("3.11.6");
		assertThat(version).isEqualTo(of(3, 11, 6));
		assertThat(version).isEqualTo(version);
		assertThat(version).isEqualByComparingTo(of(3, 11, 6));
		assertThat(version).isNotEqualByComparingTo(of(3, 12));
		assertThat(version).isNotEqualByComparingTo(of(3, 11, 2));
		assertThat(version.getMajor()).isEqualTo(3);
		assertThat(version.getMinor()).isEqualTo(11);
		assertThat(version.getPatch()).hasValue(6);
		assertThat(version.toString()).isEqualTo("3.11.6");
	}

	@Test
	void shouldParseMajorMinor() {
		Version version = Version.of("3.11");
		assertThat(version).isEqualTo(of(3, 11));
		assertThat(version).isEqualTo(version);
		assertThat(version).isEqualByComparingTo(of(3, 11));
		assertThat(version).isNotEqualByComparingTo(of(3, 12));
		assertThat(version).isNotEqualByComparingTo(of(3, 11, 2));
		assertThat(version).isNotEqualByComparingTo(of(3, 12, 2));
		assertThat(version.getMajor()).isEqualTo(3);
		assertThat(version.getMinor()).isEqualTo(11);
		assertThat(version.getPatch()).isEmpty();
		assertThat(version.toString()).isEqualTo("3.11");
	}

	@Test
	void shouldParseBetaVersion() {
		String v = "1.1.0-beta1";
		Version version = Version.of(v);
		assertThat(version).isEqualTo(Version.of(v));
		assertThat(version).isEqualTo(version);
		assertThat(version).isEqualByComparingTo(Version.of(v));
		assertThat(version).isNotEqualByComparingTo(of(1, 1, 0));
		assertThat(version).isNotEqualByComparingTo(of(1, 1, 1));
		assertThat(version).isNotEqualByComparingTo(of(1, 1));
		assertThat(version.getMajor()).isEqualTo(1);
		assertThat(version.getMinor()).isEqualTo(1);
		assertThat(version.getPatch()).hasValue(0);
		assertThat(version.toString()).isEqualTo(v);
	}

	@Test
	void shouldNotParseInvalid() {
		assertThatThrownBy(() -> Version.of("3.")).hasStackTraceContaining("is invalid").isInstanceOf(
				IllegalArgumentException.class);
	}

	private static Version of(int major, int minor, int patch) {
		return Version.of(String.format("%d.%d.%d", major, minor, patch));
	}

	private static Version of(int major, int minor) {
		return Version.of(String.format("%d.%d", major, minor));
	}

}
