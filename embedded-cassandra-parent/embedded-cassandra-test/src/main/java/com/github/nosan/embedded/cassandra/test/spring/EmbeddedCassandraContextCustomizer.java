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

import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.cql.CqlStatements;
import com.github.nosan.embedded.cassandra.cql.UrlCqlScript;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;
import com.github.nosan.embedded.cassandra.test.TestCassandra;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * {@link ContextCustomizer} used to create a {@link TestCassandra} bean.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
class EmbeddedCassandraContextCustomizer implements ContextCustomizer {

	private static final Logger log = LoggerFactory.getLogger(EmbeddedCassandraContextCustomizer.class);

	private final Class<?> testClass;

	private final EmbeddedCassandra annotation;

	EmbeddedCassandraContextCustomizer(Class<?> testClass, EmbeddedCassandra annotation) {
		this.testClass = testClass;
		this.annotation = annotation;
	}

	@Override
	public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			BeanDefinition bd = getBeanDefinition(this.testClass, this.annotation, context);
			registry.registerBeanDefinition(TestCassandra.class.getName(), bd);
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

	private static BeanDefinition getBeanDefinition(Class<?> testClass, EmbeddedCassandra annotation,
			ConfigurableApplicationContext context) {
		GenericBeanDefinition bd = new GenericBeanDefinition();
		bd.setBeanClass(TestCassandra.class);
		bd.setInitMethodName("start");
		bd.setDestroyMethodName("stop");
		bd.setInstanceSupplier(() -> {
			TestCassandraFactory testCassandraFactory = context.getBeanProvider(TestCassandraFactory.class)
					.getIfUnique(() -> TestCassandra::new);
			CassandraFactory cassandraFactory = context.getBeanProvider(CassandraFactory.class)
					.getIfUnique(() -> getCassandraFactory(testClass, annotation, context));
			context.getBeanProvider(EmbeddedCassandraFactoryCustomizer.class).orderedStream()
					.forEach(customizer -> customize(cassandraFactory, customizer));
			return testCassandraFactory.create(cassandraFactory, getScripts(testClass, annotation, context));
		});
		return bd;
	}

	@SuppressWarnings("unchecked")
	private static void customize(CassandraFactory cassandraFactory, EmbeddedCassandraFactoryCustomizer customizer) {
		try {
			customizer.customize(cassandraFactory);
		}
		catch (ClassCastException ex) {
			String message = ex.getMessage();
			if (message == null || message.contains(cassandraFactory.getClass().getName())) {
				if (log.isDebugEnabled()) {
					log.error(String.format("Factory customizer '%s' was not invoked due to the factory type mismatch.",
							customizer), ex);
				}
				return;
			}
			throw ex;
		}
	}

	private static CassandraFactory getCassandraFactory(Class<?> testClass, EmbeddedCassandra annotation,
			ApplicationContext context) {
		LocalCassandraFactory cassandraFactory = new LocalCassandraFactory();
		cassandraFactory.setVersion(getVersion(annotation.version(), context));
		cassandraFactory.setConfigurationFile(getURL(annotation.configurationFile(), testClass, context));
		cassandraFactory.setJvmOptions(getArray(annotation.jvmOptions(), context));
		cassandraFactory.setJmxLocalPort(getPort(annotation.jmxLocalPort(), context));
		cassandraFactory.setPort(getPort(annotation.port(), context));
		cassandraFactory.setStoragePort(getPort(annotation.storagePort(), context));
		cassandraFactory.setSslStoragePort(getPort(annotation.sslStoragePort(), context));
		cassandraFactory.setRpcPort(getPort(annotation.rpcPort(), context));
		return cassandraFactory;
	}

	private static CqlScript[] getScripts(Class<?> testClass, EmbeddedCassandra annotation,
			ApplicationContext context) {
		List<CqlScript> scripts = new ArrayList<>();
		for (URL url : ResourceUtils.getResources(context, testClass,
				getArray(annotation.scripts(), context))) {
			scripts.add(new UrlCqlScript(url, getCharset(annotation.encoding(), context)));
		}
		List<String> statements = getStatements(annotation.statements());
		if (!statements.isEmpty()) {
			scripts.add(new CqlStatements(statements));
		}
		return scripts.toArray(new CqlScript[0]);
	}

	private static List<String> getStatements(String[] statements) {
		return Arrays.stream(statements).filter(StringUtils::hasText).collect(Collectors.toList());
	}

	private static String[] getArray(String[] values, ApplicationContext context) {
		Environment environment = context.getEnvironment();
		return Arrays.stream(values).map(environment::resolvePlaceholders).filter(StringUtils::hasText)
				.toArray(String[]::new);
	}

	@Nullable
	private static Version getVersion(String value, ApplicationContext context) {
		Environment environment = context.getEnvironment();
		String version = environment.resolvePlaceholders(value);
		return StringUtils.hasText(version) ? Version.parse(version) : null;
	}

	@Nullable
	private static Charset getCharset(String value, ApplicationContext context) {
		Environment environment = context.getEnvironment();
		String charset = environment.resolvePlaceholders(value);
		return StringUtils.hasText(charset) ? Charset.forName(charset) : null;
	}

	@Nullable
	private static Integer getPort(String value, ApplicationContext context) {
		Environment environment = context.getEnvironment();
		String port = environment.resolvePlaceholders(value);
		return StringUtils.hasText(port) ? Integer.parseInt(port) : null;
	}

	@Nullable
	private static URL getURL(String value, Class<?> testClass, ApplicationContext context) {
		Environment environment = context.getEnvironment();
		String location = environment.resolvePlaceholders(value);
		return StringUtils.hasText(location) ? ResourceUtils.getResource(context, testClass, location) : null;
	}

}
