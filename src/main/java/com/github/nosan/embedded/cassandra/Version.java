/*
 * Copyright 2020-2025 the original author or authors.
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

import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A representation of a Cassandra version.
 *
 * @author Dmytro Nosan
 * @see #parse(String)
 * @since 4.0.0
 */
public final class Version implements Comparable<Version> {

	private static final Pattern VERSION_PATTERN = Pattern
			.compile("^([0-9]+)\\.([0-9]+)(\\.([0-9]+))?(-([^\\\\/]+))?$");

	private final String version;

	private final int major;

	private final int minor;

	private final Integer patch;

	private final String label;

	private Version(String version, int major, int minor, Integer patch, String label) {
		this.version = version;
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.label = label;
	}

	/**
	 * Creates a {@link Version} from a textual representation, such as {@code 4.0}.
	 *
	 * @param version the version to parse
	 * @return the parsed {@link Version}
	 * @throws IllegalArgumentException if the version is invalid
	 */
	public static Version parse(String version) throws IllegalArgumentException {
		if (version == null || version.trim().isEmpty()) {
			throw new IllegalArgumentException("Version must not be null or blank");
		}
		Matcher matcher = VERSION_PATTERN.matcher(version.trim());
		if (matcher.find()) {
			int major = Integer.parseInt(matcher.group(1));
			int minor = Integer.parseInt(matcher.group(2));
			Integer patch = (matcher.group(4) != null) ? Integer.parseInt(matcher.group(4)) : null;
			String label = matcher.group(6);
			return new Version(version.trim(), major, minor, patch, label);
		}
		throw new IllegalArgumentException("Version '" + version + "' is invalid");
	}

	/**
	 * Retrieves the major version.
	 *
	 * @return the major version
	 */
	public int getMajor() {
		return this.major;
	}

	/**
	 * Retrieves the minor version.
	 *
	 * @return the minor version
	 */
	public int getMinor() {
		return this.minor;
	}

	/**
	 * Retrieves the patch version, if available.
	 *
	 * @return an {@link OptionalInt} containing the patch version, or an empty {@code OptionalInt} if not present
	 */
	public OptionalInt getPatch() {
		Integer patch = this.patch;
		return (patch != null) ? OptionalInt.of(patch) : OptionalInt.empty();
	}

	/**
	 * Retrieves the pre-release label, if available.
	 *
	 * @return an {@link Optional} containing the label, or an empty {@code Optional} if not present
	 */
	public Optional<String> getLabel() {
		return Optional.ofNullable(this.label);
	}

	@Override
	public boolean equals(Object other) {
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
		int majorCmp = Integer.compare(getMajor(), other.getMajor());
		if (majorCmp == 0) {
			int minorCmp = Integer.compare(getMinor(), other.getMinor());
			if (minorCmp == 0) {
				int patchCmp = Integer.compare(getPatch().orElse(0), other.getPatch().orElse(0));
				if (patchCmp == 0) {
					if (this.label == null && other.label == null) {
						return 0;
					}
					if (this.label == null) {
						return 1;
					}
					if (other.label == null) {
						return -1;
					}
					return Math.max(Math.min(this.label.compareTo(other.label), 1), -1);
				}
				return patchCmp;
			}
			return minorCmp;
		}
		return majorCmp;
	}

	@Override
	public String toString() {
		return this.version;
	}

}
