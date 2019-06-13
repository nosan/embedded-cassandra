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
 * Utility methods for dealing with classes. <b>Only for internal purposes.</b>
 *
 * @author Dmytro Nosan
 * @since 1.2.1
 */
public abstract class ClassUtils {

	/**
	 * Returns the one of the {@link ClassLoader} to use.
	 * <ol>
	 * <li>Thread.currentThread().getContextClassLoader()</li>
	 * <li>ClassUtils.class.getClassLoader()</li>
	 * <li>ClassLoader.getSystemClassLoader()</li>
	 * </ol>
	 *
	 * @return the class loader to use
	 */
	@Nullable
	public static ClassLoader getClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		}
		catch (Throwable ex) {
			//ignore
		}
		if (cl == null) {
			try {
				cl = ClassUtils.class.getClassLoader();
			}
			catch (Throwable ex) {
				//ignore
			}
		}
		if (cl == null) {
			try {
				cl = ClassLoader.getSystemClassLoader();
			}
			catch (Throwable ex) {
				//ignore
			}
		}
		return cl;
	}

	/**
	 * Determines the package name for the given class.
	 *
	 * @param clazz a class
	 * @return package name or empty string
	 * @since 1.2.2
	 */
	public static String getPackageName(@Nullable Class<?> clazz) {
		while (clazz != null && clazz.isArray()) {
			clazz = clazz.getComponentType();
		}
		String name = (clazz != null) ? clazz.getName() : "";
		int i = name.lastIndexOf('.');
		return (i > 0) ? name.substring(0, i) : "";
	}

	/**
	 * Determine whether the {@link Class} is present or not.
	 *
	 * @param name the name of the class
	 * @param classLoader the class loader to use
	 * @return {@code true} if class is present, otherwise {@code false}
	 * @since 2.0.0
	 */
	public static boolean isPresent(String name, @Nullable ClassLoader classLoader) {
		try {
			Class.forName(name, false, (classLoader != null) ? classLoader : getClassLoader());
			return true;
		}
		catch (ClassNotFoundException ex) {
			return false;
		}
	}

}
