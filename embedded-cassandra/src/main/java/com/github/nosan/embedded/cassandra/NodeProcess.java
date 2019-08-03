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
 * Simple interface that represent the {@code Cassandra's} node.
 *
 * @author Dmytro Nosan
 */
interface NodeProcess {

	/**
	 * Returns {@code Cassandra's} process.
	 *
	 * @return the process
	 */
	Process getProcess();

	/**
	 * Returns the pid.
	 *
	 * @return the pid (or -1 if none)
	 */
	long getPid();

	/**
	 * Returns whether the node  is alive.
	 *
	 * @return {@code true} if the node has not yet terminated.
	 */
	boolean isAlive();

	/**
	 * Stops {@code Cassandra's} node.
	 *
	 * @throws IOException if  {@code Cassandra's} node cannot be stopped
	 * @throws InterruptedException if  {@code Cassandra's} node has been interrupted.
	 */
	void stop() throws IOException, InterruptedException;

}
