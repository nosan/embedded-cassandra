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

package com.github.nosan.embedded.cassandra.artifact;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.github.nosan.embedded.cassandra.api.Version;

/**
 * {@link UrlFactory} implementation that returns URLs to well-known repositories.
 * <pre>{@code
 * https://apache.org/dyn/closer.cgi/?action=download&filename=cassandra/$version/apache-cassandra-$version-bin.tar.gz
 * https://dist.apache.org/repos/dist/release/cassandra/$version/apache-cassandra-$version-bin.tar.gz
 * https://archive.apache.org/dist/cassandra/$version/apache-cassandra-$version-bin.tar.gz
 * }
 * </pre>
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public final class DefaultUrlFactory implements UrlFactory {

	@Override
	public List<URL> create(Version version) throws MalformedURLException {
		Objects.requireNonNull(version, "'version' must not be null");
		List<URL> urls = new ArrayList<>();
		urls.add(new URL(String.format("https://apache.org/dyn/closer.cgi?action=download"
				+ "&filename=cassandra/%1$s/apache-cassandra-%1$s-bin.tar.gz", version)));
		urls.add(new URL(String.format("https://dist.apache.org/repos/dist/release/cassandra/"
				+ "%1$s/apache-cassandra-%1$s-bin.tar.gz", version)));
		urls.add(new URL(String
				.format("https://archive.apache.org/dist/cassandra/%1$s/apache-cassandra-%1$s-bin.tar.gz", version)));
		return Collections.unmodifiableList(urls);
	}

}
