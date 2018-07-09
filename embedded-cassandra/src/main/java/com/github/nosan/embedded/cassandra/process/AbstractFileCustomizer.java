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

/**
 * A basic {@link FileCustomizer File Customizer}.
 *
 * @author Dmytro Nosan
 */
abstract class AbstractFileCustomizer implements FileCustomizer {

	@Override
	public final void customize(File file, Context context) throws IOException {
		if (file.exists() && file.isFile() && isMatch(file, context)) {
			process(file, context);
		}
	}

	/**
	 * Determine if the specified file matches or not.
	 *
	 * @param file source file.
	 * @param context {@link Context processContext}.
	 * @return Whether source file is match or not.
	 * @throws IOException if an I/O error occurs.
	 */
	protected abstract boolean isMatch(File file, Context context) throws IOException;

	/**
	 * Process the provided file.
	 *
	 * @param file source file.
	 * @param context {@link Context processContext}.
	 * @throws IOException if an I/O error occurs.
	 */
	protected abstract void process(File file, Context context) throws IOException;

}
