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
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import de.flapdoodle.embed.process.distribution.Distribution;

/**
 * A basic class for {@link FileCustomizer FileCustomizers} composition.
 *
 * @author Dmytro Nosan
 * @see JVMOptionsFileCustomizer
 * @see EnvironmentFileCustomizer
 * @see NoopFileCustomizer
 */
public class CompositeFileCustomizer implements FileCustomizer {

	private final Collection<? extends FileCustomizer> customizers;

	public CompositeFileCustomizer(Collection<? extends FileCustomizer> customizers) {
		this.customizers = Objects.requireNonNull(customizers,
				"FileCustomizers must not be null");
	}

	public CompositeFileCustomizer(FileCustomizer... customizers) {
		this(Arrays.asList(
				Objects.requireNonNull(customizers, "FileCustomizers must not be null")));
	}

	@Override
	public void customize(File file, Distribution distribution) throws IOException {
		for (FileCustomizer fileCustomizer : this.customizers) {
			fileCustomizer.customize(file, distribution);
		}
	}

}
