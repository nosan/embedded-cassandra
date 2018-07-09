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

package com.github.nosan.embedded.cassandra.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;

/**
 * Simple {@link ContextCustomizer Customizer} to run {@link FileCustomizer File
 * Customizers}.
 *
 * @author Dmytro Nosan
 * @see #addCustomizer(FileCustomizer)
 */
class FileCustomizers implements ContextCustomizer {

	private final List<FileCustomizer> customizers = new ArrayList<>();

	@Override
	public void customize(Context context) {
		IExtractedFileSet fileSet = context.getExtractedFileSet();
		for (File file : fileSet.files(FileType.Library)) {
			for (FileCustomizer fileCustomizer : this.customizers) {
				try {
					fileCustomizer.customize(file, context);
				}
				catch (IOException ex) {
					throw new IllegalStateException(ex);
				}
			}
		}

	}

	/**
	 * Add a {@link FileCustomizer} into the list.
	 *
	 * @param fileCustomizer file customizer to add
	 */
	void addCustomizer(FileCustomizer fileCustomizer) {
		this.customizers.add(Objects.requireNonNull(fileCustomizer,
				"File Customizer must not be null"));
	}

}
