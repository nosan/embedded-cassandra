/*
 * Copyright 2018-2018 the original author or authors.
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Simple class to represent {@link Cassandra} version.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
public final class Version implements Comparable<Version> {

	private static final Pattern VERSION =
			Pattern.compile("^\\s*([0-9]+)\\.([0-9]+)\\.([0-9]+)\\s*$");

	private final int major;

	private final int minor;

	private final int patch;

	/**
	 * Creates a {@link Version}.
	 *
	 * @param major a major value
	 * @param minor a minor value
	 * @param patch a patch value
	 */
	public Version(int major, int minor, int patch) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
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
	 * Returns a patch value.
	 *
	 * @return The value of the {@code patch} attribute
	 */
	public int getPatch() {
		return this.patch;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.major, this.minor, this.patch);
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		Version version = (Version) other;
		return this.major == version.major &&
				this.minor == version.minor &&
				this.patch == version.patch;
	}

	@Override
	@Nonnull
	public String toString() {
		return String.format("%s.%s.%s", this.major, this.minor, this.patch);
	}

	@Override
	public int compareTo(@Nonnull Version other) {
		int major = Integer.compare(this.major, other.major);
		if (major == 0) {
			int minor = Integer.compare(this.minor, other.minor);
			if (minor == 0) {
				return Integer.compare(this.patch, other.patch);
			}
			return minor;
		}
		return major;
	}

	/**
	 * Parses a {@code version}.
	 *
	 * @param version a version (expected format ({@code int.int.int}))
	 * @return a parsed {@link Version}
	 */
	@Nonnull
	public static Version parse(@Nonnull String version) {
		Objects.requireNonNull(version, "Version must not be null");
		Matcher matcher = VERSION.matcher(version);
		if (matcher.find()) {
			int major = Integer.parseInt(matcher.group(1));
			int minor = Integer.parseInt(matcher.group(2));
			int patch = Integer.parseInt(matcher.group(3));
			return new Version(major, minor, patch);
		}
		throw new IllegalArgumentException(String.format("Version (%s) is invalid. Expected format is (int.int.int)",
				version));
	}

}
