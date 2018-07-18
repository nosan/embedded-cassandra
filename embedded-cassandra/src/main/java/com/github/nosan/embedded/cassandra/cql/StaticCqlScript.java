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


package com.github.nosan.embedded.cassandra.cql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * {@link CqlScript} implementation for a given CQL statements.
 *
 * @author Dmytro Nosan
 */
public class StaticCqlScript implements CqlScript {

	private final List<String> statements;

	public StaticCqlScript(String... statements) {
		this.statements = new ArrayList<>(statements != null ? Arrays.asList(statements) : Collections.emptyList());
	}

	@Override
	public String getName() {
		return "Script: 'Static CQL Statements'";
	}

	@Override
	public Collection<String> getStatements() {
		return Collections.unmodifiableList(this.statements);
	}
}
