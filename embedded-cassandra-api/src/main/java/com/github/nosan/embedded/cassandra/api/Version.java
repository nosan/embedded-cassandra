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

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.nosan.embedded.cassandra.annotations.Nullable;

/**
 * A representation of a version string for a {@link Cassandra}.
 *
 * @author Dmytro Nosan
 * @see #of(String)
 * @since 3.0.0
 */
public final class Version implements Comparable<Version> {

	private static final Pattern VERSION_PATTERN = Pattern.compile("^([0-9]+)(\\.([0-9]+))(\\.([0-9]+))?.*$");

	private final String version;

	private final int major;

	private final int minor;

	@Nullable
	private final Integer patch;

	private Version(int major, int minor, @Nullable Integer patch, String version) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.version = version;
	}

	/**
	 * Obtain a {@link Version} from a text string such as {@code 4.0.0}.
	 *
	 * @param version the version to parse
	 * @return the parsed {@link Version}
	 * @throws IllegalArgumentException version is invalid
	 * @throws NullPointerException version is null
	 */
	public static Version of(String version) throws IllegalArgumentException, NullPointerException {
		Objects.requireNonNull(version, "'version' must not be null");
		Matcher matcher = VERSION_PATTERN.matcher(version.trim());
		if (matcher.find()) {
			int major = Integer.parseInt(matcher.group(1));
			int minor = Integer.parseInt(matcher.group(3));
			Integer patch = (matcher.group(5) != null) ? Integer.parseInt(matcher.group(5)) : null;
			return new Version(major, minor, patch, matcher.group(0));
		}
		throw new IllegalArgumentException("Version '" + version + "' is invalid");
	}

	/**
	 * Returns a major value.
	 *
	 * @return the major
	 */
	public int getMajor() {
		return this.major;
	}

	/**
	 * Returns a minor value.
	 *
	 * @return the minor
	 */
	public int getMinor() {
		return this.minor;
	}

	/**
	 * Returns a patch value, or {@link Optional#empty()}.
	 *
	 * @return the patch or {@code empty}
	 */
	public Optional<Integer> getPatch() {
		return Optional.ofNullable(this.patch);
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		Version v = (Version) other;
		return this.version.equals(v.version);
	}

	@Override
	public int hashCode() {
		return this.version.hashCode();
	}

	@Override
	public int compareTo(Version other) {
		Objects.requireNonNull(other, "'other' must not be null");
		int major = Integer.compare(getMajor(), other.getMajor());
		if (major == 0) {
			int min = Integer.compare(getMinor(), other.getMinor());
			if (min == 0) {
				int patch = Integer.compare(getPatch().orElse(-1), other.getPatch().orElse(-1));
				if (patch == 0) {
					return this.version.compareTo(other.version);
				}
				return patch;
			}
			return min;
		}
		return major;
	}

	@Override
	public String toString() {
		return this.version;
	}

}
