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

package com.github.nosan.embedded.cassandra.test.support;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

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
	 * @param target object from which the represented field's value is to be extracted
	 * @param name field name
	 * @return the value of the represented  field in object
	 */
	public static Object getField(Object target, String name) {
		Objects.requireNonNull(target, "Target must not be null");
		Objects.requireNonNull(name, "Name must not be null");
		return run(() -> {
			Field field = findField(target.getClass(), name, f -> !Modifier.isStatic(f.getModifiers()));
			return field.get(target);
		});
	}

	/**
	 * Returns the value of the method on the specified object.
	 *
	 * @param target object from which the represented method's value is to be extracted
	 * @param name method name
	 * @param types argument types
	 * @param arguments method arguments
	 * @return the value of the represented  method in object
	 */
	public static Object getMethod(Object target, String name, Class<?>[] types, Object[] arguments) {
		Objects.requireNonNull(target, "Target must not be null");
		Objects.requireNonNull(name, "Name must not be null");
		Objects.requireNonNull(types, "Types must not be null");
		Objects.requireNonNull(arguments, "Arguments must not be null");
		return run(() -> {
			Method method = findMethod(target.getClass(), name, types, m -> !Modifier.isStatic(m.getModifiers()));
			return method.invoke(null, arguments);
		});
	}

	/**
	 * Returns the value of the static field on the specified class.
	 *
	 * @param target class from which the represented field's value is to be extracted
	 * @param name field name
	 * @return the value of the represented static field in class
	 */
	public static Object getStaticField(Class<?> target, String name) {
		Objects.requireNonNull(target, "Target must not be null");
		Objects.requireNonNull(name, "Name must not be null");
		return run(() -> {
			Field field = findField(target, name, f -> Modifier.isStatic(f.getModifiers()));
			return field.get(null);
		});
	}

	/**
	 * Returns the value of the static method on the specified class.
	 *
	 * @param target class from which the represented method's value is to be extracted
	 * @param name method name
	 * @param types argument types
	 * @param arguments method arguments
	 * @return the value of the represented static method in class
	 */
	public static Object getStaticMethod(Class<?> target, String name, Class<?>[] types, Object[] arguments) {
		Objects.requireNonNull(target, "Target must not be null");
		Objects.requireNonNull(name, "Name must not be null");
		Objects.requireNonNull(types, "Types must not be null");
		Objects.requireNonNull(arguments, "Arguments must not be null");
		return run(() -> {
			Method method = findMethod(target, name, types, m -> Modifier.isStatic(m.getModifiers()));
			return method.invoke(null, arguments);
		});
	}

	private static Field findField(Class<?> clazz, String name, Predicate<? super Field> filter)
			throws NoSuchFieldException {
		for (Class<?> searchType = clazz; searchType != null; searchType = searchType.getSuperclass()) {
			Field[] fields = searchType.getDeclaredFields();
			for (Field field : fields) {
				if (name.equals(field.getName()) && filter.test(field)) {
					field.setAccessible(true);
					return field;
				}
			}
		}
		throw new NoSuchFieldException(name);
	}

	private static Method findMethod(Class<?> clazz, String name, Class<?>[] types, Predicate<? super Method> filter)
			throws NoSuchMethodException {
		for (Class<?> searchType = clazz; searchType != null; searchType = searchType.getSuperclass()) {
			Method[] methods = searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods();
			for (Method method : methods) {
				if (name.equals(method.getName()) && Arrays.equals(types, method.getParameterTypes()) && filter
						.test(method)) {
					method.setAccessible(true);
					return method;
				}
			}
		}
		throw new NoSuchMethodException(name);
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
