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

import java.util.LinkedHashMap;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
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

	private final EmbeddedCassandra annotation;

	EmbeddedCassandraContextCustomizer(EmbeddedCassandra annotation) {
		this.annotation = annotation;
	}

	@Override
	public void customizeContext(ConfigurableApplicationContext context,
			MergedContextConfiguration mergedConfig) {
		Assert.isInstanceOf(BeanDefinitionRegistry.class, context.getBeanFactory(),
				"Embedded Cassandra Context Customizer can only be used with a BeanDefinitionRegistry");
		addProperties(context, mergedConfig.getTestClass());
		((BeanDefinitionRegistry) context.getBeanFactory()).registerBeanDefinition("EmbeddedCassandraConfiguration",
				new RootBeanDefinition(EmbeddedCassandraConfiguration.class));
	}

	private void addProperties(ConfigurableApplicationContext context, Class<?> testClass) {
		ConfigurableEnvironment environment = context.getEnvironment();
		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
		properties.put(EmbeddedCassandraConfiguration.TEST_CLASS, testClass);
		properties.put(EmbeddedCassandraConfiguration.SCRIPTS, this.annotation.value());
		properties.put(EmbeddedCassandraConfiguration.ENCODING, this.annotation.encoding());
		properties.put(EmbeddedCassandraConfiguration.STATEMENTS, this.annotation.statements());
		environment.getPropertySources().addFirst(new MapPropertySource(EmbeddedCassandra.class.getName(),
				properties));
	}

}
