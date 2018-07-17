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

package com.github.nosan.embedded.cassandra.cql;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * Default implementation of the {@link CqlResourceLoader} interface.
 *
 * @author Dmytro Nosan
 */
public class DefaultCqlResourceLoader implements CqlResourceLoader {

	private final ClassLoader classLoader;

	private final Charset charset;

	public DefaultCqlResourceLoader() {
		this(null, null);
	}

	public DefaultCqlResourceLoader(ClassLoader classLoader, Charset charset) {
		this.classLoader = (classLoader != null ? classLoader : ClassLoaderUtils.getClassLoader());
		this.charset = (charset != null ? charset : Charset.defaultCharset());
	}

	@Override
	public CqlResource load(String location) {
		Objects.requireNonNull(location, "Location must not be null");
		try {
			URL url = new URL(location);
			return new UrlCqlResource(url, this.charset);
		}
		catch (MalformedURLException ex) {
			if (location.startsWith("/")) {
				return new ClassPathCqlResource(location.substring(1), this.classLoader, this.charset);
			}
			return new ClassPathCqlResource(location, this.classLoader, this.charset);
		}
	}


}
