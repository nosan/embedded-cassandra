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

import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ArgumentUtils}.
 *
 * @author Dmytro Nosan
 */
public class ArgumentUtilsTests {

	@Test
	public void unixLikeArguments() {
		TestContext context = new TestContext().withPlatform(Platform.Linux);
		IExtractedFileSet fileSet = context.getExtractedFileSet();
		assertThat(ArgumentUtils.get(context)).containsExactly(
				fileSet.executable().getAbsolutePath(), "-f",
				"-Dcassandra.jmx.local.port=0");

	}

	@Test
	public void windowsArguments() {
		TestContext context = new TestContext().withPlatform(Platform.Windows);
		IExtractedFileSet fileSet = context.getExtractedFileSet();
		assertThat(ArgumentUtils.get(context)).containsExactly("powershell",
				"-ExecutionPolicy", "Unrestricted",
				fileSet.executable().getAbsolutePath(), "-f",
				"`-Dcassandra.jmx.local.port=0");

	}

}
