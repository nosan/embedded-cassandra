/*
 * Copyright 2002-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra.spring;

import java.util.Map;
import java.util.Set;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.github.nosan.embedded.cassandra.cql.CqlScriptUtils;


/**
 * {@code TestExecutionListener} that provides support for executing CQL
 * {@link CqlScript#scripts scripts} and inlined {@link CqlScript#statements statements}
 * configured via the {@link CqlScript @CqlScript} annotation.
 * <p>Scripts and inlined statements will be executed {@linkplain #beforeTestMethod(TestContext) before}
 * or {@linkplain #afterTestMethod(TestContext) after} execution of the corresponding
 * {@linkplain java.lang.reflect.Method test method}, depending on the configured
 * value of the {@link CqlScript#executionPhase executionPhase} flag.
 *
 * @author Dmytro Nosan
 * @see CqlScript
 * @see CqlScripts
 */
class CqlScriptsTestExecutionListener extends AbstractTestExecutionListener {

	private static final Logger log = LoggerFactory.getLogger(CqlScriptsTestExecutionListener.class);


	/**
	 * Returns {@code 5000}.
	 */
	@Override
	public final int getOrder() {
		return 5000;
	}

	/**
	 * Execute CQL scripts configured via {@link CqlScript @CqlScript} for the supplied
	 * {@link TestContext} <em>before</em> the current test method.
	 */
	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		executeCqlScripts(testContext, CqlScript.ExecutionPhase.BEFORE_TEST_METHOD);
	}

	/**
	 * Execute CQL scripts configured via {@link CqlScript @CqlScript} for the supplied
	 * {@link TestContext} <em>after</em> the current test method.
	 */
	@Override
	public void afterTestMethod(TestContext testContext) throws Exception {
		executeCqlScripts(testContext, CqlScript.ExecutionPhase.AFTER_TEST_METHOD);
	}

	private void executeCqlScripts(TestContext testContext, CqlScript.ExecutionPhase executionPhase) throws Exception {
		Set<CqlScript> cqlScriptAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(
				testContext.getTestMethod(), CqlScript.class, CqlScripts.class);
		for (CqlScript cqlScript : cqlScriptAnnotations) {
			executeCqlScripts(cqlScript, executionPhase, testContext);
		}
	}

	private void executeCqlScripts(CqlScript cqlScript, CqlScript.ExecutionPhase executionPhase,
			TestContext testContext) throws Exception {
		if (executionPhase != cqlScript.executionPhase()) {
			return;
		}
		Cluster cluster = getCluster(cqlScript.cluster(), testContext);
		Assert.state(cluster != null, () -> String.format("Failed to execute CQL scripts for a test context %s: " +
				"supply a Cluster bean", testContext));

		try (Session session = cluster.connect()) {
			CqlScriptConfig config = new CqlScriptConfig(testContext.getTestClass(),
					cqlScript.scripts(), cqlScript.statements(), cqlScript.encoding());
			CqlScriptUtils.executeScripts(session,
					SpringCqlScriptUtils.getCqlScripts(testContext.getApplicationContext(), config));
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
			log.error(String.format("Failed to retrieve Cluster named '%s' bean for a test context %s",
					name, testContext), ex);
			throw ex;
		}

		try {
			if (bf instanceof ListableBeanFactory) {
				ListableBeanFactory lbf = (ListableBeanFactory) bf;
				Map<String, Cluster> clusters =
						BeanFactoryUtils.beansOfTypeIncludingAncestors(lbf, Cluster.class);
				if (clusters.size() == 1) {
					return clusters.values().iterator().next();
				}

				try {
					return bf.getBean(Cluster.class);
				}
				catch (BeansException ex) {
					log.debug(String.format("Failed to retrieve Cluster primary bean for a test context %s",
							testContext), ex);
				}
			}
			return bf.getBean(EmbeddedCassandraConfiguration.BEAN_NAME, Cluster.class);
		}
		catch (BeansException ex) {
			log.debug(String.format("Failed to retrieve Cluster named 'cluster' bean for a test context %s",
					testContext), ex);
			return null;
		}
	}


}
