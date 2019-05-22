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

package com.github.nosan.embedded.cassandra.local.artifact;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import com.github.nosan.embedded.cassandra.Version;

/**
 * {@link UrlFactory} that creates {@code URLs} to the well known resources.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
public class DefaultUrlFactory implements UrlFactory {

	@Override
	public URL[] create(Version version) throws MalformedURLException {
		Objects.requireNonNull(version, "Version must not be null");
		return new URL[]{
				new URL(String.format("https://apache.org/dyn/closer.cgi?action=download&filename=cassandra"
						+ "/%1$s/apache-cassandra-%1$s-bin.tar.gz", version)),
				new URL(String.format("https://dist.apache.org/repos/dist/release/cassandra"
						+ "/%1$s/apache-cassandra-%1$s-bin.tar.gz", version)),
				new URL(String.format("https://archive.apache.org/dist/cassandra"
						+ "/%1$s/apache-cassandra-%1$s-bin.tar.gz", version))
		};
	}

}
