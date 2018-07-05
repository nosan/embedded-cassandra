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

package com.github.nosan.embedded.cassandra.support;

import de.flapdoodle.embed.process.extract.ITempNaming;

/**
 * Simple {@link ITempNaming Temp Naming} implementation which uses the original name.
 *
 * @author Dmytro Nosan
 */
public class OriginTempNaming implements ITempNaming {

	@Override
	public String nameFor(String prefix, String postfix) {
		return postfix;
	}

}
