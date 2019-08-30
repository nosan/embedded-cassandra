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

package com.github.nosan.embedded.cassandra.spring.test.context;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;

import com.github.nosan.embedded.cassandra.EmbeddedCassandraFactory;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.CassandraFactory;
import com.github.nosan.embedded.cassandra.api.CassandraFactoryCustomizer;
import com.github.nosan.embedded.cassandra.spring.test.EmbeddedCassandra;

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
		GenericBeanDefinition bd = new GenericBeanDefinition();
		bd.setBeanClass(Cassandra.class);
		bd.setInitMethodName("start");
		bd.setDestroyMethodName("stop");
		bd.setLazyInit(false);
		bd.setScope(BeanDefinition.SCOPE_SINGLETON);
		bd.setInstanceSupplier(new CassandraSupplier(context));
		registry.registerBeanDefinition(Cassandra.class.getName(), bd);
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

	private static final class CassandraSupplier implements Supplier<Cassandra> {

		private final ConfigurableApplicationContext context;

		CassandraSupplier(ConfigurableApplicationContext context) {
			this.context = context;
		}

		@Override
		public Cassandra get() {
			try {
				return this.context.getBean(CassandraFactory.class).create();
			}
			catch (NoSuchBeanDefinitionException ex) {
				EmbeddedCassandraFactory cassandraFactory = EmbeddedCassandraFactory.random();
				for (CassandraFactoryCustomizer<? super EmbeddedCassandraFactory> customizer : getCustomizers(
						this.context)) {
					customizer.customize(cassandraFactory);
				}
				return cassandraFactory.create();
			}
		}

		@SuppressWarnings("unchecked")
		private static List<CassandraFactoryCustomizer<? super EmbeddedCassandraFactory>> getCustomizers(
				ConfigurableApplicationContext context) {
			List<CassandraFactoryCustomizer<? super EmbeddedCassandraFactory>> customizers = new ArrayList<>();
			String[] names = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(context,
					ResolvableType.forType(new CassandraFactoryCustomizerTypeReference()));
			for (String name : names) {
				customizers.add((CassandraFactoryCustomizer<? super EmbeddedCassandraFactory>) context.getBean(name));
			}
			AnnotationAwareOrderComparator.sort(customizers);
			return customizers;
		}

		private static final class CassandraFactoryCustomizerTypeReference
				extends ParameterizedTypeReference<CassandraFactoryCustomizer<? super EmbeddedCassandraFactory>> {

		}

	}

}
