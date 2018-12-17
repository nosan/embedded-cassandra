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

package com.github.nosan.embedded.cassandra.test.spring;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;

/**
 * {@link ContextCustomizerFactory} to support {@link EmbeddedCassandra}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
class EmbeddedCassandraContextCustomizerFactory implements ContextCustomizerFactory {

	@Nullable
	@Override
	public ContextCustomizer createContextCustomizer(@Nonnull Class<?> testClass,
			@Nonnull List<ContextConfigurationAttributes> configAttributes) {
		EmbeddedCassandra annotation = AnnotatedElementUtils
				.findMergedAnnotation(testClass, EmbeddedCassandra.class);
		if (annotation != null) {
			return new EmbeddedCassandraContextCustomizer(annotation);
		}
		return null;
	}

}
