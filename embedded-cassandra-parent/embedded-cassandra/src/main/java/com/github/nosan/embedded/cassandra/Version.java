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

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnegative;

import org.apiguardian.api.API;

import com.github.nosan.embedded.cassandra.util.StringUtils;
import com.github.nosan.embedded.cassandra.util.annotation.Nullable;

/**
 * Simple class to represent {@link Cassandra} version.
 *
 * @author Dmytro Nosan
 * @see #parse(String)
 * @since 1.0.0
 */
@API(since = "1.0.0", status = API.Status.STABLE)
public final class Version implements Comparable<Version> {

	private static final Pattern VERSION_PATTERN = Pattern.compile("^([0-9]+)(\\.([0-9]+))?(\\.([0-9]+))?(.*)$");

	private final String version;

	private final int major;

	private final int minor;

	private final int patch;

	/**
	 * Creates a {@link Version}.
	 *
	 * @param major a major value
	 */
	public Version(@Nonnegative int major) {
		this(nonNegative(major), -1, -1, Integer.toString(major));
	}

	/**
	 * Creates a {@link Version}.
	 *
	 * @param major a major value
	 * @param minor a minor value
	 */
	public Version(@Nonnegative int major, @Nonnegative int minor) {
		this(nonNegative(major), nonNegative(minor), -1, String.format("%s.%s", major, minor));
	}

	/**
	 * Creates a {@link Version}.
	 *
	 * @param major a major value
	 * @param minor a minor value
	 * @param patch a patch value
	 */
	public Version(@Nonnegative int major, @Nonnegative int minor, @Nonnegative int patch) {
		this(nonNegative(major), nonNegative(minor), nonNegative(patch),
				String.format("%s.%s.%s", major, minor, patch));
	}

	/**
	 * Creates a {@link Version}.
	 *
	 * @param major a major value
	 * @param minor a minor value
	 * @param patch a patch value
	 * @param version a string value of the version
	 */
	private Version(int major, int minor, int patch, String version) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.version = version;
	}

	/**
	 * Parses a {@code version}.
	 *
	 * @param version a text version (expected format ({@link #VERSION_PATTERN}))
	 * @return a parsed {@link Version}
	 */
	public static Version parse(String version) {
		Objects.requireNonNull(version, "Version must not be null");
		Matcher matcher = VERSION_PATTERN.matcher(version.trim());
		if (matcher.find()) {
			int major = Integer.parseInt(matcher.group(1));
			int minor = -1;
			int patch = -1;
			String minorGroup = matcher.group(3);
			if (StringUtils.hasText(minorGroup)) {
				minor = Integer.parseInt(minorGroup);
			}
			String patchGroup = matcher.group(5);
			if (StringUtils.hasText(patchGroup)) {
				patch = Integer.parseInt(patchGroup);
			}
			return new Version(major, minor, patch, matcher.group());
		}
		throw new IllegalArgumentException(
				String.format("Version '%s' is invalid. Expected format is %s", version, VERSION_PATTERN));
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
	 * @return The value of the {@code minor} attribute, or {@code -1}.
	 */
	public int getMinor() {
		return this.minor;
	}

	/**
	 * Returns a patch value.
	 *
	 * @return The value of the {@code patch} attribute, or {@code -1}.
	 */
	public int getPatch() {
		return this.patch;
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
		int majorCmp = Integer.compare(this.major, other.major);
		if (majorCmp == 0) {
			int minCmp = Integer.compare(this.minor, other.minor);
			if (minCmp == 0) {
				int patchCmp = Integer.compare(this.patch, other.patch);
				if (patchCmp == 0) {
					return this.version.compareTo(other.version);
				}
				return patchCmp;
			}
			return minCmp;
		}
		return majorCmp;
	}

	private static int nonNegative(int value) {
		if (value < 0) {
			throw new IllegalArgumentException(String.format("Value '%s' must be positive or zero", value));
		}
		return value;
	}

}
