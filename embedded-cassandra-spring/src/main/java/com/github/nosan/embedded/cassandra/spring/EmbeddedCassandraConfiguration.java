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

import java.nio.charset.Charset;
import java.util.Arrays;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.github.nosan.embedded.cassandra.ClusterFactory;
import com.github.nosan.embedded.cassandra.ExecutableConfig;
import com.github.nosan.embedded.cassandra.cql.CqlResource;
import com.github.nosan.embedded.cassandra.cql.CqlResourceLoader;
import com.github.nosan.embedded.cassandra.cql.CqlScripts;
import com.github.nosan.embedded.cassandra.cql.DefaultCqlResourceLoader;

/**
 * {@link Configuration Configuration} for {@link EmbeddedCassandra Embedded Cassandra}
 * support. Configuration overrides any existing {@link Cluster Cluster} beans with an
 * embedded {@link Cluster Cluster}.
 *
 * @author Dmytro Nosan
 */
@Configuration
@Order
class EmbeddedCassandraConfiguration implements InitializingBean {

	private static final String DEFAULT_BEAN_NAME = "cluster";

	private final Cluster cluster;

	private final EmbeddedCassandra annotation;

	EmbeddedCassandraConfiguration(Cluster cluster, EmbeddedCassandra annotation) {
		this.cluster = cluster;
		this.annotation = annotation;
	}

	@Bean
	public static EmbeddedClusterBeanFactoryPostProcessor embeddedClusterBeanFactoryPostProcessor() {
		return new EmbeddedClusterBeanFactoryPostProcessor();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		try (Session session = this.cluster.connect()) {
			String encoding = this.annotation.encoding();
			Charset charset = (!StringUtils.hasText(encoding) ? null : Charset.forName(encoding));
			CqlResourceLoader loader = new DefaultCqlResourceLoader(getClass().getClassLoader(), charset);
			CqlScripts.executeScripts(session, Arrays.stream(this.annotation.scripts())
					.map(loader::load).toArray(CqlResource[]::new));
		}
	}

	private static class EmbeddedClusterBeanFactoryPostProcessor
			implements BeanDefinitionRegistryPostProcessor, Ordered {

		@Override
		public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
				throws BeansException {
			Assert.isInstanceOf(ConfigurableListableBeanFactory.class, registry,
					"Embedded Cassandra Configuration can only be used with a ConfigurableListableBeanFactory");
			process(registry, (ConfigurableListableBeanFactory) registry);
		}

		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
				throws BeansException {
		}

		private void process(BeanDefinitionRegistry registry,
				ConfigurableListableBeanFactory beanFactory) {
			BeanDefinitionHolder holder = getClusterBeanDefinition(beanFactory);
			registry.registerBeanDefinition(holder.getBeanName(),
					holder.getBeanDefinition());
		}

		private BeanDefinitionHolder getClusterBeanDefinition(
				ConfigurableListableBeanFactory beanFactory) {
			String[] beanNames = beanFactory.getBeanNamesForType(Cluster.class);
			if (ObjectUtils.isEmpty(beanNames)) {
				return new BeanDefinitionHolder(createEmbeddedBeanDefinition(true),
						DEFAULT_BEAN_NAME);
			}
			if (beanNames.length == 1) {
				String beanName = beanNames[0];
				BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
				return new BeanDefinitionHolder(
						createEmbeddedBeanDefinition(beanDefinition.isPrimary()),
						beanName);
			}
			for (String beanName : beanNames) {
				BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
				if (beanDefinition.isPrimary()) {
					return new BeanDefinitionHolder(createEmbeddedBeanDefinition(true),
							beanName);
				}
			}
			return new BeanDefinitionHolder(createEmbeddedBeanDefinition(true),
					DEFAULT_BEAN_NAME);
		}

		private BeanDefinition createEmbeddedBeanDefinition(boolean primary) {
			BeanDefinition beanDefinition = new RootBeanDefinition(
					EmbeddedClusterFactoryBean.class);
			beanDefinition.setPrimary(primary);
			return beanDefinition;
		}

		@Override
		public int getOrder() {
			return Ordered.LOWEST_PRECEDENCE;
		}
	}

	private static class EmbeddedClusterFactoryBean
			implements FactoryBean<Cluster>, InitializingBean, DisposableBean {

		private final com.github.nosan.embedded.cassandra.EmbeddedCassandra cassandra;

		EmbeddedClusterFactoryBean(
				ObjectProvider<ClusterFactory> clusterFactory,
				ObjectProvider<ExecutableConfig> executableConfig,
				ObjectProvider<IRuntimeConfig> runtimeConfig) {
			this.cassandra = new com.github.nosan.embedded.cassandra.EmbeddedCassandra(runtimeConfig.getIfAvailable(),
					executableConfig.getIfAvailable(), clusterFactory.getIfAvailable());
		}

		@Override
		public void afterPropertiesSet() throws Exception {
			this.cassandra.start();
		}

		@Override
		public Cluster getObject() throws Exception {
			return this.cassandra.getCluster();
		}

		@Override
		public Class<?> getObjectType() {
			return Cluster.class;
		}

		@Override
		public boolean isSingleton() {
			return true;
		}

		@Override
		public void destroy() throws Exception {
			this.cassandra.stop();
		}

	}

}
