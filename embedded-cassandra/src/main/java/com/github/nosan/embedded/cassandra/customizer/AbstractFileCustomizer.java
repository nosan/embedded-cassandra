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

package com.github.nosan.embedded.cassandra.customizer;

import java.io.File;
import java.io.IOException;

import de.flapdoodle.embed.process.distribution.Distribution;

/**
 * A basic {@link FileCustomizer}.
 *
 * @author Dmytro Nosan
 */
public abstract class AbstractFileCustomizer implements FileCustomizer {

	@Override
	public final void customize(File file, Distribution distribution) throws IOException {
		if (isMatch(file, distribution)) {
			process(file, distribution);
		}
	}

	/**
	 * Determine if the specified file matches or not.
	 * @param file Source file.
	 * @param distribution {@link Distribution}.
	 * @return Whether source file is match or not.
	 * @throws IOException IO Exception.
	 */
	protected abstract boolean isMatch(File file, Distribution distribution)
			throws IOException;

	/**
	 * Process the provided file.
	 * @param file Source File.
	 * @param distribution {@link Distribution}.
	 * @throws IOException IO Exception.
	 */
	protected abstract void process(File file, Distribution distribution)
			throws IOException;

}
