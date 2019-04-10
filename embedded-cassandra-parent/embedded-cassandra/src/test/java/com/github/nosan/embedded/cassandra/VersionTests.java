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

package com.github.nosan.embedded.cassandra;

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
		Version version = Version.parse("3.11.3");
		assertThat(version).isEqualTo(getVersion(3, 11, 3));
		assertThat(version).isEqualByComparingTo(getVersion(3, 11, 3));
		assertThat(version).isNotEqualByComparingTo(getVersion(3, 12));
		assertThat(version).isNotEqualByComparingTo(getVersion(3, 11, 2));
		assertThat(version.getMajor()).isEqualTo(3);
		assertThat(version.getMinor()).isEqualTo(11);
		assertThat(version.getPatch()).hasValue(3);
		assertThat(version.toString()).isEqualTo("3.11.3");
	}

	@Test
	void shouldParseMajorMinor() {
		Version version = Version.parse("3.11");
		assertThat(version).isEqualTo(getVersion(3, 11));
		assertThat(version).isEqualByComparingTo(getVersion(3, 11));
		assertThat(version).isNotEqualByComparingTo(getVersion(3, 12));
		assertThat(version).isNotEqualByComparingTo(getVersion(3, 11, 2));
		assertThat(version).isNotEqualByComparingTo(getVersion(3, 12, 2));
		assertThat(version.getMajor()).isEqualTo(3);
		assertThat(version.getMinor()).isEqualTo(11);
		assertThat(version.getPatch()).isEmpty();
		assertThat(version.toString()).isEqualTo("3.11");
	}

	@Test
	void shouldNotParseInvalidFormat() {
		assertThatThrownBy(() -> Version.parse("3.")).hasStackTraceContaining("is invalid")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldParseBetaVersion() {
		String v = "1.1.0-beta1";
		Version version = Version.parse(v);
		assertThat(version).isEqualTo(Version.parse(v));
		assertThat(version).isEqualByComparingTo(Version.parse(v));
		assertThat(version).isNotEqualByComparingTo(getVersion(1, 1, 0));
		assertThat(version).isNotEqualByComparingTo(getVersion(1, 1, 1));
		assertThat(version).isNotEqualByComparingTo(getVersion(1, 1));
		assertThat(version.getMajor()).isEqualTo(1);
		assertThat(version.getMinor()).isEqualTo(1);
		assertThat(version.getPatch()).hasValue(0);
		assertThat(version.toString()).isEqualTo(v);
	}

	private static Version getVersion(int major, int minor, int patch) {
		return Version.parse(String.format("%d.%d.%d", major, minor, patch));
	}

	private static Version getVersion(int major, int minor) {
		return Version.parse(String.format("%d.%d", major, minor));
	}

}
