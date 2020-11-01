/*
 * Copyright 2020 the original author or authors.
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

package com.github.nosan.embedded.cassandra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import com.github.nosan.embedded.cassandra.commons.logging.Logger;

final class DefaultProcess implements Process {

	private static final Logger LOGGER = Logger.get(DefaultProcess.class);

	private final String name;

	private final java.lang.Process process;

	private final ProcessOutput stdout;

	private final ProcessOutput stderr;

	DefaultProcess(String name, java.lang.Process process) {
		this.name = name;
		this.process = process;
		this.stdout = new ProcessOutput(name + ":OUT", process, process.getInputStream());
		this.stderr = new ProcessOutput(name + ":ERR", process, process.getErrorStream());
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int getPid() {
		try {
			return Pid.get(this.process);
		}
		catch (Exception ex) {
			LOGGER.error(ex, "Could not get a PID of a process: ''{0}''", this.process);
			return -1;
		}
	}

	@Override
	public void destroy() {
		this.process.destroy();
	}

	@Override
	public void destroyForcibly() {
		this.process.destroyForcibly();
	}

	@Override
	public boolean isAlive() {
		return this.process.isAlive();
	}

	@Override
	public int waitFor() {
		boolean interrupted = false;
		try {
			while (true) {
				try {
					return this.process.waitFor();
				}
				catch (InterruptedException ex) {
					interrupted = true;
				}
			}
		}
		finally {
			if (interrupted) {
				Thread.currentThread().interrupt();
			}
		}
	}

	@Override
	public boolean waitFor(int timeout, TimeUnit unit) {
		boolean interrupted = false;
		try {
			long remainingNanos = unit.toNanos(timeout);
			long end = System.nanoTime() + remainingNanos;
			while (remainingNanos > 0) {
				try {
					return this.process.waitFor(remainingNanos, TimeUnit.NANOSECONDS);
				}
				catch (InterruptedException ex) {
					interrupted = true;
					remainingNanos = end - System.nanoTime();
				}
			}
		}
		finally {
			if (interrupted) {
				Thread.currentThread().interrupt();
			}
		}
		return false;
	}

	@Override
	public CompletableFuture<? extends Process> onExit() {
		java.lang.Process process = this.process;
		return CompletableFuture.supplyAsync(() -> {
			boolean interrupted = false;
			while (true) {
				try {
					ForkJoinPool.managedBlock(new ForkJoinPool.ManagedBlocker() {

						@Override
						public boolean block() throws InterruptedException {
							process.waitFor();
							return true;
						}

						@Override
						public boolean isReleasable() {
							return !process.isAlive();
						}
					});
					break;
				}
				catch (InterruptedException x) {
					interrupted = true;
				}
			}
			if (interrupted) {
				Thread.currentThread().interrupt();
			}
			return this;
		});
	}

	@Override
	public Output getStdOut() {
		return this.stdout;
	}

	@Override
	public Output getStdErr() {
		return this.stderr;
	}

	@Override
	public String toString() {
		return this.process.toString();
	}

	private static final class ProcessOutput extends Thread implements Output {

		private static final Logger LOGGER = Logger.get(ProcessOutput.class);

		private final ReentrantLock lock = new ReentrantLock();

		private final Condition condition = this.lock.newCondition();

		private final List<Consumer<? super String>> consumers = new CopyOnWriteArrayList<>();

		private final java.lang.Process process;

		private final InputStream is;

		private final AtomicBoolean started = new AtomicBoolean(false);

		ProcessOutput(String name, java.lang.Process process, InputStream is) {
			super(name);
			this.process = process;
			this.is = is;
			setDaemon(true);
			setUncaughtExceptionHandler((thread, ex) -> LOGGER.error(ex, "Exception in thread: ''{0}''", thread));
		}

		@Override
		public void run() {
			try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.is))) {
				String line;
				while ((line = readLine(bufferedReader)) != null) {
					this.lock.lock();
					try {
						while (this.process.isAlive() && this.consumers.isEmpty()) {
							this.condition.await(100, TimeUnit.MILLISECONDS);
						}
						for (Consumer<? super String> consumer : this.consumers) {
							consumer.accept(line);
						}
					}
					finally {
						this.lock.unlock();
					}
				}
			}
			catch (IOException ex) {
				throw new UncheckedIOException(ex);
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}

		@Override
		public void attach(Consumer<? super String> consumer) {
			doStart();
			this.lock.lock();
			try {
				this.consumers.add(consumer);
				this.condition.signalAll();
			}
			finally {
				this.lock.unlock();
			}
		}

		@Override
		public void detach(Consumer<? super String> consumer) {
			this.lock.lock();
			try {
				this.consumers.remove(consumer);
				this.condition.signalAll();
			}
			finally {
				this.lock.unlock();
			}
		}

		private void doStart() {
			if (this.started.compareAndSet(false, true)) {
				start();
			}
		}

		private static String readLine(BufferedReader reader) throws IOException {
			try {
				return reader.readLine();
			}
			catch (IOException ex) {
				if (Objects.toString(ex.getMessage(), "").contains("Stream closed")) {
					return null;
				}
				throw ex;
			}
		}

	}

	private static final class Pid {

		private static int get(java.lang.Process process) throws InvocationTargetException, IllegalAccessException {
			try {
				Method method = java.lang.Process.class.getMethod("pid");
				return getInt(method.invoke(process));
			}
			catch (NoSuchMethodException ex) {
				//ignore
			}
			try {
				Field field = process.getClass().getDeclaredField("pid");
				field.setAccessible(true);
				return getInt(field.get(process));
			}
			catch (NoSuchFieldException ex) {
				return -1;
			}

		}

		private static int getInt(Object result) {
			return Integer.parseInt(Objects.toString(result, "-1"));
		}

	}

}
