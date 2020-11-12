/*
 * Copyright 2020 the original author or authors.
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
	 * Obtain a {@link Version} from a text string such as {@code 4.0}.
	 *
	 * @param version the version to parse
	 * @return the parsed {@link Version}
	 * @throws IllegalArgumentException version is invalid
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
	 * Gets a major value.
	 *
	 * @return the major
	 */
	public int getMajor() {
		return this.major;
	}

	/**
	 * Gets a minor value.
	 *
	 * @return the minor
	 */
	public int getMinor() {
		return this.minor;
	}

	/**
	 * Gets a patch value.
	 *
	 * @return the patch
	 */
	public OptionalInt getPatch() {
		Integer patch = this.patch;
		return (patch != null) ? OptionalInt.of(patch) : OptionalInt.empty();
	}

	/**
	 * Gets a pre-release label.
	 *
	 * @return the label
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
			int minCmp = Integer.compare(getMinor(), other.getMinor());
			if (minCmp == 0) {
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
			return minCmp;
		}
		return majorCmp;
	}

	@Override
	public String toString() {
		return this.version;
	}

}
