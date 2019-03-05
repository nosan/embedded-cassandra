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

package com.github.nosan.embedded.cassandra.local;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.LinkedHashSet;
import java.util.Set;

import com.github.nosan.embedded.cassandra.Version;

/**
 * {@link Initializer} to set 'executable permission' to {@code bin/cassandra} file.
 *
 * @author Dmytro Nosan
 * @since 1.2.5
 */
class CassandraFileExecutableInitializer extends AbstractFileInitializer {

	CassandraFileExecutableInitializer() {
		super((workDir, version) -> workDir.resolve("bin/cassandra"));
	}

	@Override
	protected void initialize(Path file, Path workingDirectory, Version version) {
		if (Files.exists(file) && !Files.isExecutable(file)) {
			try {
				Set<PosixFilePermission> permissions = new LinkedHashSet<>(Files.getPosixFilePermissions(file));
				permissions.add(PosixFilePermission.OWNER_EXECUTE);
				permissions.add(PosixFilePermission.GROUP_EXECUTE);
				permissions.add(PosixFilePermission.OTHERS_EXECUTE);
				Files.setPosixFilePermissions(file, permissions);
			}
			catch (Exception ex) {
				if (this.log.isDebugEnabled()) {
					this.log.error(String.format("Could not set 'executable' permission(s) to (%s)", file), ex);
				}
			}
		}
	}

}
