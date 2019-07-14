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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.test.annotation.DirtiesContext;

import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.test.ConnectionFactory;
import com.github.nosan.embedded.cassandra.test.TestCassandra;

/**
 * Annotation that can be specified on a test class that runs Apache Cassandra based tests.
 * <p>The typical usage of this annotation is like:
 * <pre class="code">
 * &#064;RunWith(SpringRunner.class)
 * &#064;EmbeddedCassandra
 * public class CassandraTests {
 * 	&#064;Test
 * 	void testMe(){
 *    }
 * }
 * </pre>
 * If you would like to override some properties which annotation does not have, {@link
 * CassandraFactoryCustomizer customizers}  can be used. Here is a quick example:
 * <pre class="code">
 * &#064;RunWith(SpringRunner.class)
 * &#064;ContextConfiguration
 * &#064;EmbeddedCassandra
 * class EmbeddedCassandraCustomizerTests {
 *    &#064;Test
 *    void testMe() {
 *    }
 *    &#064;Configuration
 *    static class TestConfiguration {
 *        &#064;Bean
 *        public CassandraFactoryCustomizer&lt;LocalCassandraFactory&gt; allowRootCustomizer() {
 * 			return factory -&gt; factory.setAllowRoot(true);
 *        }
 *    }
 * }
 * </pre>
 * Also, it is possible to define you own {@link CassandraFactory}, {@link ConnectionFactory} bean(s) to control
 * {@link TestCassandra} instance.
 * <p>
 * In case if you registered your own {@link CassandraFactory}, annotation attributes will be ignored.
 * <p>
 * This annotation is handled by {@link EmbeddedCassandraContextCustomizer}.
 *
 * @author Dmytro Nosan
 * @see CassandraFactory
 * @see ConnectionFactory
 * @see CassandraFactoryCustomizer
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
	 * Each path will be interpreted as a Spring {@link Resource}. A plain path &mdash; for example, {@code
	 * "schema.cql"} &mdash; will be treated as a classpath resource that is <em>relative</em> to the package in which
	 * the test class is defined. A path starting with a slash will be treated as an <em>absolute</em> classpath
	 * resource, for example: {@code "/org/example/schema.cql"}. A path which references a URL (e.g., a path prefixed
	 * with {@code http:}, etc.) will be loaded using the specified resource protocol.
	 * <p>All resources will be loaded by {@link ResourcePatternResolver}.
	 * Resources which were loaded from a path with a {@code wildcard} (e.g. {@code *}) will be <b>sorted</b> by {@code
	 * Resource.getURL().toString()}.
	 * <p>
	 * The placeholder {@code ${...}} can be used.
	 *
	 * @return CQL Scripts
	 */
	String[] scripts() default {};

	/**
	 * <em>CQL statements</em> to execute.
	 * <p>This attribute may be used in conjunction with or instead of {@link #scripts}.
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
	 * <p>
	 * The placeholder {@code ${...}} can be used
	 *
	 * @return CQL scripts encoding.
	 */
	String encoding() default "";

	/**
	 * {@link Version} to use.
	 * <p>
	 * The placeholder {@code ${...}} can be used
	 *
	 * @return a version, or {@code empty} to ignore
	 * @since 2.0.0
	 */
	String version() default "";

	/**
	 * Cassandra configuration file ({@code cassandra.yaml}).
	 * <p>
	 * Path will be interpreted as a Spring {@link Resource}. A plain path &mdash; for example,
	 * {@code "cassandra.yaml"} &mdash; will be treated as a classpath resource that is <em>relative</em> to the package
	 * in which the test class is defined. A path starting with a slash will be treated as an
	 * <em>absolute</em> classpath resource. A path which references a URL will be loaded using the specified
	 * resource protocol.
	 * <p>
	 * The placeholder {@code ${...}} can be used
	 *
	 * @return the configuration file, or {@code empty} to ignore
	 * @since 2.0.0
	 */
	String configurationFile() default "";

	/**
	 * The native transport port to listen for the clients on.
	 * This value will be added as {@code -Dcassandra.native_transport_port} system property.
	 * <p>
	 * The placeholder {@code ${...}} can be used
	 *
	 * @return native transport port, or {@code empty} to ignore
	 * @since 2.0.0
	 */
	String port() default "";

	/**
	 * Thrift port for client connections.
	 * This value will be added as {@code -Dcassandra.rpc_port} system property.
	 * <p>
	 * The placeholder {@code ${...}} can be used
	 *
	 * @return the thrift port, or {@code empty} to ignore
	 * @since 2.0.0
	 */
	String rpcPort() default "";

	/**
	 * The port for inter-node communication.
	 * This value will be added as {@code -Dcassandra.storage_port} system property.
	 * <p>
	 * The placeholder {@code ${...}} can be used
	 *
	 * @return storage port, or {@code empty} to ignore
	 * @since 2.0.0
	 */
	String storagePort() default "";

	/**
	 * The ssl port for inter-node communication.
	 * <p>
	 * This value will be added as {@code -Dcassandra.ssl_storage_port} system property.
	 * <p>
	 * The placeholder {@code ${...}} can be used
	 *
	 * @return storage ssl port, or {@code empty} to ignore
	 * @since 2.0.0
	 */
	String sslStoragePort() default "";

	/**
	 * JMX port to listen on.
	 * <p>
	 * This value will be added as {@code -Dcassandra.jmx.local.port} system property.
	 * <p>
	 * The placeholder {@code ${...}} can be used
	 *
	 * @return jmx local port, or {@code empty} to ignore
	 * @since 2.0.0
	 */
	String jmxLocalPort() default "";

	/**
	 * JVM options that should be associated with Cassandra.
	 * <p>
	 * These values will be added as {@code $JVM_EXTRA_OPTS} environment variable.
	 * <p>
	 * The placeholder {@code ${...}} can be used
	 *
	 * @return jvm options
	 * @since 2.0.0
	 */
	String[] jvmOptions() default {};

}
