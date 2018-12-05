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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.test.annotation.DirtiesContext;

import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.test.ClusterFactory;

/**
 * Annotation that can be specified on a test class that runs Apache Cassandra based tests.
 * <p>The typical usage of this annotation is like:
 * <pre class="code">
 * &#064;RunWith(SpringRunner.class)
 * &#064;EmbeddedCassandra
 * &#064;DirtiesContext
 * public class CassandraTests {
 * &#064;Autowired
 * private TestCassandra cassandra;
 * }
 * </pre>
 * <p>
 * <b>Note!</b> It is possible to define you own {@link ClusterFactory} or {@link CassandraFactory} beans.
 *
 * @author Dmytro Nosan
 * @see EmbeddedLocalCassandra
 * @see EmbeddedCassandraContextCustomizer
 * @see EmbeddedCassandraFactoryBean
 * @see EmbeddedClusterFactoryBean
 * @see ClusterFactory
 * @see CassandraFactory
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
@DirtiesContext
public @interface EmbeddedCassandra {

	/**
	 * The paths to the CQL scripts to execute.
	 * <h3>Path Resource Semantics</h3>
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
	 */
	String[] scripts() default {};

	/**
	 * <em>Inline CQL statements</em> to execute.
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
	 * Determines what type of existing {@code Cluster} beans can be replaced.
	 *
	 * @return the type of existing {@code Cluster} to replace
	 */
	Replace replace() default Replace.ANY;

	/**
	 * What the {@code Cluster} should be replaced.
	 */
	enum Replace {

		/**
		 * Replace any {@code Cluster} beans.
		 */
		ANY,

		/**
		 * Don't replace {@code Cluster} beans.
		 */
		NONE

	}

}
