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

package com.github.nosan.embedded.cassandra.spring.test;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;

import com.github.nosan.embedded.cassandra.EmbeddedCassandraFactory;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.CassandraFactory;

/**
 * {@link ContextCustomizer} to support {@link EmbeddedCassandra} annotation.
 *
 * @author Dmytro Nosan
 */
class EmbeddedCassandraContextCustomizer implements ContextCustomizer {

	@Override
	public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
		BeanDefinitionRegistry registry = getRegistry(context);
		if (registry.containsBeanDefinition(Cassandra.class.getName())) {
			registry.removeBeanDefinition(Cassandra.class.getName());
		}
		registry.registerBeanDefinition(Cassandra.class.getName(), createBeanDefinition(context));
	}

	@Override
	public boolean equals(Object obj) {
		return (obj != null && obj.getClass() == getClass());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	private static BeanDefinitionRegistry getRegistry(ConfigurableApplicationContext applicationContext) {
		if (applicationContext instanceof BeanDefinitionRegistry) {
			return ((BeanDefinitionRegistry) applicationContext);
		}
		return ((BeanDefinitionRegistry) applicationContext.getBeanFactory());
	}

	private static GenericBeanDefinition createBeanDefinition(ConfigurableApplicationContext context) {
		GenericBeanDefinition bd = new GenericBeanDefinition();
		bd.setBeanClass(Cassandra.class);
		bd.setInitMethodName("start");
		bd.setDestroyMethodName("stop");
		bd.setLazyInit(false);
		bd.setScope(BeanDefinition.SCOPE_SINGLETON);
		bd.setInstanceSupplier(() -> {
			try {
				return context.getBean(CassandraFactory.class).create();
			}
			catch (NoSuchBeanDefinitionException ex) {
				EmbeddedCassandraFactory cassandraFactory = new EmbeddedCassandraFactory();
				cassandraFactory.setPort(0);
				cassandraFactory.setRpcPort(0);
				cassandraFactory.setJmxLocalPort(0);
				cassandraFactory.setStoragePort(0);
				return cassandraFactory.create();
			}
		});
		return bd;
	}

}
