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

import javax.annotation.Nonnull;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.util.Assert;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.cql.CqlScript;

/**
 * {@link ContextCustomizer} that allows the Cassandra to be {@link Cassandra#start() started} and
 * {@link Cassandra#stop() stopped}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
class EmbeddedCassandraContextCustomizer implements ContextCustomizer {


	@Nonnull
	private final EmbeddedCassandra annotation;

	EmbeddedCassandraContextCustomizer(@Nonnull EmbeddedCassandra annotation) {
		this.annotation = annotation;
	}

	@Override
	public void customizeContext(@Nonnull ConfigurableApplicationContext context,
			@Nonnull MergedContextConfiguration mergedConfig) {
		Assert.isInstanceOf(BeanDefinitionRegistry.class, context.getBeanFactory(),
				"Embedded Cassandra Context Customizer can only be used with a BeanDefinitionRegistry");
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

		CqlConfig config = new CqlConfig();
		config.setEncoding(this.annotation.encoding());
		config.setScripts(this.annotation.scripts());
		config.setStatements(this.annotation.statements());
		config.setTestClass(mergedConfig.getTestClass());

		CqlScript[] cqlScripts = CqlResourceUtils.getScripts(context, config);


		RootBeanDefinition bd = new RootBeanDefinition(EmbeddedCassandraFactoryBean.class);
		bd.getConstructorArgumentValues().addIndexedArgumentValue(0, cqlScripts);
		registry.registerBeanDefinition(EmbeddedCassandraFactoryBean.EMBEDDED_CASSANDRA_NAME, bd);

		if (this.annotation.replace() == EmbeddedCassandra.Replace.ANY) {
			registry.registerBeanDefinition(EmbeddedClusterBeanFactoryPostProcessor.EMBEDDED_CLUSTER_PROCESSOR_NAME,
					new RootBeanDefinition(EmbeddedClusterBeanFactoryPostProcessor.class));
		}
	}


}
