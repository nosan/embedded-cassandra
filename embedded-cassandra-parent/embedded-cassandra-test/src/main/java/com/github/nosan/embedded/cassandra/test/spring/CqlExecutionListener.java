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
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.datastax.driver.core.Session;
import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.util.ObjectUtils;

import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.cql.StaticCqlScript;
import com.github.nosan.embedded.cassandra.cql.UrlCqlScript;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;
import com.github.nosan.embedded.cassandra.test.spring.Cql.ExecutionPhase;
import com.github.nosan.embedded.cassandra.test.util.CqlSessionUtils;
import com.github.nosan.embedded.cassandra.test.util.SessionUtils;
import com.github.nosan.embedded.cassandra.util.ClassUtils;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * {@code TestExecutionListener} that provides support for executing CQL {@link Cql#scripts scripts} and {@link
 * Cql#statements statements} configured via the {@link Cql @Cql} annotation.
 * <p>Scripts and statements will be executed {@link #beforeTestMethod(TestContext) before}
 * or {@link #afterTestMethod(TestContext) after} execution of the corresponding {@link java.lang.reflect.Method test
 * method}, depending on the configured value of the {@link Cql#executionPhase executionPhase} flag.
 *
 * @author Dmytro Nosan
 * @see Cql
 * @see CqlGroup
 * @since 1.0.0
 */
public final class CqlExecutionListener extends AbstractTestExecutionListener {

	private static final String BEAN_NAME = "cassandraSession";

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
	public void afterTestMethod(TestContext testContext) {
		executeScripts(testContext, ExecutionPhase.AFTER_TEST_METHOD);
	}

	private void executeScripts(TestContext testContext, ExecutionPhase executionPhase) {
		Set<Cql> methodAnnotations = AnnotatedElementUtils
				.findMergedRepeatableAnnotations(testContext.getTestMethod(), Cql.class, CqlGroup.class);
		Set<Cql> classAnnotations = AnnotatedElementUtils
				.findMergedRepeatableAnnotations(testContext.getTestClass(), Cql.class, CqlGroup.class);

		if (executionPhase == ExecutionPhase.BEFORE_TEST_METHOD) {
			executeScripts(classAnnotations, executionPhase, testContext);
			executeScripts(methodAnnotations, executionPhase, testContext);
		}
		else if (executionPhase == ExecutionPhase.AFTER_TEST_METHOD) {
			executeScripts(methodAnnotations, executionPhase, testContext);
			executeScripts(classAnnotations, executionPhase, testContext);
		}
	}

	private void executeScripts(Set<Cql> cqlAnnotations, ExecutionPhase executionPhase,
			TestContext testContext) {
		ApplicationContext applicationContext = testContext.getApplicationContext();
		Environment environment = applicationContext.getEnvironment();
		for (Cql cql : cqlAnnotations) {
			if (executionPhase == cql.executionPhase()) {
				CqlScript[] scripts = getScripts(cql, testContext.getTestClass(), applicationContext);
				if (scripts.length > 0) {
					executeScripts(asSession(environment, cql::session), applicationContext, scripts);
				}
			}
		}
	}

	private void executeScripts(String name, ApplicationContext applicationContext, CqlScript... scripts) {
		ClassLoader classLoader = applicationContext.getClassLoader();
		if (ClassUtils.isPresent(CQL_SESSION_CLASS, classLoader)) {
			CqlSessionUtils.executeScripts(getSession(name, applicationContext, CqlSession.class), scripts);
		}
		else if (ClassUtils.isPresent(SESSION_CLASS, classLoader)) {
			SessionUtils.executeScripts(getSession(name, applicationContext, Session.class), scripts);
		}
		else {
			throw new IllegalStateException(String.format("There is no way to execute '%s'."
							+ " Both '%s' and '%s' classes are not present in the classpath.",
					Arrays.stream(scripts).map(String::valueOf).collect(Collectors.joining(",")),
					CQL_SESSION_CLASS, SESSION_CLASS));
		}
	}

	private CqlScript[] getScripts(Cql annotation, Class<?> testClass, ApplicationContext context) {
		List<CqlScript> scripts = new ArrayList<>();
		Environment environment = context.getEnvironment();
		for (URL url : ResourceUtils.getResources(context, testClass, asArray(environment, annotation::scripts))) {
			scripts.add(new UrlCqlScript(url, asCharset(environment, annotation::encoding)));
		}
		if (!ObjectUtils.isEmpty(annotation.statements())) {
			scripts.add(new StaticCqlScript(annotation.statements()));
		}
		return scripts.toArray(new CqlScript[0]);
	}

	private String asSession(Environment environment, Supplier<String> supplier) {
		return environment.resolvePlaceholders(supplier.get());
	}

	private String[] asArray(Environment environment, Supplier<String[]> arraySupplier) {
		return Arrays.stream(arraySupplier.get())
				.map(environment::resolvePlaceholders)
				.filter(StringUtils::hasText)
				.toArray(String[]::new);
	}

	@Nullable
	private Charset asCharset(Environment environment, Supplier<String> supplier) {
		String charset = environment.resolvePlaceholders(supplier.get());
		return StringUtils.hasText(charset) ? Charset.forName(charset) : null;
	}

	private <T> T getSession(String name, ApplicationContext applicationContext, Class<T> sessionClass) {
		if (StringUtils.hasText(name)) {
			return applicationContext.getBean(name, sessionClass);
		}
		T session = applicationContext.getBeanProvider(sessionClass).getIfUnique();
		if (session != null) {
			return session;
		}
		return applicationContext.getBean(BEAN_NAME, sessionClass);
	}

}
