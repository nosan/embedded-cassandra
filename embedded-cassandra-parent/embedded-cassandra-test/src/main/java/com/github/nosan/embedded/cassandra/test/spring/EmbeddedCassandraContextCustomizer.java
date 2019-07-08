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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;
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

	private final Class<?> testClass;

	private final AnnotationAttributes attributes;

	EmbeddedCassandraContextCustomizer(Class<?> testClass, AnnotationAttributes attributes) {
		this.testClass = testClass;
		this.attributes = attributes;
	}

	@Override
	public void customizeContext(ConfigurableApplicationContext applicationContext,
			MergedContextConfiguration mergedConfig) {
		BeanDefinitionRegistry registry = getRegistry(applicationContext);
		if (registry.containsBeanDefinition(BEAN_NAME)) {
			registry.removeBeanDefinition(BEAN_NAME);
		}
		BeanDefinition bd = getBeanDefinition(applicationContext);
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
		return this.attributes.equals(that.attributes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.attributes);
	}

	private BeanDefinitionRegistry getRegistry(ConfigurableApplicationContext applicationContext) {
		if (applicationContext instanceof BeanDefinitionRegistry) {
			return ((BeanDefinitionRegistry) applicationContext);
		}
		ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
		if (beanFactory instanceof BeanDefinitionRegistry) {
			return ((BeanDefinitionRegistry) beanFactory);
		}
		throw new IllegalStateException(String.format("'@%s' is not supported because "
						+ "'%s' is not found in the '%s'", EmbeddedCassandra.class.getName(),
				BeanDefinitionRegistry.class.getTypeName(),
				applicationContext));
	}

	private BeanDefinition getBeanDefinition(ConfigurableApplicationContext applicationContext) {
		GenericBeanDefinition bd = new GenericBeanDefinition();
		bd.setBeanClass(TestCassandra.class);
		bd.setInitMethodName("start");
		bd.setDestroyMethodName("stop");
		bd.setLazyInit(false);
		bd.setScope(BeanDefinition.SCOPE_SINGLETON);
		bd.setInstanceSupplier(() -> {
			TestCassandraFactory testCassandraFactory = getUniqueBean(applicationContext, TestCassandraFactory.class)
					.orElse(null);
			ConnectionFactory connectionFactory = getUniqueBean(applicationContext, ConnectionFactory.class).
					orElseGet(DefaultConnectionFactory::new);
			CassandraFactory cassandraFactory = getUniqueBean(applicationContext, CassandraFactory.class)
					.orElseGet(() -> getCassandraFactory(applicationContext));
			getBeans(applicationContext, CassandraFactoryCustomizer.class)
					.forEach(customizer -> customizeFactory(cassandraFactory, customizer));
			CqlScript[] scripts = getScripts(applicationContext);
			if (testCassandraFactory != null) {
				return create(testCassandraFactory, connectionFactory, cassandraFactory, scripts);
			}
			return new TestCassandra(cassandraFactory, connectionFactory, scripts);
		});
		return bd;
	}

	private TestCassandra create(TestCassandraFactory testCassandraFactory, ConnectionFactory connectionFactory,
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
	private void customizeFactory(CassandraFactory cassandraFactory, CassandraFactoryCustomizer customizer) {
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

	private CassandraFactory getCassandraFactory(ApplicationContext applicationContext) {
		Environment environment = applicationContext.getEnvironment();
		LocalCassandraFactory cassandraFactory = new LocalCassandraFactory();
		cassandraFactory.setVersion(getVersion(this.attributes.getString("version"), environment));
		cassandraFactory.setConfigurationFile(getURL(this.attributes.getString("configurationFile"),
				this.testClass, applicationContext));
		cassandraFactory.setJvmOptions(getArray(this.attributes.getStringArray("jvmOptions"), environment));
		cassandraFactory.setJmxLocalPort(getPort(this.attributes.getString("jmxLocalPort"), environment));
		cassandraFactory.setPort(getPort(this.attributes.getString("port"), environment));
		cassandraFactory.setStoragePort(getPort(this.attributes.getString("storagePort"), environment));
		cassandraFactory.setSslStoragePort(getPort(this.attributes.getString("sslStoragePort"), environment));
		cassandraFactory.setRpcPort(getPort(this.attributes.getString("rpcPort"), environment));
		return cassandraFactory;
	}

	private CqlScript[] getScripts(ApplicationContext applicationContext) {
		Environment environment = applicationContext.getEnvironment();
		List<CqlScript> scripts = new ArrayList<>();
		for (URL url : ResourceUtils.getResources(applicationContext, this.testClass,
				getArray(this.attributes.getStringArray("scripts"), environment))) {
			scripts.add(new UrlCqlScript(url, getCharset(this.attributes.getString("encoding"), environment)));
		}
		List<String> statements = getStatements(this.attributes.getStringArray("statements"));
		if (!statements.isEmpty()) {
			scripts.add(new CqlStatements(statements));
		}
		return scripts.toArray(new CqlScript[0]);
	}

	private List<String> getStatements(String[] statements) {
		return Arrays.stream(statements).filter(StringUtils::hasText).collect(Collectors.toList());
	}

	private String[] getArray(String[] values, Environment environment) {
		return Arrays.stream(values).map(environment::resolvePlaceholders).filter(StringUtils::hasText)
				.toArray(String[]::new);
	}

	@Nullable
	private Version getVersion(String value, Environment environment) {
		String version = environment.resolvePlaceholders(value);
		return StringUtils.hasText(version) ? Version.parse(version) : null;
	}

	@Nullable
	private Charset getCharset(String value, Environment environment) {
		String charset = environment.resolvePlaceholders(value);
		return StringUtils.hasText(charset) ? Charset.forName(charset) : null;
	}

	@Nullable
	private Integer getPort(String value, Environment environment) {
		String port = environment.resolvePlaceholders(value);
		return StringUtils.hasText(port) ? Integer.parseInt(port) : null;
	}

	@Nullable
	private URL getURL(String value, Class<?> testClass, ApplicationContext applicationContext) {
		Environment environment = applicationContext.getEnvironment();
		String location = environment.resolvePlaceholders(value);
		return StringUtils.hasText(location) ? ResourceUtils.getResource(applicationContext, testClass,
				location) : null;
	}

	private <T> Optional<T> getUniqueBean(ApplicationContext applicationContext, Class<T> beanClass) {
		ObjectProvider<T> beanProvider = getBeanProvider(applicationContext, beanClass);
		if (beanProvider != null) {
			return Optional.ofNullable(beanProvider.getIfUnique());
		}
		try {
			return Optional.of(applicationContext.getBean(beanClass));
		}
		catch (NoSuchBeanDefinitionException ex) {
			return Optional.empty();
		}
	}

	private <T> List<T> getBeans(ApplicationContext applicationContext, Class<T> beanClass) {
		ObjectProvider<T> beanProvider = getBeanProvider(applicationContext, beanClass);
		if (beanProvider != null) {
			return beanProvider.orderedStream().collect(Collectors.toList());
		}
		Map<String, T> beansOfType = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, beanClass);
		List<T> beans = new ArrayList<>(beansOfType.values());
		AnnotationAwareOrderComparator.sort(beans);
		return beans;
	}

	@Nullable
	private <T> ObjectProvider<T> getBeanProvider(ApplicationContext applicationContext, Class<T> beanClass) {
		try {
			return applicationContext.getBeanProvider(beanClass);
		}
		catch (NoSuchMethodError ex) {
			return null;
		}
	}

}
