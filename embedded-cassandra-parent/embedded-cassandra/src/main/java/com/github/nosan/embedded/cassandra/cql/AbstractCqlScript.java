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

package com.github.nosan.embedded.cassandra.cql;

import java.util.Collection;
import java.util.Collections;

import org.apiguardian.api.API;

import com.github.nosan.embedded.cassandra.util.annotation.Nullable;

/**
 * Convenience base class for {@link CqlScript} implementations, pre-implementing CQL script parsing.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
@API(since = "1.0.0", status = API.Status.STABLE)
public abstract class AbstractCqlScript implements CqlScript {

	@Override
	public final Collection<String> getStatements() {
		String script = getScript();
		return Collections.unmodifiableList(CqlScriptParser.parse(script));
	}

	/**
	 * Returns CQL script.
	 *
	 * @return CQL script
	 */
	@Nullable
	protected abstract String getScript();

}
