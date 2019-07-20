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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.datastax.driver.core.Session;
import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.util.TestContextResourceUtils;

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

	private static final boolean CQL_SESSION_PRESENT = ClassUtils
			.isPresent(CQL_SESSION_CLASS, CqlExecutionListener.class.getClassLoader());

	private static final boolean SESSION_PRESENT = ClassUtils
			.isPresent(SESSION_CLASS, CqlExecutionListener.class.getClassLoader());

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

	private static void executeScripts(TestContext testContext, ExecutionPhase phase) {
		Set<Definition> definitions = new LinkedHashSet<>();
		Class<?> testClass = testContext.getTestClass();
		Method testMethod = testContext.getTestMethod();
		if (phase == ExecutionPhase.BEFORE_TEST_METHOD || phase == ExecutionPhase.BEFORE_TEST_EXECUTION) {
			definitions.addAll(findDefinitions(testClass, testClass, phase));
			definitions.addAll(findDefinitions(testMethod, testClass, phase));
		}
		else if (phase == ExecutionPhase.AFTER_TEST_METHOD || phase == ExecutionPhase.AFTER_TEST_EXECUTION) {
			definitions.addAll(findDefinitions(testMethod, testClass, phase));
			definitions.addAll(findDefinitions(testClass, testClass, phase));
		}
		executeScripts(definitions, testContext.getApplicationContext());
	}

	private static Set<Definition> findDefinitions(AnnotatedElement element, Class<?> testClass, ExecutionPhase phase) {
		return AnnotatedElementUtils.findMergedRepeatableAnnotations(element, Cql.class, CqlGroup.class)
				.stream().map(cql -> new Definition(testClass, cql))
				.filter(definition -> definition.hasPhase(phase))
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	private static void executeScripts(Set<Definition> definitions, ApplicationContext applicationContext) {
		for (Definition definition : definitions) {
			CqlScript[] scripts = getScripts(definition, applicationContext);
				if (scripts.length > 0) {
					executeScripts(getSession(definition.session(), applicationContext), scripts);
			}
		}
	}

	private static void executeScripts(@Nullable Object session, CqlScript... scripts) {
		if (session instanceof Connection) {
			((Connection) session).execute(scripts);
		}
		else if (CQL_SESSION_PRESENT && session instanceof CqlSession) {
			CqlSessionUtils.execute(((CqlSession) session), scripts);
		}
		else if (SESSION_PRESENT && session instanceof Session) {
			SessionUtils.execute(((Session) session), scripts);
		}
		else {
			throw new IllegalStateException(String.format("Failed to execute CQL scripts: '%s'. "
							+ "No one of types '%s', '%s', '%s' were found.",
					Arrays.stream(scripts).map(String::valueOf).collect(Collectors.joining(",")),
					CQL_SESSION_CLASS, SESSION_CLASS, Connection.class.getName()));
		}
	}

	@Nullable
	private static Object getSession(String name, ApplicationContext applicationContext) {
		return Optional.ofNullable(getAttribute(name, applicationContext.getEnvironment(), Function.identity()))
				.map(applicationContext::getBean).orElseGet(() -> getSession(applicationContext));
	}

	@Nullable
	private static Object getSession(ApplicationContext applicationContext) {
		Connection connection = BeanUtils.getUniqueBean(applicationContext, Connection.class).orElse(null);
		if (connection != null) {
			return connection;
		}
		if (CQL_SESSION_PRESENT) {
			CqlSession session = BeanUtils.getUniqueBean(applicationContext, CqlSession.class).orElse(null);
			if (session != null) {
				return session;
			}
		}
		if (SESSION_PRESENT) {
			return BeanUtils.getUniqueBean(applicationContext, Session.class).orElse(null);
		}
		return null;
	}

	private static CqlScript[] getScripts(Definition definition, ApplicationContext applicationContext) {
		List<CqlScript> scripts = new ArrayList<>();
		Environment environment = applicationContext.getEnvironment();
		Charset encoding = getAttribute(definition.encoding(), environment, Charset::forName);
		for (URL url : getScriptsAttribute(definition, applicationContext)) {
			scripts.add(new UrlCqlScript(url, encoding));
		}
		String[] statements = definition.statements();
		if (statements.length > 0) {
			scripts.add(new CqlStatements(statements));
		}
		return scripts.toArray(new CqlScript[0]);
	}

	private static URL[] getScriptsAttribute(Definition definition, ApplicationContext applicationContext) {
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

	private static class Definition {

		private final String[] scripts;

		private final String[] statements;

		private final String encoding;

		private final String session;

		private final Set<ExecutionPhase> executionPhases;

		private final Class<?> testClass;

		Definition(Class<?> testClass, Cql annotation) {
			this.scripts = Arrays.stream(annotation.scripts()).filter(StringUtils::hasText).map(String::trim)
					.distinct().toArray(String[]::new);
			this.statements = Arrays.stream(annotation.statements()).filter(StringUtils::hasText).map(String::trim)
					.distinct().toArray(String[]::new);
			this.encoding = annotation.encoding().trim();
			this.session = annotation.session().trim();
			this.executionPhases = Arrays.stream(annotation.executionPhase()).collect(Collectors.toSet());
			this.testClass = testClass;
		}

		@Override
		public int hashCode() {
			return hashCode(convertToResourcePath(this.testClass, this.scripts), this.statements, this.encoding,
					this.session);
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
					&& this.encoding.equals(that.encoding)
					&& this.session.equals(that.session);
		}

		boolean hasPhase(ExecutionPhase phase) {
			return this.executionPhases.contains(phase);
		}

		String[] scripts() {
			return this.scripts;
		}

		String[] statements() {
			return this.statements;
		}

		String encoding() {
			return this.encoding;
		}

		String session() {
			return this.session;
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
