/*
 * Copyright 2012-2018 the original author or authors.
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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author Dmytro Nosan
 */
public abstract class ReflectionUtils {

	public static Object getField(Object target, String name) throws NoSuchFieldException, IllegalAccessException {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		return field.get(target);
	}

	public static Object getMethod(Object target, String name, Object[] arguments)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		Method method = target.getClass().getDeclaredMethod(name, Arrays.stream(arguments)
				.map(Object::getClass).toArray(Class[]::new));
		method.setAccessible(true);
		return method.invoke(target, arguments);
	}


	public static Object getStaticField(Class<?> target, String name)
			throws NoSuchFieldException, IllegalAccessException {
		Field field = target.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(null);
	}

	public static Object getStaticMethod(Class<?> target, String name, Object[] arguments)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		Method method = target.getDeclaredMethod(name, Arrays.stream(arguments)
				.map(Object::getClass).toArray(Class[]::new));
		method.setAccessible(true);
		return method.invoke(null, arguments);
	}
}
