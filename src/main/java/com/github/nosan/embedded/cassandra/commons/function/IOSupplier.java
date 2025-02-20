/*
 * Copyright 2020-2025 the original author or authors.
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

package com.github.nosan.embedded.cassandra.commons.function;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Represents a supplier that may throw an {@link IOException}.
 *
 * <p>This is a functional interface that can be used as a safety wrapper
 * for operations that supply values and may trigger I/O exceptions.</p>
 *
 * @param <T> the type of the supplied value
 * @author Dmytro Nosan
 * @see #wrap(Supplier)
 * @since 4.0.0
 */
@FunctionalInterface
public interface IOSupplier<T> {

	/**
	 * Wraps the given {@link Supplier} into an {@link IOSupplier}.
	 *
	 * <p>This method adapts a standard Java {@link Supplier} into an {@link IOSupplier}.
	 * The original {@link Supplier}'s {@link Supplier#get()} method is used as the implementation.</p>
	 *
	 * @param supplier The original supplier to be wrapped
	 * @param <T> The type of the supplied value
	 * @return A new {@link IOSupplier} wrapping the provided supplier
	 * @throws NullPointerException if the supplier is {@code null}
	 */
	static <T> IOSupplier<T> wrap(Supplier<? extends T> supplier) {
		Objects.requireNonNull(supplier, "Supplier must not be null");
		return supplier::get;
	}

	/**
	 * Supplies a value, potentially throwing an {@link IOException}.
	 *
	 * @return The supplied value
	 * @throws IOException If an I/O error occurs
	 */
	T get() throws IOException;

}
