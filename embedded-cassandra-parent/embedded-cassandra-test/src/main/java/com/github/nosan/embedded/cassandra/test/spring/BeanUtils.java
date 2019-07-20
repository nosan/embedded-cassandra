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

package com.github.nosan.embedded.cassandra.test.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * Utility methods for dealing with getting beans from the {@link ApplicationContext}.
 *
 * @author Dmytro Nosan
 * @since 2.0.4
 */
abstract class BeanUtils {

	static <T> Optional<T> getUniqueBean(ApplicationContext applicationContext, Class<T> beanClass) {
		ObjectProvider<T> beanProvider = getBeanProvider(applicationContext, beanClass);
		if (beanProvider != null) {
			return Optional.ofNullable(beanProvider.getIfUnique());
		}
		try {
			return Optional.of(applicationContext.getBean(beanClass));
		}
		catch (NoSuchBeanDefinitionException ex) {
			return Optional.empty();
		}
	}

	static <T> List<T> getBeans(ApplicationContext applicationContext, Class<T> beanClass) {
		ObjectProvider<T> beanProvider = getBeanProvider(applicationContext, beanClass);
		if (beanProvider != null) {
			return beanProvider.orderedStream().collect(Collectors.toList());
		}
		Map<String, T> beansOfType = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, beanClass);
		List<T> beans = new ArrayList<>(beansOfType.values());
		AnnotationAwareOrderComparator.sort(beans);
		return beans;
	}

	@Nullable
	private static <T> ObjectProvider<T> getBeanProvider(ApplicationContext applicationContext, Class<T> beanClass) {
		try {
			return applicationContext.getBeanProvider(beanClass);
		}
		catch (NoSuchMethodError ex) {
			return null;
		}
	}

}
