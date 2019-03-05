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
import java.net.Proxy;
import java.nio.file.Path;

import org.apiguardian.api.API;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.io.Resource;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;
import com.github.nosan.embedded.cassandra.local.artifact.ArtifactFactory;
import com.github.nosan.embedded.cassandra.local.artifact.RemoteArtifactFactory;
import com.github.nosan.embedded.cassandra.local.artifact.UrlFactory;
import com.github.nosan.embedded.cassandra.test.ClusterFactory;
import com.github.nosan.embedded.cassandra.test.TestCassandra;

/**
 * Annotation that can be specified on a test class that runs {@link Cassandra} based tests. This annotation extends
 * {@link EmbeddedCassandra} annotation and allows to customize {@link RemoteArtifactFactory}
 * and {@link LocalCassandraFactory}.
 * <p>
 * Customized {@link LocalCassandraFactory} will be registered as a <b>@Primary</b> bean with a name
 * <em>localCassandraFactory</em>.
 * <p> {@link RemoteArtifactFactory} <b>will not</b> be registered as a bean, but will be used by {@link
 * LocalCassandraFactory}.
 * <p>The typical usage of this annotation is like:
 * <pre class="code">
 * &#064;RunWith(SpringRunner.class) //for JUnit4
 * &#064;EmbeddedLocalCassandra(version = "2.2.12", ...)
 * public class CassandraTests {
 * &#064;Autowired
 * private TestCassandra cassandra;
 * &#064;Autowired
 * private Cluster cluster;
 * }
 * </pre>
 * <p>
 * <b>Note!</b> It is possible to define you own {@link ArtifactFactory} bean to control {@link LocalCassandraFactory}
 * instance, also is still possible to define you own {@link ClusterFactory} bean to control {@link TestCassandra}
 * instance.
 *
 * @author Dmytro Nosan
 * @see EmbeddedCassandra
 * @see LocalCassandraFactory
 * @see RemoteArtifactFactory
 * @since 1.2.6
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
@EmbeddedCassandra
@API(since = "1.2.6", status = API.Status.STABLE)
public @interface EmbeddedLocalCassandra {

	/**
	 * Sets attribute for {@link LocalCassandraFactory#getVersion()}.
	 * <p>
	 * This value can contain a {@code spring} placeholder.
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
	 * <p>
	 * This value can contain a {@code spring} placeholder.
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
	 * <p>
	 * This value can contain a {@code spring} placeholder.
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
	 * <p>
	 * This value can contain a {@code spring} placeholder.
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
	 * <p>
	 * This value can contain a {@code spring} placeholder.
	 *
	 * @return the value of {@code topologyFile} attribute
	 * @see Resource
	 */
	String topologyFile() default "";

	/**
	 * Sets attribute for {@link LocalCassandraFactory#getCommitLogArchivingFile()}.
	 * <p>
	 * Path will be interpreted as a Spring
	 * {@link Resource}.
	 * <p>
	 * This value can contain a {@code spring} placeholder.
	 *
	 * @return the value of {@code commitLogArchivingFile} attribute
	 * @see Resource
	 * @since 1.2.8
	 */
	String commitLogArchivingFile() default "";

	/**
	 * Sets attribute for {@link LocalCassandraFactory#getWorkingDirectory()}.
	 * <p>
	 * Path will be interpreted as a {@link Path}.
	 * <p>
	 * This value can contain a {@code spring} placeholder.
	 *
	 * @return The value of the {@code workingDirectory} attribute
	 * @see Path
	 */
	String workingDirectory() default "";

	/**
	 * Sets attribute for {@link LocalCassandraFactory#getJavaHome()}.
	 * <p>
	 * Path will be interpreted as a {@link Path}.
	 * <p>
	 * This value can contain a {@code spring} placeholder.
	 *
	 * @return The value of the {@code javaHome} attribute
	 * @see Path
	 */
	String javaHome() default "";

	/**
	 * Sets attribute for {@link LocalCassandraFactory#getJmxPort()}.
	 *
	 * @return The value of the {@code jmxPort} attribute
	 */
	int jmxPort() default 7199;

	/**
	 * Sets attribute for {@link LocalCassandraFactory#getJvmOptions()}.
	 * <p>
	 * This value can contain a {@code spring} placeholder.
	 *
	 * @return The value of the {@code jvmOptions} attribute
	 */
	String[] jvmOptions() default {};

	/**
	 * Sets attribute for {@link LocalCassandraFactory#getStartupTimeout()} in milliseconds.
	 *
	 * @return The value of the {@code startupTimeout} attribute
	 */
	long startupTimeout() default 60000;

	/**
	 * Sets attribute for {@link LocalCassandraFactory#isAllowRoot()}.
	 *
	 * @return The value of the {@code allowRoot} attribute
	 */
	boolean allowRoot() default false;

	/**
	 * Sets attribute for {@link LocalCassandraFactory#isRegisterShutdownHook()}.
	 *
	 * @return The value of the {@code registerShutdownHook} attribute
	 */
	boolean registerShutdownHook() default true;

	/**
	 * Sets attribute for {@link LocalCassandraFactory#getArtifactFactory()}.
	 *
	 * @return The value of the {@code artifactFactory} attribute
	 */
	Artifact artifact() default @Artifact;

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
	EmbeddedCassandra.Replace replace() default EmbeddedCassandra.Replace.NONE;

	/**
	 * Alias for {@link EmbeddedCassandra#registerShutdownHook()}.
	 *
	 * @return The value of the {@code registerTestShutdownHook} attribute
	 * @since 1.2.8
	 */
	@AliasFor(annotation = EmbeddedCassandra.class, attribute = "registerShutdownHook")
	boolean registerTestShutdownHook() default true;

	/**
	 * Sets attribute for {@link LocalCassandraFactory#getArtifactDirectory()}.
	 * <p>
	 * Path will be interpreted as a {@link Path}.
	 * <p>
	 * This value can contain a {@code spring} placeholder.
	 *
	 * @return The value of the {@code artifactDirectory} attribute
	 * @see Path
	 * @since 1.3.0
	 */
	String artifactDirectory() default "";

	/**
	 * Annotation that describes {@link RemoteArtifactFactory} attributes.
	 *
	 * @see RemoteArtifactFactory
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({})
	@Documented
	@interface Artifact {

		/**
		 * Sets attribute for {@link RemoteArtifactFactory#getDirectory()}.
		 * <p>
		 * Path will be interpreted as a {@link Path}.
		 * <p>
		 * This value can contain a {@code spring} placeholder.
		 *
		 * @return The value of the {@code directory} attribute
		 */
		String directory() default "";

		/**
		 * Sets attribute for {@link RemoteArtifactFactory#getUrlFactory()}.
		 *
		 * @return The value of the {@code urlFactory} attribute
		 */
		Class<? extends UrlFactory> urlFactory() default UrlFactory.class;

		/**
		 * Sets proxy host attribute for {@link RemoteArtifactFactory#getProxy()}}.
		 * <p>
		 * This value can contain a {@code spring} placeholder.
		 *
		 * @return The value of the {@code proxyHost} attribute
		 */
		String proxyHost() default "";

		/**
		 * Sets proxy port attribute for {@link RemoteArtifactFactory#getProxy()}}.
		 *
		 * @return The value of the {@code proxyPort} attribute
		 */
		int proxyPort() default -1;

		/**
		 * Sets proxy type attribute for {@link RemoteArtifactFactory#getProxy()}}.
		 *
		 * @return The value of the {@code proxyType} attribute
		 */
		Proxy.Type proxyType() default Proxy.Type.HTTP;

		/**
		 * Sets attribute for {@link RemoteArtifactFactory#getReadTimeout()} in milliseconds.
		 *
		 * @return The value of the {@code readTimeout} attribute
		 */
		long readTimeout() default 30000;

		/**
		 * Sets attribute for {@link RemoteArtifactFactory#getConnectTimeout()} in milliseconds.
		 *
		 * @return The value of the {@code connectTimeout} attribute
		 */
		long connectTimeout() default 30000;

	}

}
