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

package com.github.nosan.embedded.cassandra.test.support;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

/**
 * Utility methods for dealing with {@code Reflections}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
public abstract class ReflectionUtils {

	/**
	 * Returns the value of the field on the specified object.
	 *
	 * @param target object from which the represented field's value is
	 * to be extracted
	 * @param name field name
	 * @return the value of the represented  field in object
	 */
	public static Object getField(@Nonnull Object target, @Nonnull String name) {
		Objects.requireNonNull(target, "Target must not be null");
		Objects.requireNonNull(name, "Name must not be null");
		return run(() -> {
			Field field = target.getClass().getDeclaredField(name);
			field.setAccessible(true);
			return field.get(target);
		});
	}

	/**
	 * Returns the value of the method on the specified object.
	 *
	 * @param target object from which the represented method's value is
	 * to be extracted
	 * @param name method name
	 * @param arguments method arguments
	 * @return the value of the represented  method in object
	 */
	public static Object getMethod(@Nonnull Object target, @Nonnull String name, @Nonnull Object[] arguments) {
		Objects.requireNonNull(target, "Target must not be null");
		Objects.requireNonNull(name, "Name must not be null");
		Objects.requireNonNull(arguments, "Arguments must not be null");
		return run(() -> {
			Method method = target.getClass().getDeclaredMethod(name, Arrays.stream(arguments)
					.map(Object::getClass).toArray(Class[]::new));
			method.setAccessible(true);
			return method.invoke(target, arguments);
		});
	}

	/**
	 * Returns the value of the static field on the specified class.
	 *
	 * @param target class from which the represented field's value is
	 * to be extracted
	 * @param name field name
	 * @return the value of the represented static field in class
	 */
	public static Object getStaticField(@Nonnull Class<?> target, @Nonnull String name) {
		Objects.requireNonNull(target, "Target must not be null");
		Objects.requireNonNull(name, "Name must not be null");
		return run(() -> {
			Field field = target.getDeclaredField(name);
			field.setAccessible(true);
			return field.get(null);
		});
	}

	/**
	 * Returns the value of the static method on the specified class.
	 *
	 * @param target class from which the represented method's value is
	 * to be extracted
	 * @param name method name
	 * @param arguments method arguments
	 * @return the value of the represented static method in class
	 */
	public static Object getStaticMethod(@Nonnull Class<?> target, @Nonnull String name, @Nonnull Object[] arguments) {
		Objects.requireNonNull(target, "Target must not be null");
		Objects.requireNonNull(name, "Name must not be null");
		Objects.requireNonNull(arguments, "Arguments must not be null");
		return run(() -> {
			Method method = target.getDeclaredMethod(name, Arrays.stream(arguments)
					.map(Object::getClass).toArray(Class[]::new));
			method.setAccessible(true);
			return method.invoke(null, arguments);
		});
	}

	private static Object run(Callable callable) {
		try {
			return callable.call();
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
