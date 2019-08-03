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

package com.github.nosan.embedded.cassandra;

import java.io.IOException;

/**
 * Simple interface that allows the {@code Cassandra's} node to be  {@link #start() started}.
 *
 * @author Dmytro Nosan
 */
interface Node {

	/**
	 * Starts {@code Cassandra's} node.
	 *
	 * @return a new {@link NodeProcess}
	 * @throws IOException if the {@code Cassandra's} node cannot be started
	 * @throws InterruptedException if the {@code Cassandra's} node has been interrupted.
	 */
	NodeProcess start() throws IOException, InterruptedException;

}
