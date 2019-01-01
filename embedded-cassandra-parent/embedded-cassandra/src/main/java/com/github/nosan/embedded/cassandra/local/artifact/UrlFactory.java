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

package com.github.nosan.embedded.cassandra.local.artifact;

import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.Nonnull;

import org.apiguardian.api.API;

import com.github.nosan.embedded.cassandra.Version;

/**
 * Factory that creates {@link URL}.
 *
 * @author Dmytro Nosan
 * @see DefaultUrlFactory
 * @since 1.0.0
 */
@API(since = "1.0.0", status = API.Status.STABLE)
@FunctionalInterface
public interface UrlFactory {

	/**
	 * Creates {@link URL} depends on a {@link Version}.
	 *
	 * @param version a version
	 * @return URL candidates to use.
	 * @throws MalformedURLException if no protocol is specified, or an unknown protocol is found, or {@code spec}
	 * is {@code null}.
	 */
	@Nonnull
	URL[] create(@Nonnull Version version) throws MalformedURLException;
}
