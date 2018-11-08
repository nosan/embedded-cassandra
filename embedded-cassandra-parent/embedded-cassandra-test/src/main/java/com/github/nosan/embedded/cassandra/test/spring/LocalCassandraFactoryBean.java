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

import java.nio.file.Paths;
import java.time.Duration;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactoryBuilder;
import com.github.nosan.embedded.cassandra.local.artifact.ArtifactFactory;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * {@link FactoryBean} to create a {@link LocalCassandraFactory} bean.
 *
 * @author Dmytro Nosan
 * @since 1.0.7
 */
class LocalCassandraFactoryBean implements FactoryBean<LocalCassandraFactory>, ApplicationContextAware {

	static final String BEAN_NAME = "localCassandraFactory";

	@Nullable
	private final Class<?> testClass;

	@Nonnull
	private final LocalCassandra annotation;

	@Nullable
	private ApplicationContext context;


	/**
	 * Creates {@link LocalCassandraFactoryBean}.
	 *
	 * @param testClass test class
	 * @param annotation annotation
	 */
	LocalCassandraFactoryBean(@Nullable Class<?> testClass, @Nonnull LocalCassandra annotation) {
		this.testClass = testClass;
		this.annotation = Objects.requireNonNull(annotation, "@LocalFactory must not be null");
	}


	@Override
	public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
		this.context = Objects.requireNonNull(applicationContext, "Application Context must not be null");
	}


	@Override
	@Nonnull
	public LocalCassandraFactory getObject() throws Exception {
		LocalCassandraFactoryBuilder builder = new LocalCassandraFactoryBuilder();
		LocalCassandra annotation = this.annotation;
		ApplicationContext context = Objects.requireNonNull(this.context, "Context must not be null");
		Class<?> testClass = this.testClass;
		builder.setConfigurationFile(CqlResourceUtils.getURL(context, testClass, annotation.configurationFile()));
		builder.setLogbackFile(CqlResourceUtils.getURL(context, testClass, annotation.logbackFile()));
		builder.setTopologyFile(CqlResourceUtils.getURL(context, testClass, annotation.topologyFile()));
		builder.setRackFile(CqlResourceUtils.getURL(context, testClass, annotation.rackFile()));
		if (StringUtils.hasText(annotation.workingDirectory())) {
			builder.setWorkingDirectory(Paths.get(annotation.workingDirectory()));
		}
		if (StringUtils.hasText(annotation.version())) {
			builder.setVersion(Version.parse(annotation.version()));
		}
		builder.setStartupTimeout(Duration.ofMillis(annotation.startupTimeout()));
		builder.setJvmOptions(annotation.jvmOptions());
		builder.setArtifactFactory(getArtifactFactory(context));
		return builder.build();
	}

	@Override
	@Nonnull
	public Class<?> getObjectType() {
		return LocalCassandraFactory.class;
	}


	@Nullable
	private static ArtifactFactory getArtifactFactory(@Nullable ApplicationContext context) {
		if (context != null && context.getBeanNamesForType(ArtifactFactory.class).length > 0) {
			return context.getBean(ArtifactFactory.class);
		}
		return null;
	}
}
