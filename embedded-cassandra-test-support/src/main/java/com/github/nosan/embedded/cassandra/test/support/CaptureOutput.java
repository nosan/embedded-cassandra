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

package com.github.nosan.embedded.cassandra.test.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;

/**
 * {@code CaptureOutput} captures output to {@code System.out} and {@code System.err}.
 *
 * @author Dmytro Nosan
 * @since 1.4.2
 */
public final class CaptureOutput {

	private final TeeOutputStream systemOut;

	private final TeeOutputStream systemErr;

	private final ByteArrayOutputStream output;

	private CaptureOutput(TeeOutputStream systemOut, TeeOutputStream systemErr, ByteArrayOutputStream output) {
		this.systemOut = systemOut;
		this.systemErr = systemErr;
		this.output = output;
	}

	/**
	 * Capture the  {@code System.out} and {@code System.err} outputs.
	 *
	 * @return the capture
	 */
	public static CaptureOutput capture() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		TeeOutputStream systemOut = new TeeOutputStream(System.out, output);
		TeeOutputStream systemErr = new TeeOutputStream(System.err, output);
		System.setOut(new PrintStream(systemOut));
		System.setErr(new PrintStream(systemErr));
		return new CaptureOutput(systemOut, systemErr, output);
	}

	/**
	 * Release the  {@code System.out} and {@code System.err} outputs.
	 */
	public void release() {
		flush();
		System.setOut(this.systemOut.getOriginal());
		System.setErr(this.systemErr.getOriginal());
		this.output.reset();
	}

	/**
	 * Return all captured output to {@code System.out} and {@code System.err} as a single string.
	 */
	@Override
	public String toString() {
		flush();
		return this.output.toString();
	}

	/**
	 * Clean the current captured output.
	 */
	public void reset() {
		flush();
		this.output.reset();
	}

	private void flush() {
		try {
			this.systemOut.flush();
			this.systemErr.flush();
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private static final class TeeOutputStream extends OutputStream {

		private final PrintStream original;

		private final OutputStream delegate;

		TeeOutputStream(PrintStream original, OutputStream delegate) {
			this.original = original;
			this.delegate = delegate;
		}

		@Override
		public void write(int b) throws IOException {
			this.delegate.write(b);
			this.original.write(b);
		}

		@Override
		public void write(byte[] b) throws IOException {
			write(b, 0, b.length);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			this.delegate.write(b, off, len);
			this.original.write(b, off, len);
		}

		@Override
		public void flush() throws IOException {
			this.delegate.flush();
			this.original.flush();
		}

		PrintStream getOriginal() {
			return this.original;
		}

	}

}
