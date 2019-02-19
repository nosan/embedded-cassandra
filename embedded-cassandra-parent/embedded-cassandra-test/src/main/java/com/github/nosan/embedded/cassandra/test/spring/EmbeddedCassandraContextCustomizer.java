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

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.datastax.driver.core.Cluster;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.test.ClusterFactory;
import com.github.nosan.embedded.cassandra.test.TestCassandra;

/**
 * {@link ContextCustomizer} to add {@link EmbeddedCassandraRegistrar} and {@link EmbeddedClusterRegistrar}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
class EmbeddedCassandraContextCustomizer implements ContextCustomizer {

	@Override
	public void customizeContext(@Nonnull ConfigurableApplicationContext context,
			@Nonnull MergedContextConfiguration mergedConfig) {
		BeanDefinitionRegistry registry = BeanDefinitionUtils.getRegistry(context.getBeanFactory());
		Class<?> testClass = mergedConfig.getTestClass();
		EmbeddedCassandra annotation = AnnotatedElementUtils.findMergedAnnotation(testClass,
				EmbeddedCassandra.class);
		if (annotation != null) {
			BeanDefinitionUtils.registerBeanDefinition(registry,
					EmbeddedCassandraRegistrar.class.getName(), BeanDefinitionBuilder
							.rootBeanDefinition(EmbeddedCassandraRegistrar.class)
							.addConstructorArgValue(testClass)
							.addConstructorArgValue(annotation)
							.setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
							.getBeanDefinition());
			if (annotation.replace() == EmbeddedCassandra.Replace.ANY) {
				BeanDefinitionUtils.registerBeanDefinition(registry,
						EmbeddedClusterRegistrar.class.getName(), BeanDefinitionBuilder
								.rootBeanDefinition(EmbeddedClusterRegistrar.class)
								.setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
								.getBeanDefinition());
			}
		}
	}

	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other != null && getClass() == other.getClass()));
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	/**
	 * {@link BeanDefinitionRegistryPostProcessor} adds a {@link EmbeddedCassandraFactoryBean}
	 * bean definition as a primary bean.
	 */
	static class EmbeddedCassandraRegistrar implements BeanDefinitionRegistryPostProcessor, Ordered {

		@Nonnull
		private final Class<?> testClass;

		@Nonnull
		private final EmbeddedCassandra annotation;

		EmbeddedCassandraRegistrar(@Nonnull Class<?> testClass, @Nonnull EmbeddedCassandra annotation) {
			this.testClass = testClass;
			this.annotation = annotation;
		}

		@Override
		public void postProcessBeanDefinitionRegistry(@Nonnull BeanDefinitionRegistry registry) throws BeansException {
			ConfigurableListableBeanFactory beanFactory = BeanDefinitionUtils.getBeanFactory(registry);
			for (String name : beanFactory.getBeanNamesForType(Cassandra.class)) {
				beanFactory.getBeanDefinition(name).setPrimary(false);
			}
			BeanDefinition definition = BeanDefinitionBuilder
					.rootBeanDefinition(EmbeddedCassandraFactoryBean.class)
					.addConstructorArgValue(this.testClass)
					.addConstructorArgValue(this.annotation)
					.getBeanDefinition();
			definition.setPrimary(true);
			BeanDefinitionUtils.registerBeanDefinition(registry,
					EmbeddedCassandraFactoryBean.class.getName(), definition);
		}

		@Override
		public void postProcessBeanFactory(@Nonnull ConfigurableListableBeanFactory beanFactory) throws BeansException {

		}

		@Override
		public int getOrder() {
			return Ordered.LOWEST_PRECEDENCE;
		}
	}

	/**
	 * {@link FactoryBean} used to create and configure a {@link TestCassandra}.
	 */
	static class EmbeddedCassandraFactoryBean implements FactoryBean<TestCassandra>,
			DisposableBean, ApplicationContextAware, InitializingBean {

		@Nonnull
		private final Class<?> testClass;

		@Nonnull
		private final EmbeddedCassandra annotation;

		@Nullable
		private TestCassandra cassandra;

		private ApplicationContext applicationContext;

		EmbeddedCassandraFactoryBean(@Nonnull Class<?> testClass,
				@Nonnull EmbeddedCassandra annotation) {
			this.testClass = testClass;
			this.annotation = annotation;
		}

		@Override
		public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
			this.applicationContext = applicationContext;
		}

		@Nonnull
		@Override
		public TestCassandra getObject() {
			return Objects.requireNonNull(this.cassandra, "Cassandra is not initialized");
		}

		@Nonnull
		@Override
		public Class<?> getObjectType() {
			return TestCassandra.class;
		}

		@Override
		public void destroy() {
			TestCassandra cassandra = this.cassandra;
			if (cassandra != null) {
				cassandra.stop();
			}
		}

		@Override
		public void afterPropertiesSet() {
			ApplicationContext applicationContext = this.applicationContext;
			EmbeddedCassandra annotation = this.annotation;
			CqlConfig config = new CqlConfig(this.testClass, annotation.scripts(),
					annotation.statements(), annotation.encoding());
			CqlScript[] scripts = CqlResourceUtils.getScripts(applicationContext, config);
			TestCassandra cassandra = new TestCassandra(annotation.registerShutdownHook(),
					BeanFactoryUtils.getIfUnique(applicationContext, CassandraFactory.class),
					BeanFactoryUtils.getIfUnique(applicationContext, ClusterFactory.class), scripts);
			this.cassandra = cassandra;
			cassandra.start();
		}

		@Override
		public boolean isSingleton() {
			return true;
		}
	}

	/**
	 * {@link BeanDefinitionRegistryPostProcessor} adds a {@link EmbeddedClusterFactoryBean}
	 * bean definition as a primary bean.
	 */
	static class EmbeddedClusterRegistrar implements BeanDefinitionRegistryPostProcessor, Ordered {

		@Override
		public void postProcessBeanDefinitionRegistry(@Nonnull BeanDefinitionRegistry registry) throws BeansException {
			ConfigurableListableBeanFactory beanFactory = BeanDefinitionUtils.getBeanFactory(registry);
			for (String name : beanFactory.getBeanNamesForType(Cluster.class)) {
				beanFactory.getBeanDefinition(name).setPrimary(false);
			}
			BeanDefinition definition = BeanDefinitionBuilder.rootBeanDefinition(EmbeddedClusterFactoryBean.class)
					.getBeanDefinition();
			definition.setPrimary(true);
			BeanDefinitionUtils.registerBeanDefinition(registry,
					EmbeddedClusterFactoryBean.class.getName(), definition);
		}

		@Override
		public void postProcessBeanFactory(@Nonnull ConfigurableListableBeanFactory beanFactory) throws BeansException {
		}

		@Override
		public int getOrder() {
			return Ordered.LOWEST_PRECEDENCE;
		}
	}

	/**
	 * {@link FactoryBean} used to create and configure a {@link Cluster}.
	 */
	static class EmbeddedClusterFactoryBean implements FactoryBean<Cluster>, ApplicationContextAware {

		private ApplicationContext applicationContext;

		@Override
		public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
			this.applicationContext = applicationContext;
		}

		@Nonnull
		@Override
		public Cluster getObject() {
			ApplicationContext applicationContext = this.applicationContext;
			TestCassandra cassandra = BeanFactoryUtils.getIfUnique(applicationContext, TestCassandra.class);
			if (cassandra == null) {
				throw new IllegalStateException(String.format("'%s' bean requires a %s' bean",
						getClass().getName(), TestCassandra.class));
			}
			return cassandra.getCluster();
		}

		@Nonnull
		@Override
		public Class<?> getObjectType() {
			return Cluster.class;
		}

		@Override
		public boolean isSingleton() {
			return true;
		}
	}

}
