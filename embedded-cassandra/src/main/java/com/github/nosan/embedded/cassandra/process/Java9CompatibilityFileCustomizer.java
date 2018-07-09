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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java 9 compatability {@link FileCustomizer File Customizer}.
 *
 * @author Dmytro Nosan
 */
class Java9CompatibilityFileCustomizer implements FileCustomizer {

	private static final FileCustomizer[] FILE_CUSTOMIZERS = new FileCustomizer[]{
			new JVMCompatibilityOptionsCustomizer(),
			new EnvironmentCompatibilityCustomizer()};

	@Override
	public void customize(File file, Context context) throws IOException {
		for (FileCustomizer fileCustomizer : FILE_CUSTOMIZERS) {
			fileCustomizer.customize(file, context);
		}
	}

	private static final class JVMCompatibilityOptionsCustomizer
			extends AbstractFileCustomizer {

		@Override
		protected boolean isMatch(File file, Context context) {
			String fileName = file.getName();
			return fileName.equals("jvm.options");
		}

		@Override
		protected void process(File file, Context context) throws IOException {
			try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
				writer.println("-ea");
				writer.println("-da:net.openhft...");
				writer.println("-XX:+UseThreadPriorities");
				writer.println("-XX:+HeapDumpOnOutOfMemoryError");
				writer.println("-Xss256k");
				writer.println("-XX:StringTableSize=1000003");
				writer.println("-XX:+AlwaysPreTouch");
				writer.println("-XX:-UseBiasedLocking");
				writer.println("-XX:+UseTLAB");
				writer.println("-XX:+ResizeTLAB");
				writer.println("-XX:+UseNUMA");
				writer.println("-XX:+PerfDisableSharedMem");
				writer.println("-Djava.net.preferIPv4Stack=true");
				writer.println("-XX:+UseG1GC");
				writer.println("-XX:+ParallelRefProcEnabled");
				writer.println("-XX:G1RSetUpdatingPauseTimePercent=5");
				writer.println("-XX:MaxGCPauseMillis=500");
			}
		}

	}

	private static final class EnvironmentCompatibilityCustomizer
			extends AbstractSourceLineFileCustomizer {

		private final static Pattern XLOGGC = Pattern.compile("-Xloggc:");


		@Override
		protected String process(String line, File file, Context context) {
			Matcher matcher = XLOGGC.matcher(line);
			if (matcher.find()) {
				return "";
			}
			return line;
		}

		@Override
		protected boolean isMatch(File file, Context context) {
			String fileName = file.getName();
			return fileName.equals("cassandra-env.sh");
		}

	}

}
