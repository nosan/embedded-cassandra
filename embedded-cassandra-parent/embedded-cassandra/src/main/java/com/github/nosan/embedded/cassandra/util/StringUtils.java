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

package com.github.nosan.embedded.cassandra.util;

import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * Utility methods for dealing with strings. <b>Only for internal purposes.</b>
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
public abstract class StringUtils {

	/**
	 * Check whether the given {@code string} is empty.
	 *
	 * @param source the candidate String
	 * @return {@code true} if the {@code string} is {@code null} or has no length
	 */
	public static boolean isEmpty(@Nullable CharSequence source) {
		return !hasLength(source);
	}

	/**
	 * Check that the given {@code string} is neither {@code null} nor of length 0.
	 *
	 * @param source the {@code string} to check
	 * @return {@code true} if the {@code string} is not {@code null} and has length
	 */
	public static boolean hasLength(@Nullable CharSequence source) {
		return (source != null && source.length() > 0);
	}

	/**
	 * Check whether the given {@code string} contains actual <em>text</em>.
	 *
	 * @param source the {@code string} to check (may be {@code null})
	 * @return {@code true} if the {@code string} is not {@code null}, its length is greater than 0, and it does not
	 * contain whitespace only
	 */
	public static boolean hasText(@Nullable CharSequence source) {
		if (source == null) {
			return false;
		}
		for (int i = 0; i < source.length(); i++) {
			if (!Character.isWhitespace(source.charAt(i))) {
				return true;
			}
		}
		return false;
	}

}
