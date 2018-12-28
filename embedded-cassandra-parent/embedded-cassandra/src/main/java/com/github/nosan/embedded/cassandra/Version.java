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

	private static final Pattern VERSION_PATTERN =
			Pattern.compile("^\\s*([0-9]+)(\\.([0-9]+))?(\\.([0-9]+))?\\s*$");

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
		this.major = Math.max(major, 0);
		this.minor = Math.max(minor, -1);
		this.patch = Math.max(patch, -1);
	}

	/**
	 * Creates a {@link Version}.
	 *
	 * @param major a major value
	 * @param minor a minor value
	 */
	public Version(int major, int minor) {
		this(major, minor, -1);
	}

	/**
	 * Creates a {@link Version}.
	 *
	 * @param major a major value
	 */
	public Version(int major) {
		this(major, -1, -1);
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

	@Nonnull
	@Override
	public String toString() {
		StringBuilder text = new StringBuilder().append(this.major);
		if (this.minor >= 0) {
			text.append(".").append(this.minor);
		}
		if (this.patch >= 0) {
			text.append(".").append(this.patch);
		}
		return text.toString();
	}

	@Override
	public int compareTo(@Nonnull Version other) {
		int majorCmp = Integer.compare(this.major, other.major);
		if (majorCmp == 0) {
			int minCmp = Integer.compare(this.minor, other.minor);
			if (minCmp == 0) {
				return Integer.compare(this.patch, other.patch);
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
		Matcher matcher = VERSION_PATTERN.matcher(version);
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
			return new Version(major, minor, patch);
		}
		throw new IllegalArgumentException(
				String.format("Version (%s) is invalid. Expected format is %s", version, VERSION_PATTERN));
	}

}
