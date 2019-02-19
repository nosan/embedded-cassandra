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
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
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
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;

import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;
import com.github.nosan.embedded.cassandra.local.artifact.ArtifactFactory;
import com.github.nosan.embedded.cassandra.local.artifact.RemoteArtifactFactory;
import com.github.nosan.embedded.cassandra.local.artifact.UrlFactory;
import com.github.nosan.embedded.cassandra.test.spring.EmbeddedLocalCassandra.Artifact;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * {@link ContextCustomizer} to add {@link LocalCassandraFactoryRegistrar}.
 *
 * @author Dmytro Nosan
 * @since 1.0.7
 */
class EmbeddedLocalCassandraContextCustomizer implements ContextCustomizer {

	@Override
	public void customizeContext(@Nonnull ConfigurableApplicationContext context,
			@Nonnull MergedContextConfiguration mergedConfig) {
		Class<?> testClass = mergedConfig.getTestClass();
		EmbeddedLocalCassandra annotation = AnnotatedElementUtils.findMergedAnnotation(testClass,
				EmbeddedLocalCassandra.class);
		if (annotation != null) {
			BeanDefinitionRegistry registry = BeanDefinitionUtils.getRegistry(context.getBeanFactory());
			BeanDefinitionUtils.registerBeanDefinition(registry, LocalCassandraFactoryRegistrar.class.getName(),
					BeanDefinitionBuilder.rootBeanDefinition(LocalCassandraFactoryRegistrar.class)
							.addConstructorArgValue(testClass)
							.addConstructorArgValue(annotation)
							.setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
							.getBeanDefinition());
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
	 * {@link BeanDefinitionRegistryPostProcessor} adds a {@link LocalCassandraFactoryBean}
	 * bean definition as a primary bean.
	 */
	static class LocalCassandraFactoryRegistrar implements BeanDefinitionRegistryPostProcessor, Ordered {

		@Nonnull
		private final Class<?> testClass;

		@Nonnull
		private final EmbeddedLocalCassandra annotation;

		LocalCassandraFactoryRegistrar(@Nonnull Class<?> testClass, @Nonnull EmbeddedLocalCassandra annotation) {
			this.testClass = testClass;
			this.annotation = annotation;
		}

		@Override
		public void postProcessBeanDefinitionRegistry(@Nonnull BeanDefinitionRegistry registry) throws BeansException {
			ConfigurableListableBeanFactory beanFactory = BeanDefinitionUtils.getBeanFactory(registry);
			for (String name : beanFactory.getBeanNamesForType(CassandraFactory.class)) {
				beanFactory.getBeanDefinition(name).setPrimary(false);
			}
			BeanDefinition definition = BeanDefinitionBuilder
					.rootBeanDefinition(LocalCassandraFactoryBean.class)
					.addConstructorArgValue(this.testClass)
					.addConstructorArgValue(this.annotation)
					.getBeanDefinition();
			definition.setPrimary(true);
			BeanDefinitionUtils.registerBeanDefinition(registry, LocalCassandraFactoryBean.class.getName(), definition);
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
	 * {@link FactoryBean} used to create and configure a {@link LocalCassandraFactory}.
	 */
	static class LocalCassandraFactoryBean
			implements FactoryBean<LocalCassandraFactory>, ApplicationContextAware {

		@Nonnull
		private final Class<?> testClass;

		@Nonnull
		private final EmbeddedLocalCassandra annotation;

		private ApplicationContext applicationContext;

		LocalCassandraFactoryBean(@Nonnull Class<?> testClass, @Nonnull EmbeddedLocalCassandra annotation) {
			this.testClass = testClass;
			this.annotation = annotation;
		}

		@Override
		public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
			this.applicationContext = applicationContext;
		}

		@Nonnull
		@Override
		public LocalCassandraFactory getObject() {
			ApplicationContext applicationContext = this.applicationContext;
			Environment environment = applicationContext.getEnvironment();
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
			List<String> jvmOptions = Arrays.stream(annotation.jvmOptions())
					.map(environment::resolvePlaceholders)
					.filter(StringUtils::hasText)
					.collect(Collectors.toList());

			LocalCassandraFactory factory = new LocalCassandraFactory();
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
			ArtifactFactory artifactFactory = BeanFactoryUtils.getIfUnique(applicationContext, ArtifactFactory.class);
			if (artifactFactory != null) {
				factory.setArtifactFactory(artifactFactory);
			}
			else {
				factory.setArtifactFactory(getArtifactFactory(environment, annotation.artifact()));
			}
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

		private static ArtifactFactory getArtifactFactory(Environment environment, Artifact annotation) {
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
}
