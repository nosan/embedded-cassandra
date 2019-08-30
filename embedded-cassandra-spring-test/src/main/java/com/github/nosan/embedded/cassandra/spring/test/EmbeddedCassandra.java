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

package com.github.nosan.embedded.cassandra.spring.test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.nosan.embedded.cassandra.EmbeddedCassandraFactory;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.CassandraFactoryCustomizer;

/**
 * Annotation that allows the {@link Cassandra} to be started and stopped. Cassandra will be registered as a bean named
 * Cassandra.class.getName(). By default, {@link EmbeddedCassandraFactory} used, which is configured to use random
 * ports. However, it still exists a way to register your own CassandraFactory to control Cassandra instance by your own
 * wish.
 * <p>Example:
 * <pre>
 * &#64;EmbeddedCassandra
 * &#64;ExtendWith(SpringExtension.class)
 * class CassandraTests {
 *
 *
 *      &#64;BeforeAll
 *      static void prepare(@Autowired Cassandra cassandra) {
 *      }
 *
 *      &#64;Test
 *      void test(&#64;Autowired Cassandra cassandra) {
 *      }
 *
 *      &#64;Configuration
 *      static class TestConfig {
 *
 *         &#64;Bean
 *         public EmbeddedCassandraFactory createCassandraFactory() {
 *           EmbeddedCassandraFactory cassandraFactory = new EmbeddedCassandraFactory();
 *           //...
 *           return cassandraFactory;
 *          }
 *     }
 * }
 * </pre>
 * Additional to the above, there is also possible to register {@link CassandraFactoryCustomizer} beans to customize a
 * default {@link EmbeddedCassandraFactory} before the {@link Cassandra} itself is started.
 * <p>Example:
 * <pre>
 * &#64;EmbeddedCassandra
 * &#64;ExtendWith(SpringExtension.class)
 * class CassandraTests {
 *
 *      &#64;BeforeAll
 *      static void prepare(@Autowired Cassandra cassandra) {
 *      }
 *
 *      &#64;Test
 *      void test(&#64;Autowired Cassandra cassandra) {
 *      }
 *
 *      &#64;Configuration
 *      static class TestConfig {
 *
 *         &#64;Bean
 *         public CassandraFactoryCustomizer&lt;EmbeddedCassandraFactory&gt; versionCustomizer() {
 *            return cassandraFactory -&gt; cassandraFactory.setArtifact(Artifact.of("3.0.0"));
 *         }
 *     }
 * }
 * </pre>
 *
 * @author Dmytro Nosan
 * @see EmbeddedCassandraFactory
 * @see CassandraFactoryCustomizer
 * @since 3.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface EmbeddedCassandra {

}
