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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
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
import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.CassandraCreationException;
import com.github.nosan.embedded.cassandra.api.CassandraFactory;
import com.github.nosan.embedded.cassandra.api.CassandraFactoryCustomizer;
import com.github.nosan.embedded.cassandra.api.connection.CassandraConnection;
import com.github.nosan.embedded.cassandra.api.connection.CassandraConnectionFactory;
import com.github.nosan.embedded.cassandra.api.connection.DefaultCassandraConnectionFactory;
import com.github.nosan.embedded.cassandra.api.cql.CqlDataSet;
import com.github.nosan.embedded.cassandra.commons.io.Resource;
import com.github.nosan.embedded.cassandra.commons.io.SpringResource;
import com.github.nosan.embedded.cassandra.spring.test.EmbeddedCassandra;

/**
 * {@link ContextCustomizer} to support {@link EmbeddedCassandra} annotation.
 *
 * @author Dmytro Nosan
 */
class EmbeddedCassandraContextCustomizer implements ContextCustomizer {

	private final EmbeddedCassandra annotation;

	EmbeddedCassandraContextCustomizer(EmbeddedCassandra annotation) {
		this.annotation = annotation;
	}

	@Override
	public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
		BeanDefinitionRegistry registry = getRegistry(context);
		Resource[] resources = getResources(this.annotation, context);
		Charset charset = Charset.forName(this.annotation.encoding());
		CqlDataSet dataSet = CqlDataSet.ofResources(charset, resources);
		registerCassandraBeanDefinition(context, registry);
		registerCassandraConnectionBeanDefinition(context, registry);
		registerCassandraInitializerBeanDefinition(dataSet, context, registry);
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		EmbeddedCassandraContextCustomizer that = (EmbeddedCassandraContextCustomizer) other;
		return this.annotation.equals(that.annotation);
	}

	@Override
	public int hashCode() {
		return this.annotation.hashCode();
	}

	private static BeanDefinitionRegistry getRegistry(ConfigurableApplicationContext applicationContext) {
		if (applicationContext instanceof BeanDefinitionRegistry) {
			return ((BeanDefinitionRegistry) applicationContext);
		}
		return ((BeanDefinitionRegistry) applicationContext.getBeanFactory());
	}

	private static void registerCassandraBeanDefinition(ConfigurableApplicationContext context,
			BeanDefinitionRegistry registry) {
		GenericBeanDefinition bd = new GenericBeanDefinition();
		bd.setBeanClass(Cassandra.class);
		bd.setInitMethodName("start");
		bd.setDestroyMethodName("stop");
		bd.setLazyInit(false);
		bd.setScope(BeanDefinition.SCOPE_SINGLETON);
		bd.setInstanceSupplier(new CassandraSupplier(context));
		registry.registerBeanDefinition(Cassandra.class.getName(), bd);
	}

	private static void registerCassandraConnectionBeanDefinition(ConfigurableApplicationContext context,
			BeanDefinitionRegistry registry) {
		GenericBeanDefinition bd = new GenericBeanDefinition();
		bd.setBeanClass(CassandraConnection.class);
		bd.setDestroyMethodName("close");
		bd.setLazyInit(true);
		bd.setDependsOn(Cassandra.class.getName());
		bd.setScope(BeanDefinition.SCOPE_SINGLETON);
		bd.setInstanceSupplier(new CassandraConnectionSupplier(context));
		registry.registerBeanDefinition(CassandraConnection.class.getName(), bd);
	}

	private static void registerCassandraInitializerBeanDefinition(CqlDataSet dataSet,
			ConfigurableApplicationContext context,
			BeanDefinitionRegistry registry) {
		GenericBeanDefinition bd = new GenericBeanDefinition();
		bd.setBeanClass(CassandraInitializer.class);
		bd.setLazyInit(false);
		bd.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		bd.setScope(BeanDefinition.SCOPE_SINGLETON);
		bd.setInstanceSupplier(() -> new CassandraInitializer(context, dataSet));
		registry.registerBeanDefinition(CassandraInitializer.class.getName(), bd);
	}

	private static Resource[] getResources(EmbeddedCassandra annotation, ConfigurableApplicationContext context) {
		List<Resource> resources = new ArrayList<>();
		for (String script : annotation.scripts()) {
			for (org.springframework.core.io.Resource resource : doGetResources(context, script)) {
				if (resource.exists()) {
					resources.add(new SpringResource(resource));
				}
			}
		}
		return resources.toArray(new Resource[0]);
	}

	private static org.springframework.core.io.Resource[] doGetResources(ConfigurableApplicationContext context,
			String location) {
		try {
			org.springframework.core.io.Resource[] resources = context.getResources(location);
			Arrays.sort(resources, Comparator.comparing(resource -> toURL(resource).toString()));
			return resources;
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private static URL toURL(org.springframework.core.io.Resource resource) {
		try {
			return resource.getURL();
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
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
				return new DefaultCassandraFactory(getCustomizers(this.context)).create();
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

	private static final class CassandraConnectionSupplier implements Supplier<CassandraConnection> {

		private final ConfigurableApplicationContext context;

		CassandraConnectionSupplier(ConfigurableApplicationContext context) {
			this.context = context;
		}

		@Override
		public CassandraConnection get() {
			Cassandra cassandra = this.context.getBean(Cassandra.class.getName(), Cassandra.class);
			try {
				return this.context.getBean(CassandraConnectionFactory.class).create(cassandra);
			}
			catch (NoSuchBeanDefinitionException ex) {
				return new DefaultCassandraConnectionFactory().create(cassandra);
			}
		}

	}

	private static final class DefaultCassandraFactory implements CassandraFactory {

		private final List<CassandraFactoryCustomizer<? super EmbeddedCassandraFactory>> customizers;

		DefaultCassandraFactory(List<CassandraFactoryCustomizer<? super EmbeddedCassandraFactory>> customizers) {
			this.customizers = customizers;
		}

		@Override
		public Cassandra create() throws CassandraCreationException {
			EmbeddedCassandraFactory cassandraFactory = new EmbeddedCassandraFactory();
			cassandraFactory.setPort(0);
			cassandraFactory.setRpcPort(0);
			cassandraFactory.setJmxLocalPort(0);
			cassandraFactory.setStoragePort(0);
			this.customizers.forEach(customizer -> customizer.customize(cassandraFactory));
			return cassandraFactory.create();
		}

	}

	private static final class CassandraInitializer implements InitializingBean {

		private final ConfigurableApplicationContext context;

		private final CqlDataSet dataSet;

		CassandraInitializer(ConfigurableApplicationContext context, CqlDataSet dataSet) {
			this.context = context;
			this.dataSet = dataSet;
		}

		@Override
		public void afterPropertiesSet() {
			List<String> statements = this.dataSet.getStatements();
			if (!statements.isEmpty()) {
				CassandraConnection cassandraConnection = this.context.getBean(CassandraConnection.class.getName(),
						CassandraConnection.class);
				statements.forEach(cassandraConnection::execute);
			}
		}

	}

}
