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

import java.net.URL;

import javax.annotation.Nullable;

/**
 * {@link Initializer} to initialize {@code cassandra-rackdc.properties}.
 *
 * @author Dmytro Nosan
 * @since 1.0.9
 */
class RackFileInitializer extends AbstractFileReplacerInitializer {

	/**
	 * Creates a {@link RackFileInitializer}.
	 *
	 * @param rackFile URL to {@code cassandra-rackdc.properties}
	 */
	RackFileInitializer(@Nullable URL rackFile) {
		super(rackFile, (workDir, version) -> workDir.resolve("conf/cassandra-rackdc.properties"));
	}

}

