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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apiguardian.api.API;

import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * Simple class to represent {@link Cassandra} version.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
@API(since = "1.0.0", status = API.Status.STABLE)
public final class Version implements Comparable<Version> {

	private static final Pattern VERSION_PATTERN = Pattern.compile("^([0-9]+)(\\.([0-9]+))?(\\.([0-9]+))?(.*)$");

	@Nonnull
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
		this(nonNegative(major), -1, -1, null);
	}

	/**
	 * Creates a {@link Version}.
	 *
	 * @param major a major value
	 * @param minor a minor value
	 */
	public Version(@Nonnegative int major, @Nonnegative int minor) {
		this(nonNegative(major), nonNegative(minor), -1, null);
	}

	/**
	 * Creates a {@link Version}.
	 *
	 * @param major a major value
	 * @param minor a minor value
	 * @param patch a patch value
	 */
	public Version(@Nonnegative int major, @Nonnegative int minor, @Nonnegative int patch) {
		this(nonNegative(major), nonNegative(minor), nonNegative(patch), null);
	}

	/**
	 * Creates a {@link Version}.
	 *
	 * @param major a major value
	 * @param minor a minor value
	 * @param patch a patch value
	 * @param version a string value of the version
	 */
	private Version(int major, int minor, int patch, @Nullable String version) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.version = StringUtils.hasText(version) ? version : toVersion(major, minor, patch);
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

	@Nonnull
	@Override
	public String toString() {
		return this.version;
	}

	@Override
	public int compareTo(@Nonnull Version other) {
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

	/**
	 * Parses a {@code version}.
	 *
	 * @param version a version (expected format ({@link #VERSION_PATTERN}))
	 * @return a parsed {@link Version}
	 */
	@Nonnull
	public static Version parse(@Nonnull String version) {
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
				String.format("Version (%s) is invalid. Expected format is %s", version, VERSION_PATTERN));
	}

	private static String toVersion(int major, int minor, int patch) {
		return IntStream.of(major, minor, patch)
				.filter(i -> i >= 0)
				.mapToObj(Integer::toString)
				.collect(Collectors.joining("."));
	}

	private static int nonNegative(int value) {
		if (value < 0) {
			throw new IllegalArgumentException(String.format("Value (%s) must be positive or zero", value));
		}
		return value;
	}
}
