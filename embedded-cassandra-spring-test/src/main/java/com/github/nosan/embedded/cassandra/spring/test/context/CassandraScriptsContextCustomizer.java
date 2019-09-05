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

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SocketOptions;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.util.ClassUtils;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.commons.io.Resource;
import com.github.nosan.embedded.cassandra.commons.io.SpringResource;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.spring.test.CassandraScripts;
import com.github.nosan.embedded.cassandra.spring.test.CassandraScriptsExecutor;

/**
 * {@link ContextCustomizer} to support {@link CassandraScripts} annotation.
 *
 * @author Dmytro Nosan
 */
class CassandraScriptsContextCustomizer implements ContextCustomizer {

	private final Set<CassandraScripts> scripts;

	CassandraScriptsContextCustomizer(Set<CassandraScripts> scripts) {
		this.scripts = scripts;
	}

	@Override
	public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
		List<CqlScript> scripts = new ArrayList<>();
		for (CassandraScripts annotation : this.scripts) {
			Charset charset = Charset.forName(annotation.encoding());
			Resource[] resources = getResources(context, annotation);
			for (Resource resource : resources) {
				scripts.add(CqlScript.ofResource(charset, resource));
			}
		}
		if (!scripts.isEmpty()) {
			BeanDefinitionRegistry registry = getRegistry(context);
			GenericBeanDefinition bd = new GenericBeanDefinition();
			bd.setBeanClass(CassandraScriptsInitializer.class);
			bd.setLazyInit(false);
			bd.setScope(BeanDefinition.SCOPE_SINGLETON);
			bd.setInstanceSupplier(() -> new CassandraScriptsInitializer(context, scripts));
			registry.registerBeanDefinition(CassandraScriptsInitializer.class.getName(), bd);
		}
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}

		CassandraScriptsContextCustomizer that = (CassandraScriptsContextCustomizer) other;
		return this.scripts.equals(that.scripts);
	}

	@Override
	public int hashCode() {
		return this.scripts.hashCode();
	}

	private static BeanDefinitionRegistry getRegistry(ConfigurableApplicationContext applicationContext) {
		if (applicationContext instanceof BeanDefinitionRegistry) {
			return ((BeanDefinitionRegistry) applicationContext);
		}
		return ((BeanDefinitionRegistry) applicationContext.getBeanFactory());
	}

	private static Resource[] getResources(ConfigurableApplicationContext context, CassandraScripts annotation) {
		List<Resource> resources = new ArrayList<>();
		for (String script : annotation.scripts()) {
			resources.add(new SpringResource(context.getResource(script)));
		}
		return resources.toArray(new Resource[0]);
	}

	private static final class CassandraScriptsInitializer implements InitializingBean {

		private final ConfigurableApplicationContext context;

		private final List<CqlScript> scripts;

		private CassandraScriptsInitializer(ConfigurableApplicationContext context, List<CqlScript> scripts) {
			this.context = context;
			this.scripts = Collections.unmodifiableList(scripts);
		}

		@Override
		public void afterPropertiesSet() {
			Cassandra cassandra = this.context.getBean(Cassandra.class.getName(), Cassandra.class);
			getCassandraScriptsExecutor().execute(cassandra, this.scripts);
		}

		private CassandraScriptsExecutor getCassandraScriptsExecutor() {
			try {

				return this.context.getBean(CassandraScriptsExecutor.class);
			}
			catch (NoSuchBeanDefinitionException ex) {
				ClassLoader cl = getClass().getClassLoader();
				if (ClassUtils.isPresent("com.datastax.driver.core.Cluster", cl)) {
					return new ClusterCassandraScriptsExecutor();
				}
				else if (ClassUtils.isPresent("com.datastax.oss.driver.api.core.CqlSession", cl)) {
					return new CqlSessionCassandraScriptsExecutor();
				}
				throw ex;
			}
		}

		private static final class ClusterCassandraScriptsExecutor implements CassandraScriptsExecutor {

			@Override
			public void execute(Cassandra cassandra, List<? extends CqlScript> scripts) {
				try (Cluster cluster = Cluster.builder().addContactPoints(cassandra.getAddress()).withPort(
						cassandra.getPort()).withoutJMXReporting().withoutMetrics().withSocketOptions(
						new SocketOptions().setReadTimeoutMillis(30000).setConnectTimeoutMillis(30000)).build()) {
					Session session = cluster.connect();
					for (CqlScript script : scripts) {
						script.forEach(session::execute);
					}
				}
			}

		}

		private static final class CqlSessionCassandraScriptsExecutor implements CassandraScriptsExecutor {

			@Override
			public void execute(Cassandra cassandra, List<? extends CqlScript> scripts) {
				try (CqlSession session = CqlSession.builder().addContactPoint(
						new InetSocketAddress(cassandra.getAddress(), cassandra.getPort())).withConfigLoader(
						DriverConfigLoader.programmaticBuilder().withDuration(DefaultDriverOption.REQUEST_TIMEOUT,
								Duration.ofSeconds(30))
								.withDuration(DefaultDriverOption.CONNECTION_INIT_QUERY_TIMEOUT, Duration.ofSeconds(3))
								.build()).build()) {
					for (CqlScript script : scripts) {
						script.forEach(session::execute);
					}
				}
			}

		}

	}

}
