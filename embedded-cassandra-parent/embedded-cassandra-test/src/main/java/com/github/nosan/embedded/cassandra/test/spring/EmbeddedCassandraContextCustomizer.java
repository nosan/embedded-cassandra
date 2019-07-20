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

package com.github.nosan.embedded.cassandra.test.spring;

import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.util.TestContextResourceUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.cql.CqlStatements;
import com.github.nosan.embedded.cassandra.cql.UrlCqlScript;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;
import com.github.nosan.embedded.cassandra.test.ConnectionFactory;
import com.github.nosan.embedded.cassandra.test.DefaultConnectionFactory;
import com.github.nosan.embedded.cassandra.test.TestCassandra;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * {@link ContextCustomizer} used to create a {@link TestCassandra} bean.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
class EmbeddedCassandraContextCustomizer implements ContextCustomizer {

	private static final Logger log = LoggerFactory.getLogger(EmbeddedCassandraContextCustomizer.class);

	private static final String BEAN_NAME = TestCassandra.class.getName();

	private final Definition definition;

	EmbeddedCassandraContextCustomizer(Class<?> testClass, EmbeddedCassandra annotation) {
		this.definition = new Definition(testClass, annotation);
	}

	@Override
	public void customizeContext(ConfigurableApplicationContext applicationContext,
			MergedContextConfiguration mergedConfig) {
		BeanDefinitionRegistry registry = getRegistry(applicationContext);
		if (registry.containsBeanDefinition(BEAN_NAME)) {
			registry.removeBeanDefinition(BEAN_NAME);
		}
		BeanDefinition bd = getBeanDefinition(new TestCassandraSupplier(applicationContext, this.definition));
		registry.registerBeanDefinition(BEAN_NAME, bd);
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		EmbeddedCassandraContextCustomizer that = (EmbeddedCassandraContextCustomizer) other;
		return this.definition.equals(that.definition);
	}

	@Override
	public int hashCode() {
		return this.definition.hashCode();
	}

	private static BeanDefinitionRegistry getRegistry(ConfigurableApplicationContext applicationContext) {
		if (applicationContext instanceof BeanDefinitionRegistry) {
			return ((BeanDefinitionRegistry) applicationContext);
		}
		ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
		if (beanFactory instanceof BeanDefinitionRegistry) {
			return ((BeanDefinitionRegistry) beanFactory);
		}
		throw new IllegalStateException(String.format("'%s' cannot be casted to '%s'",
				applicationContext, BeanDefinitionRegistry.class));
	}

	private static BeanDefinition getBeanDefinition(TestCassandraSupplier supplier) {
		GenericBeanDefinition bd = new GenericBeanDefinition();
		bd.setBeanClass(TestCassandra.class);
		bd.setInitMethodName("start");
		bd.setDestroyMethodName("stop");
		bd.setLazyInit(false);
		bd.setScope(BeanDefinition.SCOPE_SINGLETON);
		bd.setInstanceSupplier(supplier);
		return bd;
	}

	private static class TestCassandraSupplier implements Supplier<TestCassandra> {

		private final ApplicationContext applicationContext;

		private final Definition definition;

		TestCassandraSupplier(ApplicationContext applicationContext, Definition definition) {
			this.applicationContext = applicationContext;
			this.definition = definition;
		}

		@Override
		public TestCassandra get() {
			TestCassandraFactory testCassandraFactory = BeanUtils
					.getUniqueBean(this.applicationContext, TestCassandraFactory.class).orElse(null);
			ConnectionFactory connectionFactory = BeanUtils.getUniqueBean(this.applicationContext,
					ConnectionFactory.class).orElseGet(DefaultConnectionFactory::new);
			CassandraFactory cassandraFactory = BeanUtils.getUniqueBean(this.applicationContext, CassandraFactory.class)
					.orElseGet(() -> getCassandraFactory(this.applicationContext, this.definition));
			BeanUtils.getBeans(this.applicationContext, CassandraFactoryCustomizer.class)
					.forEach(customizer -> customizeFactory(cassandraFactory, customizer));
			CqlScript[] scripts = getScripts(this.applicationContext, this.definition);
			if (testCassandraFactory != null) {
				return create(testCassandraFactory, connectionFactory, cassandraFactory, scripts);
			}
			return new TestCassandra(cassandraFactory, connectionFactory, scripts);
		}

