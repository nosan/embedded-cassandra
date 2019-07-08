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

package com.github.nosan.embedded.cassandra.test.spring;

import java.util.List;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;

import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * {@link ContextCustomizerFactory} to create {@link EmbeddedCassandraContextCustomizer}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
class EmbeddedCassandraContextCustomizerFactory implements ContextCustomizerFactory {

	@Nullable
	@Override
	public ContextCustomizer createContextCustomizer(Class<?> testClass,
			List<ContextConfigurationAttributes> configAttributes) {
		AnnotationAttributes attributes = AnnotatedElementUtils
				.findMergedAnnotationAttributes(testClass, EmbeddedCassandra.class.getName(), false, false);
		if (attributes != null) {
			return new EmbeddedCassandraContextCustomizer(testClass, attributes);
		}
		return null;
	}

}
