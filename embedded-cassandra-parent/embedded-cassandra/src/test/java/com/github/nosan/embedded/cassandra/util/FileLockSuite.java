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

package com.github.nosan.embedded.cassandra.util;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility test class to lock a file.
 *
 * @author Dmytro Nosan
 */
public abstract class FileLockSuite {

	private static final Logger log = LoggerFactory.getLogger(FileLockSuite.class);

	public static void main(String[] args) throws Exception {
		Path lockFile = Paths.get(args[0]);
		log.info("File lock is '{}'", lockFile);
		try (FileLock fileLock = new FileLock(lockFile)) {
			fileLock.lock();
			Path file = Paths.get(args[1]);
			long l = Long.parseLong(new String(Files.readAllBytes(file)));
			log.info("Current value is '{}' in the file '{}'", l, file);
			Files.copy(new ByteArrayInputStream(Long.toString(l + 1).getBytes()), file,
					StandardCopyOption.REPLACE_EXISTING);
		}
	}

}
