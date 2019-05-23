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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * {@code @Cql} is used to annotate a test method to configure CQL {@link #scripts} and {@link #statements} to be
 * executed against a given {@code connection} during integration tests.
 * <p>Script execution is performed by the {@link CqlExecutionListener},
 * which is enabled by default.
 * <p>This annotation may be used as a <em>meta-annotation</em> to create custom
 * <em>composed annotations</em> with attribute overrides.
 *
 * @author Dmytro Nosan
 * @see CqlGroup
 * @see CqlExecutionListener
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Repeatable(CqlGroup.class)
public @interface Cql {

	/**
	 * The paths to the CQL scripts to execute.
	 * <h3>Path Resource Semantics</h3>
	 * <p>
	 * Each path will be interpreted as a Spring {@link Resource}. A plain path &mdash; for example, {@code
	 * "schema.cql"} &mdash; will be treated as a classpath resource that is <em>relative</em> to the package in which
	 * the test class is defined. A path starting with a slash will be treated as an <em>absolute</em> classpath
	 * resource, for example: {@code "/org/example/schema.cql"}. A path which references a URL (e.g., a path prefixed
	 * with {@code http:}, etc.) will be loaded using the specified resource protocol.
	 * <p>All resources will be loaded by {@link ResourcePatternResolver}.
	 * Resources which were loaded from a path with a {@code wildcard} (e.g. {@code *}) will be <b>sorted</b> by {@code
	 * Resource.getURL().toString()}.
	 * <p>
	 * The placeholder {@code ${...}} can be used
	 *
	 * @return CQL Scripts
	 */
	String[] scripts() default {};

	/**
	 * <em>CQL statements</em> to execute.
	 * <p>This attribute may be used in conjunction with or instead of
	 * {@link #scripts}.
	 * <h3>Ordering</h3>
	 * <p>Statements declared via this attribute will be executed after
	 * statements loaded from {@link #scripts}.
	 *
	 * @return CQL statements
	 * @see #scripts
	 */
	String[] statements() default {};

	/**
	 * The encoding for the supplied CQL scripts, if different from the platform encoding.
	 * <p>An empty string denotes that the platform encoding should be used.
	 * <p>
	 * The placeholder {@code ${...}} can be used
	 *
	 * @return CQL scripts encoding.
	 */
	String encoding() default "";

	/**
	 * When the CQL scripts and statements should be executed.
	 * <p>Defaults to {@link ExecutionPhase#BEFORE_TEST_METHOD BEFORE_TEST_METHOD}.
	 *
	 * @return Execution phase.
	 */
	ExecutionPhase[] executionPhase() default ExecutionPhase.BEFORE_TEST_METHOD;

	/**
	 * The bean name of the {@code connection} against which the scripts should be executed.
	 * <p>The name is only required if there is more than one bean of {@code connection} in the test's {@code
	 * ApplicationContext}. If there is only one such bean, it is not necessary to specify a bean name.
	 * <p>Defaults to an empty string, requiring that one of the following is
	 * true:
	 * <ol>
	 * <li>There is an unique bean of type {@code com.github.nosan.embedded.cassandra.test.Connection} in the test's
	 * {@code ApplicationContext}.</li>
	 * <li>There is an unique bean of type {@code com.datastax.oss.driver.api.core.CqlSession} in the test's
	 * {@code ApplicationContext}.</li>
	 * <li>There is an unique bean of type {@code com.datastax.driver.core.Session} in the test's
	 * {@code ApplicationContext}.</li>
	 * </ol>
	 * The placeholder {@code ${...}} can be used
	 *
	 * @return a bean name.
	 */
	String session() default "";

	/**
	 * Enumeration of <em>phases</em> that dictate when {@code CQL} scripts are executed.
	 */
	enum ExecutionPhase {

		/**
		 * The configured {@code CQL} scripts and statements will be executed
		 * <em>before</em> the test method.
		 *
		 * @see TestExecutionListener#beforeTestMethod(TestContext)
		 */
		BEFORE_TEST_METHOD,

		/**
		 * The configured {@code CQL} scripts and statements will be executed
		 * <em>before</em> the test execution.
		 *
		 * @see TestExecutionListener#beforeTestExecution(TestContext)
		 * @since 2.0.2
		 */
		BEFORE_TEST_EXECUTION,

		/**
		 * The configured {@code CQL} scripts and statements will be executed
		 * <em>after</em> the test execution.
		 *
		 * @see TestExecutionListener#afterTestExecution(TestContext)
		 * @since 2.0.2
		 */
		AFTER_TEST_EXECUTION,
		/**
		 * The configured {@code CQL} scripts and statements will be executed
		 * <em>after</em> the test method.
		 *
		 * @see TestExecutionListener#afterTestMethod(TestContext)
		 */
		AFTER_TEST_METHOD,

	}

}
