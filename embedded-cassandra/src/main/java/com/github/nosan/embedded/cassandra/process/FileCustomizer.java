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
 * Callback interface that can be used to customize a {@link File File}.
 *
 * @author Dmytro Nosan
 * @see AbstractFileCustomizer
 * @see AbstractSourceLineFileCustomizer
 */
interface FileCustomizer {

	/**
	 * Callback to customize a {@link File} instance.
	 *
	 * @param file the file to customize
	 * @param context the processContext
	 * @throws IOException if an I/O error occurs.
	 */
	void customize(File file, Context context) throws IOException;

}
