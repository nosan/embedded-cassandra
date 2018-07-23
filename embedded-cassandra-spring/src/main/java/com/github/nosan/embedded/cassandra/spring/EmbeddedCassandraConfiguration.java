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

import java.io.IOException;

import com.datastax.driver.core.Cluster;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.ClusterFactory;
import com.github.nosan.embedded.cassandra.ExecutableConfig;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.cql.CqlScriptUtils;

/**
 * {@link Configuration Configuration} for {@link EmbeddedCassandra Embedded Cassandra}
 * support. Configuration overrides any existing {@link Cluster Cluster} beans with an
 * embedded {@link Cluster Cluster}.
 *
 * @author Dmytro Nosan
 */
@Configuration
@Order
class EmbeddedCassandraConfiguration {

	static final String BEAN_NAME = "cluster";

	static final String TEST_CLASS = "com.github.nosan.embedded-cassandra.test-class";

	static final String SCRIPTS = "com.github.nosan.embedded-cassandra.scripts";

	static final String ENCODING = "com.github.nosan.embedded-cassandra.encoding";

	static final String STATEMENTS = "com.github.nosan.embedded-cassandra.statements";

	private static final Logger log = LoggerFactory.getLogger(EmbeddedCassandraConfiguration.class);


	@Bean
	public static EmbeddedClusterBeanFactoryPostProcessor embeddedClusterBeanFactoryPostProcessor() {
		return new EmbeddedClusterBeanFactoryPostProcessor();
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
			if (registry.containsBeanDefinition(holder.getBeanName())) {
				registry.removeBeanDefinition(holder.getBeanName());
			}
			registry.registerBeanDefinition(holder.getBeanName(), holder.getBeanDefinition());
		}

		private BeanDefinitionHolder getClusterBeanDefinition(
				ConfigurableListableBeanFactory beanFactory) {
			String[] beanNames = beanFactory.getBeanNamesForType(Cluster.class);


			if (beanNames.length == 1) {
				String beanName = beanNames[0];
				BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
				log.info("Replacing '{}' Cluster bean with {} embedded version",
						beanName, (!beanDefinition.isPrimary() ? "" : "a primary"));
				return new BeanDefinitionHolder(
						createEmbeddedBeanDefinition(beanDefinition.isPrimary()),
						beanName);
			}

			for (String beanName : beanNames) {
				BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
				if (beanDefinition.isPrimary()) {
					log.info("Replacing primary '{}' Cluster bean with a primary embedded version",
							beanName);
					return new BeanDefinitionHolder(createEmbeddedBeanDefinition(true),
							beanName);
				}
			}

			log.info("There is no Cluster beans. Embedded primary '{}' Cluster bean will be registered", BEAN_NAME);

			return new BeanDefinitionHolder(createEmbeddedBeanDefinition(true),
					BEAN_NAME);
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

		private final Cassandra cassandra;

		private final ApplicationContext context;

		EmbeddedClusterFactoryBean(
				ObjectProvider<ClusterFactory> clusterFactory,
				ObjectProvider<ExecutableConfig> executableConfig,
				ObjectProvider<IRuntimeConfig> runtimeConfig,
				ApplicationContext applicationContext) {
			this.context = applicationContext;
			this.cassandra = new Cassandra(runtimeConfig.getIfAvailable(), executableConfig.getIfAvailable(),
					clusterFactory.getIfAvailable());
		}

		@Override
		public void afterPropertiesSet() throws Exception {
			this.cassandra.start();
			CqlScriptUtils.executeScripts(this.cassandra.getSession(), getCqlScripts(this.context));
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


		private static CqlScript[] getCqlScripts(ApplicationContext context) throws IOException {
			Environment env = context.getEnvironment();
			String[] scripts = env.getProperty(SCRIPTS, String[].class, new String[0]);
			String[] statements = env.getProperty(STATEMENTS, String[].class, new String[0]);
			Class<?> testClass = env.getProperty(TEST_CLASS, Class.class);
			String encoding = env.getProperty(ENCODING);
			return SpringCqlUtils
					.getCqlScripts(context, new CqlConfig(testClass, scripts, statements, encoding));

		}

	}

}
