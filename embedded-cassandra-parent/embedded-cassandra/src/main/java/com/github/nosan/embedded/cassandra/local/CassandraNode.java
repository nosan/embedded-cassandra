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

import javax.annotation.Nullable;

import com.github.nosan.embedded.cassandra.Settings;

/**
 * Simple interface that allows the Cassandra Node to be {@link #start() started} and {@link #stop()
 * stopped}.
 *
 * @author Dmytro Nosan
 * @since 1.4.1
 */
interface CassandraNode {

	/**
	 * Starts the Cassandra Node.
	 *
	 * @throws IOException if the Cassandra Node can not be started
	 * @throws InterruptedException if the current thread is {@link Thread#interrupt() interrupted} by another thread
	 */
	void start() throws IOException, InterruptedException;

	/**
	 * Stops the Cassandra Node.
	 *
	 * @throws IOException if the Cassandra Node can not be stopped
	 * @throws InterruptedException if the current thread is {@link Thread#interrupt() interrupted} by another thread
	 */
	void stop() throws IOException, InterruptedException;

	/**
	 * Returns the settings this node is running on.
	 *
	 * @return the settings
	 */
	@Nullable
	Settings getSettings();

}
