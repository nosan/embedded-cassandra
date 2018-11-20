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
import java.nio.file.Path;

import org.springframework.core.annotation.AliasFor;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;
import com.github.nosan.embedded.cassandra.local.artifact.ArtifactFactory;

/**
 * Annotation that can be specified on a test class that runs {@link Cassandra} based tests. This annotation extends
 * {@link EmbeddedCassandra} and allows to customize and register {@link LocalCassandraFactory} bean.
 * <p>
 * <b>Note!</b> It is possible to define you own {@link ArtifactFactory} bean to control {@link LocalCassandraFactory}
 * instance.
 *
 * @author Dmytro Nosan
 * @see EmbeddedCassandra
 * @see DirtiesContext
 * @see ArtifactFactory
 * @see LocalCassandraFactory
 * @see LocalCassandraContextCustomizer
 * @see LocalCassandraFactoryBean
 * @since 1.0.7
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
@EmbeddedCassandra
public @interface LocalCassandra {

	/**
	 * Sets attribute for {@link LocalCassandraFactory#getVersion()}.
	 *
	 * @return The value of the {@code version} attribute
	 * @see Version
	 */
	String version() default "";

	/**
	 * Sets attribute for {@link LocalCassandraFactory#getConfigurationFile()}.
	 * <p>
	 * Path will be interpreted as a Spring
	 * {@link Resource}.
	 *
	 * @return The value of the {@code configurationFile} attribute
	 * @see Resource
	 */
	String configurationFile() default "";

	/**
	 * Sets attribute for {@link LocalCassandraFactory#getLogbackFile()}.
	 * <p>
	 * Path will be interpreted as a Spring
	 * {@link Resource}.
	 *
	 * @return The value of the {@code logbackFile} attribute
	 * @see Resource
	 */
	String logbackFile() default "";

	/**
	 * Sets attribute for {@link LocalCassandraFactory#getRackFile()}.
	 * <p>
	 * Path will be interpreted as a Spring
	 * {@link Resource}.
	 *
	 * @return the value of {@code rackFile} attribute
	 * @see Resource
	 */
	String rackFile() default "";

	/**
	 * Sets attribute for {@link LocalCassandraFactory#getTopologyFile()}.
	 * <p>
	 * Path will be interpreted as a Spring
	 * {@link Resource}.
	 *
	 * @return the value of {@code topologyFile} attribute
	 * @see Resource
	 */
	String topologyFile() default "";

	/**
	 * Sets attribute for {@link LocalCassandraFactory#getWorkingDirectory()}.
	 * <p>
	 * Path will be interpreted as a {@link Path}.
	 *
	 * @return The value of the {@code workingDirectory} attribute
	 * @see Path
	 */
	String workingDirectory() default "";

	/**
	 * Sets attribute for {@link LocalCassandraFactory#getJavaHome()}.
	 * <p>
	 * Path will be interpreted as a {@link Path}.
	 *
	 * @return The value of the {@code javaHome} attribute
	 * @see Path
	 * @since 1.0.9
	 */
	String javaHome() default "";

	/**
	 * Sets attribute for {@link LocalCassandraFactory#getJmxPort()}.
	 *
	 * @return The value of the {@code jmxPort} attribute
	 * @since 1.1.1
	 */
	int jmxPort() default 7199;

	/**
	 * JVM options that should be associated with Cassandra.
	 *
	 * @return The value of the {@code jvmOptions} attribute
	 */
	String[] jvmOptions() default {};

	/**
	 * Sets attribute for {@link LocalCassandraFactory#getStartupTimeout()} in milliseconds.
	 *
	 * @return The value of the {@code startupTimeout} attribute
	 */
	long startupTimeout() default 30000;


	/**
	 * Alias for {@link EmbeddedCassandra#scripts()}.
	 *
	 * @return CQL Scripts
	 */
	@AliasFor(annotation = EmbeddedCassandra.class)
	String[] scripts() default {};

	/**
	 * Alias for {@link EmbeddedCassandra#statements()}.
	 *
	 * @return CQL statements
	 */
	@AliasFor(annotation = EmbeddedCassandra.class)
	String[] statements() default {};

	/**
	 * Alias for {@link EmbeddedCassandra#encoding()}.
	 *
	 * @return CQL scripts encoding.
	 */
	@AliasFor(annotation = EmbeddedCassandra.class)
	String encoding() default "";

	/**
	 * Alias for {@link EmbeddedCassandra#replace()}.
	 *
	 * @return the type of existing {@code Cluster} to replace
	 */
	@AliasFor(annotation = EmbeddedCassandra.class)
	EmbeddedCassandra.Replace replace() default EmbeddedCassandra.Replace.ANY;


}
