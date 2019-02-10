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

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

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

	@Nonnull
	@Override
	public LocalCassandraFactory getObject() {
		ApplicationContext applicationContext = Objects.requireNonNull(this.context, "Context must not be null");
		Environment environment = applicationContext.getEnvironment();
		LocalCassandraFactory factory = new LocalCassandraFactory();
		EmbeddedLocalCassandra annotation = this.annotation;
		Class<?> testClass = this.testClass;
		String configurationFile = environment.resolvePlaceholders(annotation.configurationFile());
		String logbackFile = environment.resolvePlaceholders(annotation.logbackFile());
		String topologyFile = environment.resolvePlaceholders(annotation.topologyFile());
		String rackFile = environment.resolvePlaceholders(annotation.rackFile());
		String commitLogArchivingFile = environment.resolvePlaceholders(annotation.commitLogArchivingFile());
		String workingDirectory = environment.resolvePlaceholders(annotation.workingDirectory());
		String artifactDirectory = environment.resolvePlaceholders(annotation.artifactDirectory());
		String javaHome = environment.resolvePlaceholders(annotation.javaHome());
		String version = environment.resolvePlaceholders(annotation.version());
		Duration startupTimeout = Duration.ofMillis(annotation.startupTimeout());
		int jmxPort = annotation.jmxPort();
		boolean allowRoot = annotation.allowRoot();
		boolean registerShutdownHook = annotation.registerShutdownHook();
		EmbeddedLocalCassandra.Artifact artifact = annotation.artifact();
		List<String> jvmOptions = Arrays.stream(annotation.jvmOptions())
				.map(environment::resolvePlaceholders)
				.filter(StringUtils::hasText)
				.collect(Collectors.toList());

		if (StringUtils.hasText(workingDirectory)) {
			factory.setWorkingDirectory(Paths.get(workingDirectory));
		}
		if (StringUtils.hasText(artifactDirectory)) {
			factory.setArtifactDirectory(Paths.get(artifactDirectory));
		}
		if (StringUtils.hasText(javaHome)) {
			factory.setJavaHome(Paths.get(javaHome));
		}
		if (StringUtils.hasText(version)) {
			factory.setVersion(Version.parse(version));
		}
		if (StringUtils.hasText(configurationFile)) {
			factory.setConfigurationFile(CqlResourceUtils.getURL(applicationContext, configurationFile, testClass));
		}
		if (StringUtils.hasText(logbackFile)) {
			factory.setLogbackFile(CqlResourceUtils.getURL(applicationContext, logbackFile, testClass));
		}
		if (StringUtils.hasText(topologyFile)) {
			factory.setTopologyFile(CqlResourceUtils.getURL(applicationContext, topologyFile, testClass));
		}
		if (StringUtils.hasText(rackFile)) {
			factory.setRackFile(CqlResourceUtils.getURL(applicationContext, rackFile, testClass));
		}
		if (StringUtils.hasText(commitLogArchivingFile)) {
			factory.setCommitLogArchivingFile(CqlResourceUtils.getURL(applicationContext,
					commitLogArchivingFile, testClass));
		}
		factory.setStartupTimeout(startupTimeout);
		factory.getJvmOptions().addAll(jvmOptions);
		factory.setJmxPort(jmxPort);
		factory.setAllowRoot(allowRoot);
		factory.setRegisterShutdownHook(registerShutdownHook);
		factory.setArtifactFactory(getArtifactFactory(artifact, applicationContext));
		return factory;
	}

	@Nonnull
	@Override
	public Class<?> getObjectType() {
		return LocalCassandraFactory.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	private static ArtifactFactory getArtifactFactory(EmbeddedLocalCassandra.Artifact annotation,
			ApplicationContext context) {
		Environment environment = context.getEnvironment();
		ArtifactFactory artifactFactory = BeanFactoryUtils.getIfUnique(context, ArtifactFactory.class);
		if (artifactFactory != null) {
			return artifactFactory;
		}
		String directory = environment.resolvePlaceholders(annotation.directory());
		String proxyHost = environment.resolvePlaceholders(annotation.proxyHost());
		int proxyPort = annotation.proxyPort();
		Proxy.Type proxyType = annotation.proxyType();
		Class<? extends UrlFactory> urlFactory = annotation.urlFactory();
		Duration readTimeout = Duration.ofMillis(annotation.readTimeout());
		Duration connectTimeout = Duration.ofMillis(annotation.connectTimeout());

		RemoteArtifactFactory factory = new RemoteArtifactFactory();
		if (StringUtils.hasText(directory)) {
			factory.setDirectory(Paths.get(directory));
		}
		if (proxyType != Proxy.Type.DIRECT && StringUtils.hasText(proxyHost) && proxyPort != -1) {
			factory.setProxy(new Proxy(proxyType, new InetSocketAddress(proxyHost, proxyPort)));
		}
		if (!UrlFactory.class.equals(urlFactory)) {
			factory.setUrlFactory(BeanUtils.instantiateClass(urlFactory));
		}
		factory.setReadTimeout(readTimeout);
		factory.setConnectTimeout(connectTimeout);
		return factory;
	}
}
