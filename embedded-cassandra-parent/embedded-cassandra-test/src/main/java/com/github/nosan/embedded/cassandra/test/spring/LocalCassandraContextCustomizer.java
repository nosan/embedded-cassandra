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

package com.github.nosan.embedded.cassandra.test.spring;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.util.Assert;

/**
 * {@link ContextCustomizer} that register {@link LocalCassandraFactoryBean}.
 *
 * @author Dmytro Nosan
 * @since 1.0.7
 */
class LocalCassandraContextCustomizer implements ContextCustomizer {


	@Nullable
	private final Class<?> testClass;

	@Nonnull
	private final LocalCassandra annotation;

	/**
	 * Creates {@link LocalCassandraContextCustomizer}.
	 *
	 * @param testClass test class
	 * @param annotation annotation
	 */
	LocalCassandraContextCustomizer(@Nullable Class<?> testClass, @Nonnull LocalCassandra annotation) {
		this.testClass = testClass;
		this.annotation = Objects.requireNonNull(annotation, "@LocalCassandra must not be null");
	}

	@Override
	public void customizeContext(@Nonnull ConfigurableApplicationContext context,
			@Nonnull MergedContextConfiguration mergedConfig) {
		Assert.isInstanceOf(BeanDefinitionRegistry.class, context.getBeanFactory(),
				String.format("(%s) can only be used with a BeanDefinitionRegistry", getClass()));
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
		RootBeanDefinition bd = new RootBeanDefinition(LocalCassandraFactoryBean.class);
		bd.getConstructorArgumentValues().addIndexedArgumentValue(0, this.testClass);
		bd.getConstructorArgumentValues().addIndexedArgumentValue(1, this.annotation);
		registry.registerBeanDefinition(LocalCassandraFactoryBean.BEAN_NAME, bd);

	}


}
