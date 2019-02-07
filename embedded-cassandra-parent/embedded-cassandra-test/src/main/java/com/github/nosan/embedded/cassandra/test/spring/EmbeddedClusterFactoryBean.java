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
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.github.nosan.embedded.cassandra.test.TestCassandra;

/**
 * {@link FactoryBean} to create a {@link Cluster} bean.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
class EmbeddedClusterFactoryBean implements FactoryBean<Cluster>, ApplicationContextAware {

	static final String BEAN_NAME = "embeddedCluster";

	@Nullable
	private ApplicationContext context;

	@Override
	public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
		this.context = Objects.requireNonNull(applicationContext, "Context must not be null");
	}

	@Nonnull
	@Override
	public Cluster getObject() {
		ApplicationContext context = this.context;
		Objects.requireNonNull(context, "Context must not be null");
		TestCassandra cassandra = BeanFactoryUtils.getIfUnique(context, TestCassandra.class);
		if (cassandra == null) {
			throw new NoSuchBeanDefinitionException(TestCassandra.class);
		}
		return cassandra.getCluster();
	}

	@Nonnull
	@Override
	public Class<?> getObjectType() {
		return Cluster.class;
	}
}
