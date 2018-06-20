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

package com.github.nosan.embedded.cassandra.config;

import com.github.nosan.embedded.cassandra.CassandraVersion;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.IPackageResolver;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.IVersion;

/**
 * Factory for creating {@link IPackageResolver}.
 *
 * @author Dmytro Nosan
 */
class PackageResolverFactory implements IPackageResolver {

	@Override
	public FileSet getFileSet(Distribution distribution) {
		return getResolver(distribution).getFileSet(distribution);
	}

	@Override
	public ArchiveType getArchiveType(Distribution distribution) {
		return getResolver(distribution).getArchiveType(distribution);
	}

	@Override
	public String getPath(Distribution distribution) {
		return getResolver(distribution).getPath(distribution);
	}

	private IPackageResolver getResolver(Distribution distribution) {
		IVersion version = distribution.getVersion();
		if (version instanceof CassandraVersion) {
			return create(((CassandraVersion) version));
		}
		throw new IllegalArgumentException("Version '" + version + "' is not supported");
	}

	private IPackageResolver create(CassandraVersion version) {
		if (CassandraVersion.LATEST == version) {
			return new LatestPackageResolver();
		}
		throw new IllegalArgumentException("Version '" + version + "' is not supported");
	}

}
