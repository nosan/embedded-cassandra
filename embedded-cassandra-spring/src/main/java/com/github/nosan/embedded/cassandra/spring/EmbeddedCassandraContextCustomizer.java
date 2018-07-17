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

package com.github.nosan.embedded.cassandra.spring;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.util.Assert;

/**
 * {@link ContextCustomizer} to add {@link EmbeddedCassandraConfiguration} if
 * {@code Test Class} has an {@link EmbeddedCassandra} annotation.
 *
 * @author Dmytro Nosan
 */
class EmbeddedCassandraContextCustomizer implements ContextCustomizer {

	private final EmbeddedCassandra embeddedCassandra;

	EmbeddedCassandraContextCustomizer(EmbeddedCassandra embeddedCassandra) {
		this.embeddedCassandra = embeddedCassandra;
	}

	@Override
	public void customizeContext(ConfigurableApplicationContext context,
			MergedContextConfiguration mergedConfig) {
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		Assert.isInstanceOf(BeanDefinitionRegistry.class, beanFactory,
				"Embedded Cassandra Context Customizer can only be used with a BeanDefinitionRegistry");
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
		RootBeanDefinition beanDefinition = new RootBeanDefinition(EmbeddedCassandraConfiguration.class);
		beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(this.embeddedCassandra);
		registry.registerBeanDefinition("embeddedCassandraConfiguration", beanDefinition);
	}

}
