/*
 * Copyright 2020 the original author or authors.
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

package com.github.nosan.embedded.cassandra.commons;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

import com.github.nosan.embedded.cassandra.commons.function.IOSupplier;

/**
 * The supplier which returns path to {@code user.home} directory.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public final class UserHomeDirectorySupplier implements Supplier<Path>, IOSupplier<Path> {

	@Override
	public Path get() {
		String userHome = System.getProperty("user.home");
		if (userHome == null || userHome.trim().isEmpty()) {
			throw new IllegalStateException("System Property: 'user.home' is empty");
		}
		return Paths.get(userHome);
	}

}
