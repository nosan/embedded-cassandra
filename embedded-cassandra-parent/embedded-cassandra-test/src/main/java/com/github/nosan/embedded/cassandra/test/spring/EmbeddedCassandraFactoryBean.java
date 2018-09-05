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

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;
import com.github.nosan.embedded.cassandra.test.ClusterFactory;
import com.github.nosan.embedded.cassandra.test.DefaultClusterFactory;
import com.github.nosan.embedded.cassandra.test.TestCassandra;

/**
 * {@link FactoryBean} to create a {@link TestCassandra} bean.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
class EmbeddedCassandraFactoryBean implements FactoryBean<TestCassandra>,
		DisposableBean, ApplicationContextAware, InitializingBean {

	static final String EMBEDDED_CASSANDRA_NAME = "embeddedCassandra";

	@Nonnull
	private final CqlScript[] scripts;

	@Nullable
	private TestCassandra cassandra;

	@Nullable
	private ApplicationContext context;


	EmbeddedCassandraFactoryBean(@Nullable CqlScript[] scripts) {
		this.scripts = (scripts != null) ? scripts : new CqlScript[0];
	}


	@Override
	public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
		this.context = Objects.requireNonNull(applicationContext, "Context must not be null");
	}

	@Nonnull
	@Override
	public TestCassandra getObject() {
		return Objects.requireNonNull(this.cassandra, "Cassandra is not initialized.");
	}

	@Nonnull
	@Override
	public Class<?> getObjectType() {
		return TestCassandra.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void destroy() {
		Cassandra cassandra = this.cassandra;
		if (cassandra != null) {
			cassandra.stop();
		}
	}

	@Override
	public void afterPropertiesSet() {
		CassandraFactory cassandraFactory = getBean(this.context, CassandraFactory.class);
		if (cassandraFactory == null) {
			cassandraFactory = new LocalCassandraFactory();
		}
		ClusterFactory clusterFactory = getBean(this.context, ClusterFactory.class);
		if (clusterFactory == null) {
			clusterFactory = new DefaultClusterFactory();
		}
		this.cassandra = new TestCassandra(cassandraFactory, clusterFactory, this.scripts);
		this.cassandra.start();
	}


	@Nullable
	private static <T> T getBean(@Nullable ApplicationContext context, @Nonnull Class<T> targetClass) {
		if (context != null) {
			if (context.getBeanNamesForType(targetClass).length > 0) {
				return context.getBean(targetClass);
			}
		}
		return null;
	}

}
