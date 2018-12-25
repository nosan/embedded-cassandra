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

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;
import com.github.nosan.embedded.cassandra.local.artifact.ArtifactFactory;
import com.github.nosan.embedded.cassandra.local.artifact.RemoteArtifactFactory;
import com.github.nosan.embedded.cassandra.local.artifact.UrlFactory;
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
	private final EmbeddedLocalCassandra annotation;

	@Nullable
	private ApplicationContext context;

	/**
	 * Creates a {@link LocalCassandraFactoryBean}.
	 *
	 * @param testClass test class
	 * @param annotation annotation
	 */
	LocalCassandraFactoryBean(@Nullable Class<?> testClass, @Nonnull EmbeddedLocalCassandra annotation) {
		this.testClass = testClass;
		this.annotation = Objects.requireNonNull(annotation, "@EmbeddedLocalCassandra must not be null");
	}

	@Override
	public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
		this.context = Objects.requireNonNull(applicationContext, "Application Context must not be null");
	}

	@Override
	@Nonnull
	public LocalCassandraFactory getObject() throws Exception {
		LocalCassandraFactory factory = new LocalCassandraFactory();
		EmbeddedLocalCassandra annotation = this.annotation;
		ApplicationContext context = Objects.requireNonNull(this.context, "Context must not be null");
		Class<?> testClass = this.testClass;
		factory.setConfigurationFile(CqlResourceUtils.getURL(context, testClass, annotation.configurationFile()));
		factory.setLogbackFile(CqlResourceUtils.getURL(context, testClass, annotation.logbackFile()));
		factory.setTopologyFile(CqlResourceUtils.getURL(context, testClass, annotation.topologyFile()));
		factory.setRackFile(CqlResourceUtils.getURL(context, testClass, annotation.rackFile()));
		factory.setCommitLogArchivingFile(
				CqlResourceUtils.getURL(context, testClass, annotation.commitLogArchivingFile()));
		if (StringUtils.hasText(annotation.workingDirectory())) {
			factory.setWorkingDirectory(Paths.get(annotation.workingDirectory()));
		}
		if (StringUtils.hasText(annotation.javaHome())) {
			factory.setJavaHome(Paths.get(annotation.javaHome()));
		}
		if (StringUtils.hasText(annotation.version())) {
			factory.setVersion(Version.parse(annotation.version()));
		}
		factory.setStartupTimeout(Duration.ofMillis(annotation.startupTimeout()));
		factory.getJvmOptions().addAll(Arrays.asList(annotation.jvmOptions()));
		factory.setJmxPort(annotation.jmxPort());
		factory.setAllowRoot(annotation.allowRoot());
		factory.setRegisterShutdownHook(annotation.registerShutdownHook());
		factory.setArtifactFactory(getArtifactFactory(annotation.artifact(), context));
		return factory;
	}

	@Override
	@Nonnull
	public Class<?> getObjectType() {
		return LocalCassandraFactory.class;
	}

	private static ArtifactFactory getArtifactFactory(EmbeddedLocalCassandra.Artifact annotation,
			ApplicationContext context) {
		ArtifactFactory artifactFactory = BeanFactoryUtils.getBean(context, ArtifactFactory.class);
		if (artifactFactory != null) {
			return artifactFactory;
		}
		RemoteArtifactFactory factory = new RemoteArtifactFactory();
		if (StringUtils.hasText(annotation.directory())) {
			factory.setDirectory(Paths.get(annotation.directory()));
		}
		if (annotation.proxyType() != Proxy.Type.DIRECT && StringUtils.hasText(annotation.proxyHost()) &&
				annotation.proxyPort() != -1) {
			factory.setProxy(new Proxy(annotation.proxyType(), new InetSocketAddress(annotation.proxyHost(),
					annotation.proxyPort())));
		}
		if (!UrlFactory.class.equals(annotation.urlFactory())) {
			factory.setUrlFactory(BeanUtils.instantiateClass(annotation.urlFactory()));
		}
		factory.setReadTimeout(Duration.ofMillis(annotation.readTimeout()));
		factory.setConnectTimeout(Duration.ofMillis(annotation.connectTimeout()));
		return factory;
	}

}
