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

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * A simple class to represent the version.
 *
 * @author Dmytro Nosan
 * @see #parse(String)
 * @since 1.0.0
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
	 * Parses a {@code version}.
	 *
	 * @param version a version
	 * @return a {@link Version}
	 */
	public static Version parse(String version) {
		Objects.requireNonNull(version, "Version must not be null");
		Matcher matcher = VERSION_PATTERN.matcher(version.trim());
		if (matcher.find()) {
			int major = Integer.parseInt(matcher.group(1));
			int minor = Integer.parseInt(matcher.group(3));
			Integer patch = null;
			String patchGroup = matcher.group(5);
			if (StringUtils.hasText(patchGroup)) {
				patch = Integer.parseInt(patchGroup);
			}
			return new Version(major, minor, patch, matcher.group(0));
		}
		throw new IllegalArgumentException(String.format("Version '%s' is invalid.", version));
	}

	/**
	 * Returns a major value.
	 *
	 * @return The value of the {@code major} attribute
	 */
	public int getMajor() {
		return this.major;
	}

	/**
	 * Returns a minor value.
	 *
	 * @return The value of the {@code minor} attribute
	 */
	public int getMinor() {
		return this.minor;
	}

	/**
	 * Returns a patch value, or empty.
	 *
	 * @return The value of the {@code patch} attribute, or empty
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
		return Objects.hash(this.version);
	}

	@Override
	public String toString() {
		return this.version;
	}

	@Override
	public int compareTo(Version other) {
		Objects.requireNonNull(other, "Version must not be null");
		int majorCmp = Integer.compare(getMajor(), other.getMajor());
		if (majorCmp == 0) {
			int minCmp = Integer.compare(getMinor(), other.getMinor());
			if (minCmp == 0) {
				int patchCmp = Integer.compare(getPatch().orElse(-1), other.getPatch().orElse(-1));
				if (patchCmp == 0) {
					return this.version.compareTo(other.version);
				}
				return patchCmp;
			}
			return minCmp;
		}
		return majorCmp;
	}

}
