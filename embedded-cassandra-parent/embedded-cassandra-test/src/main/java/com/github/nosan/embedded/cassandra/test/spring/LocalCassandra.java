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

import org.apiguardian.api.API;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.io.Resource;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;
import com.github.nosan.embedded.cassandra.local.artifact.ArtifactFactory;
import com.github.nosan.embedded.cassandra.local.artifact.RemoteArtifactFactory;

/**
 * Annotation that can be specified on a test class that runs {@link Cassandra} based tests. This annotation extends
 * {@link EmbeddedCassandra} annotation and allows to customize {@link RemoteArtifactFactory}
 * and {@link LocalCassandraFactory}.
 * <p>
 * Customized {@link LocalCassandraFactory} will be registered as a <b>primary</b> bean with a name
 * <b><i>localCassandraFactory</i></b>.
 * <p> {@link RemoteArtifactFactory} <b>will not</b> be registered, but will be used by {@link LocalCassandraFactory}.
 * <p>The typical usage of this annotation is like:
 * <pre class="code">
 * &#064;RunWith(SpringRunner.class)
 * &#064;LocalCassandra(version = "2.2.12", ...)
 * public class CassandraTests {
 * &#064;Autowired
 * private TestCassandra cassandra;
 * &#064;Autowired
 * private Cluster cluster;
 * }
 * </pre>
 * <p>
 * <b>Note!</b> It is possible to define you own {@link ArtifactFactory} bean to control {@link LocalCassandraFactory}
 * instance.
 *
 * @author Dmytro Nosan
 * @see EmbeddedCassandra
 * @see EmbeddedLocalCassandra
 * @see LocalCassandraFactory
 * @see RemoteArtifactFactory
 * @see LocalCassandraContextCustomizer
 * @see LocalCassandraFactoryBean
 * @since 1.0.7
 * @deprecated in favor of {@link EmbeddedLocalCassandra}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
@EmbeddedLocalCassandra
@Deprecated
@API(since = "1.0.7", status = API.Status.DEPRECATED)
public @interface LocalCassandra {

	/**
	 * Alias for {@link EmbeddedLocalCassandra#version()}.
	 *
	 * @return The value of the {@code version} attribute
	 * @see Version
	 */
	@AliasFor(annotation = EmbeddedLocalCassandra.class)
	String version() default "";

	/**
	 * Alias for {@link EmbeddedLocalCassandra#configurationFile()}.
	 *
	 * @return The value of the {@code configurationFile} attribute
	 * @see Resource
	 */
	@AliasFor(annotation = EmbeddedLocalCassandra.class)
	String configurationFile() default "";

	/**
	 * Alias for {@link EmbeddedLocalCassandra#logbackFile()}.
	 *
	 * @return The value of the {@code logbackFile} attribute
	 * @see Resource
	 */
	@AliasFor(annotation = EmbeddedLocalCassandra.class)
	String logbackFile() default "";

	/**
	 * Alias for {@link EmbeddedLocalCassandra#rackFile()}.
	 *
	 * @return the value of {@code rackFile} attribute
	 * @see Resource
	 */
	@AliasFor(annotation = EmbeddedLocalCassandra.class)
	String rackFile() default "";

	/**
	 * Alias for {@link EmbeddedLocalCassandra#commitLogArchivingFile()}.
	 *
	 * @return the value of {@code commitLogArchivingFile} attribute
	 * @see Resource
	 * @since 1.2.8
	 */
	@AliasFor(annotation = EmbeddedLocalCassandra.class)
	String commitLogArchivingFile() default "";

	/**
	 * Alias for {@link EmbeddedLocalCassandra#topologyFile()}.
	 *
	 * @return the value of {@code topologyFile} attribute
	 * @see Resource
	 */
	@AliasFor(annotation = EmbeddedLocalCassandra.class)
	String topologyFile() default "";

	/**
	 * Alias for {@link EmbeddedLocalCassandra#workingDirectory()}.
	 *
	 * @return The value of the {@code workingDirectory} attribute
	 * @see Path
	 */
	@AliasFor(annotation = EmbeddedLocalCassandra.class)
	String workingDirectory() default "";

	/**
	 * Alias for {@link EmbeddedLocalCassandra#javaHome()}.
	 *
	 * @return The value of the {@code javaHome} attribute
	 * @see Path
	 * @since 1.0.9
	 */
	@AliasFor(annotation = EmbeddedLocalCassandra.class)
	String javaHome() default "";

	/**
	 * Alias for {@link EmbeddedLocalCassandra#jmxPort()}.
	 *
	 * @return The value of the {@code jmxPort} attribute
	 * @since 1.1.1
	 */
	@AliasFor(annotation = EmbeddedLocalCassandra.class)
	int jmxPort() default 7199;

	/**
	 * Alias for {@link EmbeddedLocalCassandra#jvmOptions()}.
	 *
	 * @return The value of the {@code jvmOptions} attribute
	 */
	@AliasFor(annotation = EmbeddedLocalCassandra.class)
	String[] jvmOptions() default {};

	/**
	 * Alias for {@link EmbeddedLocalCassandra#startupTimeout()}.
	 *
	 * @return The value of the {@code startupTimeout} attribute
	 */
	@AliasFor(annotation = EmbeddedLocalCassandra.class)
	long startupTimeout() default 30000;

	/**
	 * Alias for {@link EmbeddedLocalCassandra#allowRoot()}.
	 *
	 * @return The value of the {@code allowRoot} attribute
	 * @since 1.2.1
	 */
	@AliasFor(annotation = EmbeddedLocalCassandra.class)
	boolean allowRoot() default false;

	/**
	 * Alias for {@link EmbeddedLocalCassandra#registerShutdownHook()}.
	 *
	 * @return The value of the {@code registerShutdownHook} attribute
	 * @since 1.2.3
	 */
	@AliasFor(annotation = EmbeddedLocalCassandra.class)
	boolean registerShutdownHook() default true;

	/**
	 * Alias for {@link EmbeddedLocalCassandra#artifact()}.
	 *
	 * @return The value of the {@code artifactFactory} attribute
	 * @since 1.2.6
	 */
	@AliasFor(annotation = EmbeddedLocalCassandra.class)
	EmbeddedLocalCassandra.Artifact artifact() default @EmbeddedLocalCassandra.Artifact;

	/**
	 * Alias for {@link EmbeddedLocalCassandra#scripts()}.
	 *
	 * @return CQL Scripts
	 */
	@AliasFor(annotation = EmbeddedLocalCassandra.class)
	String[] scripts() default {};

	/**
	 * Alias for {@link EmbeddedLocalCassandra#statements()}.
	 *
	 * @return CQL statements
	 */
	@AliasFor(annotation = EmbeddedLocalCassandra.class)
	String[] statements() default {};

	/**
	 * Alias for {@link EmbeddedLocalCassandra#encoding()}.
	 *
	 * @return CQL scripts encoding.
	 */
	@AliasFor(annotation = EmbeddedLocalCassandra.class)
	String encoding() default "";

	/**
	 * Alias for {@link EmbeddedLocalCassandra#replace()}.
	 *
	 * @return the type of existing {@code Cluster} to replace
	 */
	@AliasFor(annotation = EmbeddedLocalCassandra.class)
	EmbeddedCassandra.Replace replace() default EmbeddedCassandra.Replace.ANY;
}
