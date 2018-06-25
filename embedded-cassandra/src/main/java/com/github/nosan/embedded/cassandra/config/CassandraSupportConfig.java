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

import java.util.concurrent.TimeUnit;

import de.flapdoodle.embed.process.config.ISupportConfig;

/**
 * {@link ISupportConfig} support config. This config will be used for opening new issue.
 *
 * @author Dmytro Nosan
 */
class CassandraSupportConfig implements ISupportConfig {

	@Override
	public String getName() {
		return "Embedded Cassandra";
	}

	@Override
	public String getSupportUrl() {
		return "https://github.com/nosan/embedded-cassandra";
	}

	@Override
	public long maxStopTimeoutMillis() {
		return TimeUnit.SECONDS.toMillis(5);
	}

	@Override
	public String messageOnException(Class<?> context, Exception exception) {
		return "If you feel this is [" + exception.getMessage()
				+ "] a bug, please open a new issue. Follow this link: " + getSupportUrl()
				+ "\n" + "Thank you! :)";
	}

}