		private static TestCassandra create(TestCassandraFactory testCassandraFactory,
				ConnectionFactory connectionFactory,
				CassandraFactory cassandraFactory, CqlScript[] scripts) {
			TestCassandra testCassandra = testCassandraFactory.create(cassandraFactory, scripts);
			Assert.state(testCassandra != null, "TestCassandra must not be null");
			Field connectionFactoryField = ReflectionUtils.findField(testCassandra.getClass(), "connectionFactory");
			Assert.state(connectionFactoryField != null, "'connectionFactory' field does not exist.");
			ReflectionUtils.makeAccessible(connectionFactoryField);
			ReflectionUtils.setField(connectionFactoryField, testCassandra, connectionFactory);
			return testCassandra;
		}

		@SuppressWarnings("unchecked")
		private static void customizeFactory(CassandraFactory cassandraFactory, CassandraFactoryCustomizer customizer) {
			try {
				customizer.customize(cassandraFactory);
			}
			catch (ClassCastException ex) {
				String message = ex.getMessage();
				if (message == null || !message.contains(cassandraFactory.getClass().getName())) {
					throw ex;
				}
				if (log.isDebugEnabled()) {
					log.error(String.format("'%s' can not customize '%s' due to type mismatch.", customizer,
							cassandraFactory), ex);
				}
			}
		}

		private static CassandraFactory getCassandraFactory(ApplicationContext applicationContext,
				Definition definition) {
			Environment environment = applicationContext.getEnvironment();
			LocalCassandraFactory cassandraFactory = new LocalCassandraFactory();
			cassandraFactory.setVersion(getAttribute(definition.version(), environment, Version::parse));
			cassandraFactory.setConfigurationFile(getAttribute(definition.configurationFile(), environment,
					location -> ResourceUtils.getResource(applicationContext, definition.getTestClass(), location)));
			cassandraFactory.setJvmOptions(getJvmOptionsAttribute(definition, environment));
			cassandraFactory.setJmxLocalPort(getAttribute(definition.jmxLocalPort(), environment, Integer::parseInt));
			cassandraFactory.setPort(getAttribute(definition.port(), environment, Integer::parseInt));
			cassandraFactory.setStoragePort(getAttribute(definition.storagePort(), environment, Integer::parseInt));
			cassandraFactory.setSslStoragePort(getAttribute(definition.sslStoragePort(), environment,
					Integer::parseInt));
			cassandraFactory.setRpcPort(getAttribute(definition.rpcPort(), environment, Integer::parseInt));
			return cassandraFactory;
		}

		private static CqlScript[] getScripts(ApplicationContext applicationContext, Definition definition) {
			List<CqlScript> scripts = new ArrayList<>();
			Charset encoding = getAttribute(definition.encoding(), applicationContext.getEnvironment(),
					Charset::forName);
			for (URL url : getScriptsAttribute(applicationContext, definition)) {
				scripts.add(new UrlCqlScript(url, encoding));
			}
			String[] statements = definition.statements();
			if (statements.length > 0) {
				scripts.add(new CqlStatements(statements));
			}
			return scripts.toArray(new CqlScript[0]);
		}

		private static String[] getJvmOptionsAttribute(Definition definition, Environment environment) {
			return Arrays.stream(definition.jvmOptions())
					.map(jvmOption -> getAttribute(jvmOption, environment, Function.identity()))
					.filter(Objects::nonNull).toArray(String[]::new);
		}

		private static URL[] getScriptsAttribute(ApplicationContext applicationContext, Definition definition) {
			Environment environment = applicationContext.getEnvironment();
			return ResourceUtils.getResources(applicationContext, definition.getTestClass(),
					Arrays.stream(definition.scripts())
							.map(script -> getAttribute(script, environment, Function.identity()))
							.filter(Objects::nonNull).toArray(String[]::new));
		}

