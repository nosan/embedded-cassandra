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

package com.github.nosan.embedded.cassandra.local;

import java.io.IOException;
import java.nio.file.Path;

import javax.annotation.Nonnull;

/**
 * Customizer interface used to customize a {@code directory}.
 *
 * @author Dmytro Nosan
 * @since 1.0.9
 */
@FunctionalInterface
interface DirectoryCustomizer {

	/**
	 * Customize a directory.
	 *
	 * @param directory the base directory
	 * @throws IOException in the case of any IO errors
	 */
	void customize(@Nonnull Path directory) throws IOException;

}
