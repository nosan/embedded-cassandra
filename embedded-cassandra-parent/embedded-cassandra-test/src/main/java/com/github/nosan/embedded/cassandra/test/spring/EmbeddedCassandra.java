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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.datastax.driver.core.Cluster;
import org.apiguardian.api.API;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.test.annotation.DirtiesContext;

import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.test.ClusterFactory;
import com.github.nosan.embedded.cassandra.test.TestCassandra;

/**
 * Annotation that can be specified on a test class that runs Apache Cassandra based tests.
 * <p>The typical usage of this annotation is like:
 * <pre class="code">
 * &#064;RunWith(SpringRunner.class) //for JUnit4
 * &#064;EmbeddedCassandra
 * public class CassandraTests {
 * &#064;Autowired
 * private TestCassandra cassandra;
 * &#064;Autowired
 * private Cluster cluster; // `replace` attribute is Replace.ANY
 * }
 * </pre>
 * {@link TestCassandra} bean with a name <em>embeddedCassandra</em> will be registered as a <b>@Primary</b> bean.
 * <p>
 * <b>Note!</b> It is possible to define you own {@link ClusterFactory} or {@link CassandraFactory} bean(s) to control
 * {@link TestCassandra} instance.
 *
 * @author Dmytro Nosan
 * @see EmbeddedLocalCassandra
 * @see EmbeddedCassandraContextCustomizer
 * @see ClusterFactory
 * @see CassandraFactory
 * @see TestCassandra
 * @see DirtiesContext
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
@DirtiesContext
@API(since = "1.0.0", status = API.Status.STABLE)
public @interface EmbeddedCassandra {

	/**
	 * The paths to the CQL scripts to execute.
	 * <h3>Path Resource Semantics</h3>
	 * Each path will be interpreted as a Spring
	 * {@link Resource}. A plain path &mdash; for
	 * example, {@code "schema.cql"} &mdash; will be treated as a classpath resource that
	 * is <em>relative</em> to the package in which the test class is defined. A path
	 * starting with a slash will be treated as an <em>absolute</em> classpath resource,
	 * for example: {@code "/org/example/schema.cql"}. A path which references a URL
	 * (e.g., a path prefixed with
	 * {@code http:}, etc.) will be loaded using the specified resource protocol.
	 * <p>All resources will be loaded by {@link ResourcePatternResolver}.
	 * Resources which were loaded from a path with a {@code wildcard} (e.g. {@code *}) will be <b>sorted</b> by {@code
	 * Resource.getURL().toString()}.
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
	 * The encoding for the supplied CQL scripts, if different from the platform
	 * encoding.
	 * <p>An empty string denotes that the platform encoding should be used.
	 *
	 * @return CQL scripts encoding.
	 */
	String encoding() default "";

	/**
	 * Determines what type of existing {@link Cluster} beans can be replaced.
	 *
	 * @return the type of existing {@link Cluster} to replace
	 */
	Replace replace() default Replace.NONE;

	/**
	 * Register a shutdown hook with the JVM runtime, stops {@link TestCassandra} on JVM shutdown unless it has already
	 * been stopped at that time.
	 *
	 * @return The value of the {@code registerShutdownHook} attribute
	 * @since 1.2.8
	 */
	boolean registerShutdownHook() default true;

	/**
	 * What the {@link Cluster} should be replaced.
	 */
	enum Replace {

		/**
		 * Replaces any {@link Cluster} beans with an embedded <b>@Primary</b> {@link Cluster} bean with a name
		 * <em>embeddedCluster</em>.
		 */
		ANY,

		/**
		 * Don't replace {@link Cluster} beans.
		 */
		NONE

	}
}
