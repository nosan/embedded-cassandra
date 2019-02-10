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

import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.apiguardian.api.API;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.util.StringUtils;

import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.test.util.CqlScriptUtils;

/**
 * {@code TestExecutionListener} that provides support for executing CQL
 * {@link Cql#scripts scripts} and {@link Cql#statements statements}
 * configured via the {@link Cql @Cql} annotation.
 * <p>Scripts and statements will be executed {@link #beforeTestMethod(TestContext) before}
 * or {@link #afterTestMethod(TestContext) after} execution of the corresponding
 * {@link java.lang.reflect.Method test method}, depending on the configured
 * value of the {@link Cql#executionPhase executionPhase} flag.
 *
 * @author Dmytro Nosan
 * @see Cql
 * @see CqlGroup
 * @since 1.0.0
 */
@API(since = "1.0.0", status = API.Status.STABLE)
public final class CqlExecutionListener extends AbstractTestExecutionListener {

	@Override
	public int getOrder() {
		return 5000;
	}

	@Override
	public void beforeTestMethod(@Nonnull TestContext testContext) {
		executeCqlScripts(testContext, Cql.ExecutionPhase.BEFORE_TEST_METHOD);
	}

	@Override
	public void afterTestMethod(@Nonnull TestContext testContext) {
		executeCqlScripts(testContext, Cql.ExecutionPhase.AFTER_TEST_METHOD);
	}

	private static void executeCqlScripts(TestContext testContext, Cql.ExecutionPhase executionPhase) {
		Objects.requireNonNull(testContext, "Test Context must not be null");
		Set<Cql> methodAnnotations = AnnotatedElementUtils.findMergedRepeatableAnnotations(
				testContext.getTestMethod(), Cql.class, CqlGroup.class);
		Set<Cql> classAnnotations = AnnotatedElementUtils.findMergedRepeatableAnnotations(
				testContext.getTestClass(), Cql.class, CqlGroup.class);

		if (executionPhase == Cql.ExecutionPhase.BEFORE_TEST_METHOD) {
			executeCqlScripts(classAnnotations, executionPhase, testContext);
			executeCqlScripts(methodAnnotations, executionPhase, testContext);
		}
		else if (executionPhase == Cql.ExecutionPhase.AFTER_TEST_METHOD) {
			executeCqlScripts(methodAnnotations, executionPhase, testContext);
			executeCqlScripts(classAnnotations, executionPhase, testContext);
		}
	}

	private static void executeCqlScripts(Set<Cql> cqlAnnotations, Cql.ExecutionPhase executionPhase,
			TestContext testContext) {
		for (Cql cql : cqlAnnotations) {
			if (executionPhase == cql.executionPhase()) {
				executeCqlScripts(testContext, cql);
			}
		}
	}

	private static void executeCqlScripts(TestContext testContext, Cql cql) {
		ApplicationContext applicationContext = testContext.getApplicationContext();
		Cluster cluster = getCluster(cql.cluster(), testContext);
		try (Session session = cluster.connect()) {
			CqlConfig config = new CqlConfig(testContext.getTestClass(), cql.scripts(),
					cql.statements(), cql.encoding());
			CqlScript[] scripts = CqlResourceUtils.getScripts(applicationContext, config);
			CqlScriptUtils.executeScripts(session, scripts);
		}
	}

	private static Cluster getCluster(String name, TestContext testContext) {
		ApplicationContext applicationContext = testContext.getApplicationContext();
		if (StringUtils.hasText(name)) {
			return applicationContext.getBean(name, Cluster.class);
		}
		Cluster cluster = BeanFactoryUtils.getIfUnique(applicationContext, Cluster.class);
		if (cluster == null) {
			return applicationContext.getBean("cluster", Cluster.class);
		}
		return cluster;
	}
}
