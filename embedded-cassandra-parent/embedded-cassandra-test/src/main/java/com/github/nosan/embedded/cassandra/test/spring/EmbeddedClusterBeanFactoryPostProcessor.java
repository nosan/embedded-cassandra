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

import javax.annotation.Nonnull;

import com.datastax.driver.core.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

/**
 * {@link BeanDefinitionRegistryPostProcessor} to register an embedded <b>primary</b> version of the {@link Cluster}
 * bean with a name {@code embeddedCluster}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
class EmbeddedClusterBeanFactoryPostProcessor implements BeanDefinitionRegistryPostProcessor, Ordered {

	static final String BEAN_NAME = "embeddedClusterBeanFactoryPostProcessor";

	private static final Logger log = LoggerFactory.getLogger(EmbeddedClusterBeanFactoryPostProcessor.class);

	@Override
	public void postProcessBeanDefinitionRegistry(@Nonnull BeanDefinitionRegistry registry) throws BeansException {
		Assert.isInstanceOf(ConfigurableListableBeanFactory.class, registry,
				String.format("(%s) can only be used with a ConfigurableListableBeanFactory", getClass()));
		ConfigurableListableBeanFactory factory = (ConfigurableListableBeanFactory) registry;
		for (String name : factory.getBeanNamesForType(Cluster.class)) {
			BeanDefinition bd = factory.getBeanDefinition(name);
			if (bd.isPrimary()) {
				bd.setPrimary(false);
				log.warn("Set 'primary = false' for a '{}' bean", name);
			}
		}
		if (registry.containsBeanDefinition(EmbeddedClusterFactoryBean.BEAN_NAME)) {
			registry.removeBeanDefinition(EmbeddedClusterFactoryBean.BEAN_NAME);
		}
		BeanDefinition bd = new RootBeanDefinition(EmbeddedClusterFactoryBean.class);
		bd.setPrimary(true);
		registry.registerBeanDefinition(EmbeddedClusterFactoryBean.BEAN_NAME, bd);
		log.info("The primary '{}' bean has been registered", EmbeddedClusterFactoryBean.BEAN_NAME);
	}

	@Override
	public void postProcessBeanFactory(@Nonnull ConfigurableListableBeanFactory beanFactory) throws BeansException {
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

}
