/*
 * Copyright 2018-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra.test.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * {@link TestRule} to capture output from {@code System.out} and {@code System.err}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
public final class CaptureOutput implements TestRule {

	@Nullable
	private TeeOutputStream out;

	@Nullable
	private TeeOutputStream err;

	@Nullable
	private ByteArrayOutputStream output;

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				capture();
				try {
					base.evaluate();
				}
				finally {
					release();
				}
			}
		};
	}

	@Nonnull
	@Override
	public String toString() {
		flush();
		ByteArrayOutputStream output = this.output;
		if (output == null) {
			return "";
		}
		return output.toString();
	}

	/**
	 * Reset the current output.
	 */
	public void reset() {
		flush();
		ByteArrayOutputStream output = this.output;
		if (output != null) {
			output.reset();
		}
	}

	/**
	 * Capture {@code System.out} and {@code System.err}.
	 */
	public void capture() {
		this.output = new ByteArrayOutputStream();
		this.out = new TeeOutputStream(System.out, this.output);
		this.err = new TeeOutputStream(System.err, this.output);
		System.setOut(new PrintStream(this.out));
		System.setErr(new PrintStream(this.err));
	}

	/**
	 * Release {@code System.out} and {@code System.err}.
	 */
	public void release() {
		flush();
		TeeOutputStream out = this.out;
		if (out != null) {
			System.setOut(out.getOriginal());
		}
		TeeOutputStream err = this.err;
		if (err != null) {
			System.setErr(err.getOriginal());
		}
		this.output = null;
	}

	private void flush() {
		try {

			TeeOutputStream out = this.out;
			if (out != null) {
				out.flush();
			}
			TeeOutputStream err = this.err;
			if (err != null) {
				err.flush();
			}
		}
		catch (IOException ignore) {
		}
	}

	private static final class TeeOutputStream extends OutputStream {

		@Nonnull
		private final PrintStream original;

		@Nonnull
		private final OutputStream delegate;

		TeeOutputStream(@Nonnull PrintStream original, @Nonnull OutputStream delegate) {
			this.original = original;
			this.delegate = delegate;
		}

		@Override
		public void write(int b) throws IOException {
			this.delegate.write(b);
			this.original.write(b);
		}

		@Override
		public void write(@Nonnull byte[] b) throws IOException {
			write(b, 0, b.length);
		}

		@Override
		public void write(@Nonnull byte[] b, int off, int len) throws IOException {
			this.delegate.write(b, off, len);
			this.original.write(b, off, len);
		}

		@Override
		public void flush() throws IOException {
			this.delegate.flush();
			this.original.flush();
		}

		@Nonnull
		PrintStream getOriginal() {
			return this.original;
		}
	}

}
