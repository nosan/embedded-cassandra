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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

abstract class AbstractFileCustomizerSupport {

	static File classpath(String classpath) throws Exception {
		return new File(ClassLoader.getSystemResource(classpath).toURI());
	}

	static FileAssertion withFile(String name) throws IOException {
		Path directory = Files.createTempDirectory("test");
		File candidate = directory.resolve(name).toFile();
		Files.createFile(candidate.toPath());
		return new FileAssertion(candidate, (file) -> {
		});
	}

	static final class FileAssertion {

		private final File file;

		private final Consumer consumer;

		private FileAssertion(File file, Consumer consumer) {
			this.file = file;
			this.consumer = consumer;
		}

		FileAssertion from(File source) throws Exception {
			return new FileAssertion(this.file, this.consumer.andThen((file) -> {
				try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
					Files.copy(source.toPath(), fileOutputStream);
				}
			}));
		}

		void accept(Consumer consumer) throws Exception {
			try {
				this.consumer.andThen(consumer).accept(this.file);
			}
			finally {
				Files.deleteIfExists(this.file.toPath());
			}
		}

		interface Consumer {

			void accept(File file) throws Exception;

			default Consumer andThen(Consumer consumer) {
				return (file) -> {
					Consumer.this.accept(file);
					consumer.accept(file);
				};
			}

		}

	}

}
