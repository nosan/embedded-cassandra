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

package com.github.nosan.embedded.cassandra.test.support;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * {@code JUnit5} extension to capture output from {@code System.out} and {@code System.err}.
 *
 * @author Dmytro Nosan
 * @since 1.4.2
 */
public final class CaptureOutputExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

	private static final Namespace NAMESPACE = Namespace.create(CaptureOutputExtension.class);

	@Override
	public void beforeEach(ExtensionContext context) {
		context.getStore(NAMESPACE).put(CaptureOutput.class, CaptureOutput.capture());
	}

	@Override
	public void afterEach(ExtensionContext context) {
		CaptureOutput captureOutput = context.getStore(NAMESPACE).remove(CaptureOutput.class, CaptureOutput.class);
		if (captureOutput != null) {
			captureOutput.release();
		}
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return parameterContext.getParameter().getType().isAssignableFrom(CaptureOutput.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return extensionContext.getStore(NAMESPACE).get(CaptureOutput.class, CaptureOutput.class);
	}

}
