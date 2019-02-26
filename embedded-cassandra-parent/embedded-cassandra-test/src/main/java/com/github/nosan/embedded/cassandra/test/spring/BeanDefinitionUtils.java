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

package com.github.nosan.embedded.cassandra.test.spring;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.util.Assert;

/**
 * Utility class for dealing with {@link BeanDefinition}.
 *
 * @author Dmytro Nosan
 * @since 1.4.1
 */
abstract class BeanDefinitionUtils {

	private static final Logger log = LoggerFactory.getLogger(BeanDefinitionUtils.class);

	/**
	 * Register a new bean definition with a registry. Replaces existing {@link BeanDefinition} if exists.
	 *
	 * @param registry {@link BeanDefinitionRegistry} for registration bean definitions
	 * @param beanName the name of the bean instance to register
	 * @param beanDefinition definition of the bean instance to register
	 */
	static void registerBeanDefinition(@Nonnull BeanDefinitionRegistry registry, @Nonnull String beanName,
			@Nonnull BeanDefinition beanDefinition) {
		if (registry.containsBeanDefinition(beanName)) {
			registry.removeBeanDefinition(beanName);
		}
		registry.registerBeanDefinition(beanName, beanDefinition);
		log.info("{} '{}' bean has been registered.",
				beanDefinition.isPrimary() ? "The @Primary" : "", beanName);
	}

	/**
	 * Check that {@link ConfigurableListableBeanFactory} is instance of {@link BeanDefinitionRegistry} and cast it to
	 * the registry.
	 *
	 * @param beanFactory the bean factory
	 * @return the bean definition registry
	 */
	@Nonnull
	static BeanDefinitionRegistry getRegistry(@Nonnull ConfigurableListableBeanFactory beanFactory) {
		Assert.isInstanceOf(BeanDefinitionRegistry.class, beanFactory);
		return (BeanDefinitionRegistry) beanFactory;
	}

}
