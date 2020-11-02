/*
 * Copyright 2018-2020 the original author or authors.
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

package com.github.nosan.embedded.cassandra.spring.test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.CassandraFactory;
import com.github.nosan.embedded.cassandra.api.CassandraFactoryCustomizer;
import com.github.nosan.embedded.cassandra.api.connection.CassandraConnection;
import com.github.nosan.embedded.cassandra.api.connection.CassandraConnectionFactory;

/**
 * Annotation that allows the {@link Cassandra} to be started and stopped.
 * <p>Example:
 * <pre>
 * &#64;EmbeddedCassandra(scripts = "schema.cql")
 * &#64;ExtendWith(SpringExtension.class)
 * class CassandraTests {
 *      &#64;Test
 *      void test() {
 *      }
 * }
 * </pre>
 * <p><strong>Exposed properties:</strong>
 * The following properties will be added to {@link Environment} after {@link Cassandra} has started:
 * <pre>
 *     - embedded.cassandra.version
 *     - embedded.cassandra.address
 *     - embedded.cassandra.port
 *     - embedded.cassandra.ssl-port
 *     - embedded.cassandra.rpc-port
 * </pre>
 * <p>
 * Use {@link #exposeProperties()}  to disable properties exposing.
 * <p>
 * It is possible to register your own {@link CassandraFactory}, {@link CassandraConnectionFactory} or {@link
 * CassandraFactoryCustomizer CassandraFactoryCustomizers} bean(s) to control {@link Cassandra} and {@link
 * CassandraConnection} instances.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
public @interface EmbeddedCassandra {

	/**
	 * The paths to the CQL scripts to execute. Each path will be interpreted as a Spring {@link Resource}. A plain path
	 * &mdash; for example, {@code "schema.cql"} &mdash;  will be treated as an <em>absolute</em> classpath resource. A
	 * path which references a URL (e.g., a path prefixed with {@code http:}, etc.) will be loaded using the specified
	 * resource protocol.
	 *
	 * @return CQL Scripts
	 */
	String[] scripts() default {};

	/**
	 * The encoding for the supplied CQL scripts.
	 *
	 * @return CQL scripts encoding.
	 */
	String encoding() default "UTF-8";

	/**
	 * Whether {@link Cassandra} properties such as {@code 'embedded.cassandra.port'} should be added to {@link
	 * Environment} or not.
	 *
	 * @return {@code true} if properties should be exposed
	 */
	boolean exposeProperties() default true;

}
