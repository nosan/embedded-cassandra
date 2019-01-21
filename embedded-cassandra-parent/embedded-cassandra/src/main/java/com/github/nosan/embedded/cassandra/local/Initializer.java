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

import java.io.IOException;

/**
 * Basic strategy to initialize the {@code Cassandra}.
 *
 * @author Dmytro Nosan
 * @since 1.3.0
 */
@FunctionalInterface
interface Initializer {

	/**
	 * Initialize the {@code Cassandra}.
	 *
	 * @throws IOException in the case of any IO errors
	 */
	void initialize() throws IOException;
}
