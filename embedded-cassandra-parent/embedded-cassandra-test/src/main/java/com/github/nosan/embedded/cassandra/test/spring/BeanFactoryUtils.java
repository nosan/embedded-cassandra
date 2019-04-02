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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import com.github.nosan.embedded.cassandra.util.annotation.Nullable;

/**
 * Utility class for dealing with beans.
 *
 * @author Dmytro Nosan
 * @since 1.2.6
 */
abstract class BeanFactoryUtils {

	private static final Logger log = LoggerFactory.getLogger(BeanFactoryUtils.class);

	/**
	 * Retrieves a bean only if it a unique and exists.
	 *
	 * @param applicationContext the application context
	 * @param targetClass type the bean must match; can be an interface or superclass
	 * @param <T> type of the bean
	 * @return an instance of the bean, or {@code null}
	 * @throws BeansException if the bean could not be created
	 */
	@Nullable
	static <T> T getIfUnique(ApplicationContext applicationContext, Class<T> targetClass) throws BeansException {
		try {
			Map<String, T> beans = org.springframework.beans.factory.BeanFactoryUtils
					.beansOfTypeIncludingAncestors(applicationContext, targetClass);
			if (beans.size() == 1) {
				return beans.values().iterator().next();
			}
			return applicationContext.getBean(targetClass);
		}
		catch (NoUniqueBeanDefinitionException ex) {
			log.warn(ex.getMessage());
			return null;
		}
		catch (NoSuchBeanDefinitionException ex) {
			return null;
		}
	}

}