		@Nullable
		private static <T> T getAttribute(String source, Environment environment, Function<String, T> mapper) {
			return Optional.of(source).map(environment::resolvePlaceholders).filter(StringUtils::hasText)
					.map(mapper).orElse(null);
		}

	}

	private static class Definition {

		private final String[] scripts;

		private final String[] statements;

		private final String[] jvmOptions;

		private final String encoding;

		private final String version;

		private final String configurationFile;

		private final String port;

		private final String rpcPort;

		private final String storagePort;

		private final String sslStoragePort;

		private final String jmxLocalPort;

		private final Class<?> testClass;

		Definition(Class<?> testClass, EmbeddedCassandra annotation) {
			this.scripts = Arrays.stream(annotation.scripts()).filter(StringUtils::hasText).map(String::trim)
					.distinct().toArray(String[]::new);
			this.jvmOptions = Arrays.stream(annotation.jvmOptions()).filter(StringUtils::hasText).map(String::trim)
					.distinct().toArray(String[]::new);
			this.statements = Arrays.stream(annotation.statements()).filter(StringUtils::hasText).map(String::trim)
					.distinct().toArray(String[]::new);
			this.configurationFile = annotation.configurationFile().trim();
			this.encoding = annotation.encoding().trim();
			this.version = annotation.version().trim();
			this.port = annotation.port().trim();
			this.rpcPort = annotation.rpcPort().trim();
			this.jmxLocalPort = annotation.jmxLocalPort().trim();
			this.storagePort = annotation.storagePort().trim();
			this.sslStoragePort = annotation.sslStoragePort().trim();
			this.testClass = testClass;
		}

		@Override
		public int hashCode() {
			return hashCode(convertToResourcePath(this.testClass, this.scripts), this.statements, this.jvmOptions,
					this.encoding, this.version, convertToResourcePath(this.testClass, this.configurationFile),
					this.port, this.rpcPort, this.storagePort, this.sslStoragePort, this.jmxLocalPort);
		}

		@Override
		public boolean equals(@Nullable Object other) {
			if (this == other) {
				return true;
			}
			if (other == null || getClass() != other.getClass()) {
				return false;
			}
			Definition that = (Definition) other;
			return Arrays.equals(convertToResourcePath(this.testClass, this.scripts),
					convertToResourcePath(that.testClass, that.scripts))
					&& Arrays.equals(this.statements, that.statements)
					&& Arrays.equals(this.jvmOptions, that.jvmOptions)
					&& this.encoding.equals(that.encoding)
					&& this.version.equals(that.version)
					&& Arrays.equals(convertToResourcePath(this.testClass, this.configurationFile),
					convertToResourcePath(that.testClass, that.configurationFile))
					&& this.port.equals(that.port)
					&& this.rpcPort.equals(that.rpcPort)
					&& this.storagePort.equals(that.storagePort)
					&& this.sslStoragePort.equals(that.sslStoragePort)
					&& this.jmxLocalPort.equals(that.jmxLocalPort);
		}

		String[] scripts() {
			return this.scripts;
		}

		String[] statements() {
			return this.statements;
		}

		String[] jvmOptions() {
			return this.jvmOptions;
		}

		String encoding() {
			return this.encoding;
		}

		String version() {
			return this.version;
		}

		String configurationFile() {
			return this.configurationFile;
		}

		String port() {
			return this.port;
		}

		String rpcPort() {
			return this.rpcPort;
		}

		String storagePort() {
			return this.storagePort;
		}

		String sslStoragePort() {
			return this.sslStoragePort;
		}

		String jmxLocalPort() {
			return this.jmxLocalPort;
		}

		Class<?> getTestClass() {
			return this.testClass;
		}

		private static int hashCode(Object... values) {
			return Arrays.deepHashCode(values);
		}

		private static String[] convertToResourcePath(Class<?> clazz, String... locations) {
			return TestContextResourceUtils.convertToClasspathResourcePaths(clazz, locations);
		}

	}

}
