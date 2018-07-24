/*
 * Copyright 2012-2018 the original author or authors.
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * {@code @Cql} is used to annotate a test method to configure
 * CQL {@link #scripts} and {@link #statements} to be executed against a given
 * cluster during integration tests.
 * <p>Script execution is performed by the {@link CqlExecutionListener},
 * which is enabled by default.
 * <p>This annotation may be used as a <em>meta-annotation</em> to create custom
 * <em>composed annotations</em> with attribute overrides.
 *
 * @author Dmytro Nosan
 * @see CqlScripts
 * @see CqlExecutionListener
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Repeatable(CqlScripts.class)
public @interface Cql {

	/**
	 * Alias for {@link #scripts}.
	 * <p>
	 * This attribute may <strong>not</strong> be used in conjunction with
	 * {@link #scripts}, but it may be used instead of {@link #scripts}.
	 *
	 * @return CQL scripts.
	 * @see #scripts
	 */
	@AliasFor("scripts")
	String[] value() default {};

	/**
	 * The paths to the CQL scripts to execute.
	 * <p>
	 * This attribute may <strong>not</strong> be used in conjunction with {@link #value},
	 * but it may be used instead of {@link #value}.
	 * <h3>Path Resource Semantics</h3>
	 * <p>
	 * Each path will be interpreted as a Spring
	 * {@link org.springframework.core.io.Resource Resource}. A plain path &mdash; for
	 * example, {@code "schema.cql"} &mdash; will be treated as a classpath resource that
	 * is <em>relative</em> to the package in which the test class is defined. A path
	 * starting with a slash will be treated as an <em>absolute</em> classpath resource,
	 * for example: {@code "/org/example/schema.cql"}. A path which references a URL
	 * (e.g., a path prefixed with
	 * {@code http:}, etc.) will be loaded using the specified resource protocol. A path which contains
	 * <em>"&frasl;**&frasl;**.cql"</em>
	 * will be handled by
	 * {@link org.springframework.core.io.support.ResourcePatternResolver ResourcePatternResolver}.
	 *
	 * @return CQL Scripts
	 * @see #value
	 */
	@AliasFor("value")
	String[] scripts() default {};

	/**
	 * <em>Inlined CQL statements</em> to execute.
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
	 * The encoding for the supplied CQL scripts, if different from the platform
	 * encoding.
	 * <p>An empty string denotes that the platform encoding should be used.
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
	ExecutionPhase executionPhase() default ExecutionPhase.BEFORE_TEST_METHOD;


	/**
	 * The bean name of the {@link com.datastax.driver.core.Cluster} against which the
	 * scripts should be executed.
	 * <p>The name is only required if there is more than one bean of type
	 * {@code Cluster} in the test's {@code ApplicationContext}. If there
	 * is only one such bean, it is not necessary to specify a bean name.
	 * <p>Defaults to an empty string, requiring that one of the following is
	 * true:
	 * <ol>
	 * <li>There is only one bean of type {@code Cluster} in the test's
	 * {@code ApplicationContext}.</li>
	 * <li>There is a primary bean of type {@code Cluster} in the test's
	 * {@code ApplicationContext}.</li>
	 * <li>The {@code Cluster} bean to use is named {@code "cluster"}.</li>
	 * </ol>
	 *
	 * @return {@code Cluster} bean name.
	 */
	String cluster() default "";


	/**
	 * Enumeration of <em>phases</em> that dictate when CQL scripts are executed.
	 */
	enum ExecutionPhase {

		/**
		 * The configured CQL scripts and statements will be executed
		 * <em>before</em> the corresponding test method.
		 */
		BEFORE_TEST_METHOD,

		/**
		 * The configured CQL scripts and statements will be executed
		 * <em>after</em> the corresponding test method.
		 */
		AFTER_TEST_METHOD
	}

}
