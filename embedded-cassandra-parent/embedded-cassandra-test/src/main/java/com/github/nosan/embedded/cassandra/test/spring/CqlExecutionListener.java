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
import java.util.Set;
import java.util.stream.Collectors;

import com.datastax.driver.core.Session;
import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.util.Assert;

import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.cql.CqlStatements;
import com.github.nosan.embedded.cassandra.cql.UrlCqlScript;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;
import com.github.nosan.embedded.cassandra.test.Connection;
import com.github.nosan.embedded.cassandra.test.spring.Cql.ExecutionPhase;
import com.github.nosan.embedded.cassandra.test.util.CqlSessionUtils;
import com.github.nosan.embedded.cassandra.test.util.SessionUtils;
import com.github.nosan.embedded.cassandra.util.ClassUtils;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * {@code TestExecutionListener} that provides support for executing CQL {@link Cql#scripts} and {@link
 * Cql#statements} configured via the {@link Cql} annotation.
 *
 * @author Dmytro Nosan
 * @see Cql
 * @see CqlGroup
 * @since 1.0.0
 */
public final class CqlExecutionListener extends AbstractTestExecutionListener {

	private static final String CQL_SESSION_CLASS = "com.datastax.oss.driver.api.core.CqlSession";

	private static final String SESSION_CLASS = "com.datastax.driver.core.Session";

	@Override
	public int getOrder() {
		return 5000;
	}

	@Override
	public void beforeTestMethod(TestContext testContext) {
		executeScripts(testContext, ExecutionPhase.BEFORE_TEST_METHOD);
	}

	@Override
	public void beforeTestExecution(TestContext testContext) {
		executeScripts(testContext, ExecutionPhase.BEFORE_TEST_EXECUTION);
	}

	@Override
	public void afterTestExecution(TestContext testContext) {
		executeScripts(testContext, ExecutionPhase.AFTER_TEST_EXECUTION);
	}

	@Override
	public void afterTestMethod(TestContext testContext) {
		executeScripts(testContext, ExecutionPhase.AFTER_TEST_METHOD);
	}

	private void executeScripts(TestContext testContext, ExecutionPhase phase) {
		Set<Cql> methodAnnotations = AnnotatedElementUtils
				.findMergedRepeatableAnnotations(testContext.getTestMethod(), Cql.class, CqlGroup.class);
		Set<Cql> classAnnotations = AnnotatedElementUtils
				.findMergedRepeatableAnnotations(testContext.getTestClass(), Cql.class, CqlGroup.class);

		if (phase == ExecutionPhase.BEFORE_TEST_METHOD || phase == ExecutionPhase.BEFORE_TEST_EXECUTION) {
			executeScripts(classAnnotations, phase, testContext);
			executeScripts(methodAnnotations, phase, testContext);
		}
		else if (phase == ExecutionPhase.AFTER_TEST_METHOD || phase == ExecutionPhase.AFTER_TEST_EXECUTION) {
			executeScripts(methodAnnotations, phase, testContext);
			executeScripts(classAnnotations, phase, testContext);
		}
	}

	private void executeScripts(Set<Cql> cqlAnnotations, ExecutionPhase phase, TestContext testContext) {
		ApplicationContext applicationContext = testContext.getApplicationContext();
		Environment environment = applicationContext.getEnvironment();
		for (Cql cql : cqlAnnotations) {
			List<ExecutionPhase> phases = Arrays.asList(cql.executionPhase());
			Assert.notEmpty(phases, "@Cql annotation does not have an execution phase");
			if (phases.contains(phase)) {
				CqlScript[] scripts = getScripts(cql, testContext.getTestClass(), applicationContext);
				if (scripts.length > 0) {
					String name = environment.resolvePlaceholders(cql.session());
					executeScripts(name, applicationContext, scripts);
				}
			}
		}
	}

	private void executeScripts(String name, ApplicationContext applicationContext, CqlScript... scripts) {
		ClassLoader cl = getClass().getClassLoader();
		Object session = getSession(name, applicationContext);
		if (session instanceof Connection) {
			((Connection) session).execute(scripts);
		}
		else if (ClassUtils.isPresent(CQL_SESSION_CLASS, cl) && session instanceof CqlSession) {
			CqlSessionUtils.execute(((CqlSession) session), scripts);
		}
		else if (ClassUtils.isPresent(SESSION_CLASS, cl) && session instanceof Session) {
			SessionUtils.execute(((Session) session), scripts);
		}
		else {
			throw new IllegalStateException(String.format("Failed to execute CQL scripts: '%s'. "
							+ "No one of types '%s', '%s', '%s' were found.",
					Arrays.stream(scripts).map(String::valueOf).collect(Collectors.joining(",")),
					CQL_SESSION_CLASS, SESSION_CLASS, Connection.class.getName()));
		}
	}

	private CqlScript[] getScripts(Cql annotation, Class<?> testClass, ApplicationContext applicationContext) {
		List<CqlScript> scripts = new ArrayList<>();
		Environment environment = applicationContext.getEnvironment();
		Charset charset = getCharset(environment, annotation.encoding());
		for (URL url : ResourceUtils.getResources(applicationContext, testClass,
				getArray(environment, annotation.scripts()))) {
			scripts.add(new UrlCqlScript(url, charset));
		}
		List<String> statements = getStatements(annotation.statements());
		if (!statements.isEmpty()) {
			scripts.add(new CqlStatements(statements));
		}
		return scripts.toArray(new CqlScript[0]);
	}

	@Nullable
	private Object getSession(String name, ApplicationContext applicationContext) {
		if (StringUtils.hasText(name)) {
			return applicationContext.getBean(name);
		}
		return getSession(applicationContext);
	}

	@Nullable
	private Object getSession(ApplicationContext applicationContext) {
		Connection connection = getUniqueBean(applicationContext, Connection.class);
		if (connection != null) {
			return connection;
		}
		ClassLoader cl = getClass().getClassLoader();
		if (ClassUtils.isPresent(CQL_SESSION_CLASS, cl)) {
			CqlSession session = getUniqueBean(applicationContext, CqlSession.class);
			if (session != null) {
				return session;
			}
		}
		if (ClassUtils.isPresent(SESSION_CLASS, cl)) {
			return getUniqueBean(applicationContext, Session.class);
		}
		return null;
	}

	@Nullable
	private <T> T getUniqueBean(ApplicationContext applicationContext, Class<T> beanClass) {
		ObjectProvider<T> beanProvider = getBeanProvider(applicationContext, beanClass);
		if (beanProvider != null) {
			return beanProvider.getIfUnique();
		}
		try {
			return applicationContext.getBean(beanClass);
		}
		catch (NoSuchBeanDefinitionException ex) {
			return null;
		}
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

	private String[] getArray(Environment environment, String[] values) {
		return Arrays.stream(values).map(environment::resolvePlaceholders).filter(StringUtils::hasText)
				.toArray(String[]::new);
	}

	private List<String> getStatements(String[] statements) {
		return Arrays.stream(statements).filter(StringUtils::hasText).collect(Collectors.toList());
	}

	@Nullable
	private Charset getCharset(Environment environment, String value) {
		String charset = environment.resolvePlaceholders(value);
		return StringUtils.hasText(charset) ? Charset.forName(charset) : null;
	}

}
