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
import java.util.Objects;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.util.ObjectUtils;

import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.cql.StaticCqlScript;
import com.github.nosan.embedded.cassandra.cql.UrlCqlScript;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;
import com.github.nosan.embedded.cassandra.test.TestCassandra;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * {@link ContextCustomizer} used to create {@link EmbeddedCassandraFactoryBean}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
class EmbeddedCassandraContextCustomizer implements ContextCustomizer {

	@Override
	public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		if (beanFactory instanceof BeanDefinitionRegistry) {
			Class<?> testClass = mergedConfig.getTestClass();
			EmbeddedCassandra annotation = AnnotatedElementUtils
					.findMergedAnnotation(testClass, EmbeddedCassandra.class);
			if (annotation != null) {
				BeanDefinitionRegistry registry = ((BeanDefinitionRegistry) beanFactory);
				registry.registerBeanDefinition(EmbeddedCassandra.class.getName(),
						BeanDefinitionBuilder.rootBeanDefinition(EmbeddedCassandraFactoryBean.class)
								.addConstructorArgValue(annotation).addConstructorArgValue(testClass)
								.getBeanDefinition());
			}
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
	 * {@link FactoryBean} used to create and configure a {@link TestCassandra}.
	 */
	static class EmbeddedCassandraFactoryBean implements FactoryBean<TestCassandra>, InitializingBean,
			DisposableBean, ApplicationContextAware {

		private static final Logger log = LoggerFactory.getLogger(EmbeddedCassandraFactoryBean.class);

		private final EmbeddedCassandra annotation;

		private final Class<?> testClass;

		@Nullable
		private TestCassandra cassandra;

		@Nullable
		private ApplicationContext applicationContext;

		EmbeddedCassandraFactoryBean(EmbeddedCassandra annotation, Class<?> testClass) {
			this.annotation = annotation;
			this.testClass = testClass;
		}

		@Override
		public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
			this.applicationContext = applicationContext;
		}

		@Override
		public TestCassandra getObject() {
			return Objects.requireNonNull(this.cassandra, "Cassandra must not be null");
		}

		@Override
		public Class<?> getObjectType() {
			return TestCassandra.class;
		}

		@Override
		public void destroy() {
			TestCassandra cassandra = this.cassandra;
			if (cassandra != null) {
				cassandra.stop();
				this.cassandra = null;
			}
		}

		@Override
		public void afterPropertiesSet() {
			ApplicationContext context = getContext();
			TestCassandraFactory testCassandraFactory = context.getBeanProvider(TestCassandraFactory.class)
					.getIfUnique(() -> TestCassandra::new);
			CassandraFactory cassandraFactory = context.getBeanProvider(CassandraFactory.class)
					.getIfUnique(this::getCassandraFactory);
			context.getBeanProvider(EmbeddedCassandraFactoryCustomizer.class)
					.forEach(customizer -> customize(cassandraFactory, customizer));
			TestCassandra cassandra = testCassandraFactory.create(cassandraFactory, getScripts());
			this.cassandra = Objects.requireNonNull(cassandra, "Test Cassandra must not be null");
			cassandra.start();
		}

		@Override
		public boolean isSingleton() {
			return true;
		}

		@SuppressWarnings("unchecked")
		private void customize(CassandraFactory cassandraFactory, EmbeddedCassandraFactoryCustomizer customizer) {
			try {
				customizer.customize(cassandraFactory);
			}
			catch (ClassCastException ex) {
				if (log.isDebugEnabled()) {
					log.error(String.format("Factory customizer '%s' was not invoked due to the factory type mismatch",
							customizer), ex);
				}
			}
		}

		private CassandraFactory getCassandraFactory() {
			EmbeddedCassandra annotation = this.annotation;
			LocalCassandraFactory cassandraFactory = new LocalCassandraFactory();
			cassandraFactory.setVersion(asVersion(annotation::version));
			cassandraFactory.setConfigurationFile(asURL(annotation::configurationFile));
			cassandraFactory.setJvmOptions(asArray(annotation::jvmOptions));
			cassandraFactory.setJmxLocalPort(asPort(annotation::jmxLocalPort));
			cassandraFactory.setPort(asPort(annotation::port));
			cassandraFactory.setStoragePort(asPort(annotation::storagePort));
			cassandraFactory.setSslStoragePort(asPort(annotation::sslStoragePort));
			cassandraFactory.setRpcPort(asPort(annotation::rpcPort));
			return cassandraFactory;
		}

		private CqlScript[] getScripts() {
			EmbeddedCassandra annotation = this.annotation;
			Class<?> testClass = this.testClass;
			ApplicationContext context = getContext();
			List<CqlScript> scripts = new ArrayList<>();
			for (URL url : ResourceUtils.getResources(context, testClass, asArray(annotation::scripts))) {
				scripts.add(new UrlCqlScript(url, asCharset(annotation::encoding)));
			}
			if (!ObjectUtils.isEmpty(annotation.statements())) {
				scripts.add(new StaticCqlScript(Arrays.asList(annotation.statements())));
			}
			return scripts.toArray(new CqlScript[0]);
		}

		private String[] asArray(Supplier<String[]> arraySupplier) {
			Environment environment = getContext().getEnvironment();
			return Arrays.stream(arraySupplier.get())
					.map(environment::resolvePlaceholders)
					.filter(StringUtils::hasText)
					.toArray(String[]::new);
		}

		@Nullable
		private Version asVersion(Supplier<String> supplier) {
			Environment environment = getContext().getEnvironment();
			String version = environment.resolvePlaceholders(supplier.get());
			return StringUtils.hasText(version) ? Version.parse(version) : null;
		}

		@Nullable
		private Charset asCharset(Supplier<String> supplier) {
			Environment environment = getContext().getEnvironment();
			String charset = environment.resolvePlaceholders(supplier.get());
			return StringUtils.hasText(charset) ? Charset.forName(charset) : null;
		}

		@Nullable
		private Integer asPort(Supplier<String> supplier) {
			Environment environment = getContext().getEnvironment();
			String port = environment.resolvePlaceholders(supplier.get());
			return StringUtils.hasText(port) ? Integer.parseInt(port) : null;
		}

		@Nullable
		private URL asURL(Supplier<String> supplier) {
			Class<?> testClass = this.testClass;
			ApplicationContext context = getContext();
			Environment environment = context.getEnvironment();
			String location = environment.resolvePlaceholders(supplier.get());
			return StringUtils.hasText(location) ? ResourceUtils.getResource(context, testClass, location) : null;
		}

		private ApplicationContext getContext() {
			ApplicationContext context = this.applicationContext;
			Objects.requireNonNull(context, "Application Context must not be null");
			return context;
		}

	}

}
