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

package com.github.nosan.embedded.cassandra.spring.test.context;

import java.util.List;
import java.util.Set;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.spring.test.CassandraScripts;

/**
 * {@link ContextCustomizerFactory} to support {@link CassandraScripts} annotation.
 *
 * @author Dmytro Nosan
 */
class CassandraScriptsContextCustomizerFactory implements ContextCustomizerFactory {

	@Override
	@Nullable
	public ContextCustomizer createContextCustomizer(Class<?> testClass,
			List<ContextConfigurationAttributes> configAttributes) {
		Set<CassandraScripts> scripts = AnnotatedElementUtils.findAllMergedAnnotations(testClass,
				CassandraScripts.class);
		if (!scripts.isEmpty()) {
			return new CassandraScriptsContextCustomizer(scripts);
		}
		return null;
	}

}
