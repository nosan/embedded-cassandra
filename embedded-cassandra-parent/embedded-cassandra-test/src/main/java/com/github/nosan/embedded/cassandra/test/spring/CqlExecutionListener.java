/*
 * Copyright 2018-2018 the original author or authors.
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

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.test.util.CqlScriptUtils;

/**
 * {@code TestExecutionListener} that provides support for executing CQL
 * {@link Cql#scripts scripts} and inlined {@link Cql#statements statements}
 * configured via the {@link Cql @Cql} annotation.
 * <p>Scripts and inlined statements will be executed {@link #beforeTestMethod(TestContext) before}
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

	private static final Logger log = LoggerFactory.getLogger(CqlExecutionListener.class);

	@Override
	public int getOrder() {
		return 5000;
	}

	@Override
	public void beforeTestMethod(@Nonnull TestContext testContext) throws IOException {
		executeCqlScripts(testContext, Cql.ExecutionPhase.BEFORE_TEST_METHOD);
	}

	@Override
	public void afterTestMethod(@Nonnull TestContext testContext) throws IOException {
		executeCqlScripts(testContext, Cql.ExecutionPhase.AFTER_TEST_METHOD);
	}

	private void executeCqlScripts(TestContext testContext, Cql.ExecutionPhase executionPhase) throws IOException {
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

	private void executeCqlScripts(Set<Cql> cqlAnnotations, Cql.ExecutionPhase executionPhase,
			TestContext testContext) throws IOException {
		for (Cql cql : cqlAnnotations) {
			executeCqlScripts(cql, executionPhase, testContext);
		}
	}

	private void executeCqlScripts(Cql cql, Cql.ExecutionPhase executionPhase, TestContext testContext)
			throws IOException {
		if (executionPhase != cql.executionPhase()) {
			return;
		}
		ApplicationContext context = testContext.getApplicationContext();
		Cluster cluster = getCluster(cql.cluster(), testContext);
		Assert.state(cluster != null, () -> String.format("Failed to execute CQL scripts for a test context %s: " +
				"supply a '%s' bean", Cluster.class, testContext));
		Environment environment = context.getEnvironment();
		try (Session session = cluster.connect()) {
			CqlConfig config = new CqlConfig();
			config.setEncoding(environment.resolvePlaceholders(cql.encoding()));
			config.setScripts(Arrays.stream(cql.scripts())
					.map(environment::resolvePlaceholders).toArray(String[]::new));
			config.setStatements(Arrays.stream(cql.statements())
					.map(environment::resolvePlaceholders).toArray(String[]::new));
			config.setTestClass(testContext.getTestClass());
			CqlScript[] scripts = CqlResourceUtils.getScripts(context, config);
			CqlScriptUtils.executeScripts(session, scripts);
		}

	}

	private Cluster getCluster(String name, TestContext testContext) {
		BeanFactory bf = testContext.getApplicationContext().getAutowireCapableBeanFactory();
		try {
			if (StringUtils.hasText(name)) {
				return bf.getBean(name, Cluster.class);
			}
		}
		catch (BeansException ex) {
			log.error(String.format("Failed to retrieve '%s' named '%s' bean for a test context %s", name,
					Cluster.class, testContext), ex);
			return null;
		}
		try {
			if (bf instanceof ListableBeanFactory) {
				ListableBeanFactory lbf = (ListableBeanFactory) bf;
				Map<String, Cluster> clusters = BeanFactoryUtils.beansOfTypeIncludingAncestors(lbf, Cluster.class);
				if (clusters.size() == 1) {
					return clusters.values().iterator().next();
				}
			}
			try {
				return bf.getBean(Cluster.class);
			}
			catch (BeansException ex) {
				if (log.isDebugEnabled()) {
					log.debug(String.format("Failed to retrieve '%s' primary bean for a test context %s",
							Cluster.class, testContext), ex);
				}
			}
			return bf.getBean("cluster", Cluster.class);
		}
		catch (BeansException ex) {
			if (log.isDebugEnabled()) {
				log.debug(String.format("Failed to retrieve '%s' named 'cluster' bean for a test context %s",
						Cluster.class, testContext), ex);
			}
			return null;
		}
	}

}
