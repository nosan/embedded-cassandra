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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * Annotation that can be applied to a test class to configure an Embedded Cassandra
 * {@link com.datastax.driver.core.Cluster Cluster} to use instead of any application
 * defined {@link com.datastax.driver.core.Cluster Cluster}. <pre>
 * &#64;RunWith(SpringRunner.class)
 * &#64;ContextConfiguration
 * &#64;EmbeddedCassandra
 * public class CassandraTests {
 * 	&#64;Autowired
 * 	private Cluster cluster;
 * 	&#64;Test
 * 	public void test() {
 * 	//test...
 *    }
 * }
 * </pre>
 *
 * @author Dmytro Nosan
 * @see EmbeddedCassandraConfiguration
 * @see EmbeddedCassandraContextCustomizer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface EmbeddedCassandra {

	@AliasFor("scripts")
	String[] value() default {};

	@AliasFor("value")
	String[] scripts() default {};

	String encoding() default "";

}
